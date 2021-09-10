package net.medimuse.emotibitconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author peterslack
 */
public class UDPServer extends Thread {

	private Boolean running = false;
	private EmotibitEcho myParent;
	private DatagramSocket socket;

	public static final Integer MAXIMUM_QUEUE_SIZE = 3000;
	private Integer queueSize = MAXIMUM_QUEUE_SIZE;

	private final BlockingQueue<String> incomingQueue = new PriorityBlockingQueue<String>();

	private final Map<Emotibit.TypeTag, Map<Long, Double>> processedDataMap = Collections
			.synchronizedMap(new HashMap<>());

	private final Map<Emotibit.TypeTag, Map<Long, Double>> processedDataBlock = Collections
			.synchronizedMap(new HashMap<>());

	public UDPServer(int port, EmotibitEcho parent) {

		myParent = parent;

		initializeDataMaps();

		try {
			socket = new DatagramSocket(port);
		} catch (SocketException ex) {
			Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private void initializeDataMaps() {
		synchronized (processedDataMap) {
			processedDataMap.clear();
			// initialize our data maps based on what we know
			// are signals
			for (Emotibit.TypeTag tt : Emotibit.sampleRateMap.keySet()) {
				processedDataMap.put(tt, new HashMap<>());
			}
		}
	}

	public final Map<Long, Double> getDataLine(Emotibit.TypeTag type) {
		synchronized (processedDataMap) {
			return processedDataMap.get(type);
		}
	}

	public void clearAllData() {
		synchronized (processedDataMap) {
			for (Emotibit.TypeTag tt : processedDataMap.keySet()) {
				processedDataMap.get(tt).clear();
			}
		}
	}

	private final void addToDataBlock(Emotibit.TypeTag tag, Long time, Double val) {
		synchronized (processedDataBlock) {
			Map<Long, Double> t = processedDataBlock.get(tag);
			if (t == null) {
				t = new HashMap<>();
				processedDataBlock.put(tag, t);
			}
			t.put(time, val);
		}
	}

	private final void processData(String data) {
		Emotibit.Header h = Emotibit.Header.parseHeader(data);
		if (h != null) {
			Emotibit.TypeTag tt = Emotibit.STT(h.typeTag);
			if (tt == null) {
				return;
			}
			Double freq = Emotibit.getSignalSamplerate(tt);
			if (freq == null) {
				System.out.println(data);
				return;
			}
			List<String> packetData = h.getDataStrings();
			synchronized (processedDataMap) {
				Map<Long, Double> dataset = processedDataMap.get(tt);
				Long period = (long) (1 / freq * 1000);
				int timeOffset = 0;
				if (packetData.size() > 0) {
					for (String dl : packetData) {
						Double d = Double.parseDouble(dl);
						// dataset.put(h.timeStamp + timeOffset, d);
						addToDataBlock(tt, h.timeStamp + timeOffset, d);
						timeOffset += period;
					}
				}
			}
		}
	}

	private Thread processor = new Thread() {
		@Override
		public void run() {
			while (running) {
				String j = "";
				try {
					j = incomingQueue.take();
				} catch (InterruptedException ex) {
					// Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
				}
				Reader inputString = new StringReader(j);
				BufferedReader reader = new BufferedReader(inputString);
				synchronized (processedDataBlock) {
					processedDataBlock.clear();
				}
				String line = "";
				while (line != null) {
					try {
						line = reader.readLine();
					} catch (IOException ex) {
						Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
					}
					if (line != null) {
						processData(line);
					}
				}

				myParent.dataBlock(processedDataBlock);

			}
		}
	};

	public void stopRunning() {
		running = false;
		processor.interrupt();
		this.interrupt();
	}

	private byte[] buf = new byte[EmotibitEcho.MIN_BUFFER_SIZE];

	@Override
	public void run() {
		running = true;
		processor.start();
		while (running) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
			} catch (IOException ex) {
				Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
			}

			String st = new String(packet.getData(), 0, packet.getLength());

			if (incomingQueue.size() < queueSize) {
				incomingQueue.add(st);
			}

		}

		System.out.println("exit UDP Service");
	}
}

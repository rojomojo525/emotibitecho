package net.medimuse.emotibitconnector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import net.medimuse.emotibitconnector.Emotibit.TypeTag;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author peterslack
 */
public class EmotibitEcho extends Thread implements EmotibitEchoListener {

	public static final int MIN_BUFFER_SIZE = 10000;
	private DatagramSocket socket;
	private Boolean running = false;
	private final EmotibitEchoListener myListener;
	private final StateMachine machineListener;
	/** this is class that listens on the broadcast / advertising address */
	private final EmotibitReceiver myReceiver;
	/** The Emotibit Device we are currently connected to */
	private EmotibitDevice connectedDevice = null;

	private final UDPServer udpData;
	private final TCPServer control;
	private final HashMap<InetAddress, EmotibitDevice> deviceMap = new HashMap<>();
	private final List<String> errorList = new ArrayList<>();

	public Map<InetAddress, EmotibitDevice> getDeviceMap() {
		return deviceMap;
	}

	public static enum MachineState {
		INITIALIZING, SEARCHING, CONNECTING, CONNECTED, DISCONNECTING, READY, STOPPED, ERROR
	}

	public static enum StateChangeMessage {
		REQUEST_CONNECT, REQUEST_DISCONNECT, STATUS_CONNECTED, STATUS_DISCOVERED, REQUEST_STOP, REQUEST_START,
		REQUEST_RESET

	}

	public static enum MachineError {
		WRONG_CONNECT, // a machine we were not expecting connected
		CONNECTION_TIMEOUT, // we timed out while trying to connect to a device
		NETWORK_ERROR
	}

	private MachineState currentState = MachineState.INITIALIZING;

	/**
	 * 
	 * @return
	 */
	public MachineState getCurrentState() {
		return currentState;
	}

	/**
	 * 
	 */
	public interface StateMachine {

		public void stateChangeMessage(StateChangeMessage msg);

		public void stateChange(MachineState newState);

		public void stateError(MachineError error);

	}

	/**
	 * 
	 * @param newState
	 */
	protected void changeState(MachineState newState) {
		this.currentState = newState;
		// Runs inside of the Swing UI thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				machineListener.stateChange(newState);
			}
		});
	}

	/**
	 * Send a machine error state message
	 * 
	 * @param error
	 */
	protected void machineError(MachineError error) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				machineListener.stateError(error);
			}
		});
	}

	public List<String> getErrorList() {
		return errorList;
	}

	/**
	 *
	 * @param msg
	 */
	public void sendStateMessage(StateChangeMessage msg) {

		MachineState state = getCurrentState();

		if (msg == StateChangeMessage.STATUS_DISCOVERED) {
			if (state == MachineState.SEARCHING) {
				changeState(MachineState.READY);
			}
		} else if (msg == StateChangeMessage.REQUEST_CONNECT) {
			if (state != MachineState.CONNECTING) {
				changeState(MachineState.CONNECTING);
			}
		} else if (msg == StateChangeMessage.REQUEST_STOP) {
			stopRunning();
		} else if (msg == StateChangeMessage.STATUS_CONNECTED) {
			// get the address from control
			InetAddress addy = control.getConnectedDeviceAddress();
			EmotibitDevice connected = deviceMap.get(addy);
			if (connected == null) {
				// something other than a device we know connected

			} else {
				connectedDevice = connected;
				changeState(MachineState.CONNECTED);
			}
		} else if (msg == StateChangeMessage.REQUEST_DISCONNECT) {

			if (state == MachineState.CONNECTED) {
				disconnectFromEmotibit();
				connectedDevice = null;
				changeState(MachineState.DISCONNECTING);
			}
		} else if (msg == StateChangeMessage.REQUEST_START) {
			if (state == MachineState.STOPPED || state == MachineState.INITIALIZING) {
				start();
			}
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				machineListener.stateChangeMessage(msg);
			}
		});

	}

	/**
	 *
	 * @param listener
	 * @param machineListener
	 */
	public EmotibitEcho(EmotibitEchoListener listener, StateMachine machineListener) {
		myListener = listener;
		this.machineListener = machineListener;
		changeState(MachineState.INITIALIZING);

		// some networking checks
		List<InetAddress> addresses = null;
		try {
			addresses = listAllBroadcastAddresses();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		myReceiver = new EmotibitReceiver(this);
		udpData = new UDPServer(Emotibit.EMOTIBIT_ADVERTISEMENT_PORT + 2, this);
		control = new TCPServer(Emotibit.EMOTIBIT_ADVERTISEMENT_PORT + 1, this);

		if (addresses != null && !addresses.isEmpty()) {

		} else {

		}
	}

	/**
	 * 
	 * @param address
	 */
	@Override
	public void emotibitDiscovered(InetAddress address) {
		myListener.emotibitDiscovered(address);
	}

	/**
	 * 
	 * @param incoming
	 * @param state
	 */
	private void emotibitDiscovered(InetAddress incoming, int state) {
		EmotibitDevice d = deviceMap.get(incoming);
		if (d == null) {
			d = new EmotibitDevice();
			d.setAddress(incoming);
			d.setState(state);
			deviceMap.put(incoming, d);
			sendStateMessage(StateChangeMessage.STATUS_DISCOVERED);
			emotibitDiscovered(incoming);
		} else {
			d.setState(state);
		}
	}

	/**
	 * 
	 */
	private void disconnectFromEmotibit() {
		MachineState state = getCurrentState();
		if (state == MachineState.SEARCHING || state == MachineState.READY || connectedDevice == null) {
			// if we're still looking for an emotibit or already diconnected then return
			return;
		}
		String packet = Emotibit.createPacket(Emotibit.TTS(Emotibit.TypeTag.EMOTIBIT_DISCONNECT), 0,
				new ArrayList<String>());
		control.write(packet);
		control.changeState(TCPServer.ControlState.DISCONNECTING);
	}

	/**
	 * 
	 * @param emote
	 */
	public void connectToEmotibit(InetAddress emote) {
		MachineState state = getCurrentState();
		if (state == MachineState.SEARCHING || state == MachineState.ERROR) {
			// if we're still looking for an emotibit or we are in an error state then
			// return
			return;
		}

		// first check if we are already connected
		if (connectedDevice != null) {
			if (connectedDevice.getAddress() == emote) {
				// we're already connected
				System.out.println("Already connecteds to this device");
				return;
			} else {
				// we need to diconnect from the connected device

			}

		}

		if (emote != null) {
			sendStateMessage(StateChangeMessage.REQUEST_CONNECT);
			try {
				ArrayList<String> payload = new ArrayList<String>();
				payload.add(Emotibit.PTS(Emotibit.PayloadLabel.CONTROL_PORT));
				payload.add(String.valueOf(Emotibit.EMOTIBIT_ADVERTISEMENT_PORT + 1));
				payload.add(Emotibit.PTS(Emotibit.PayloadLabel.DATA_PORT));
				payload.add(String.valueOf(Emotibit.EMOTIBIT_ADVERTISEMENT_PORT + 2));
				String packet = Emotibit.createPacket(Emotibit.TTS(Emotibit.TypeTag.EMOTIBIT_CONNECT), 0, payload);
				DatagramPacket pack = new DatagramPacket(packet.getBytes(), 0, packet.length(), emote,
						Emotibit.EMOTIBIT_ADVERTISEMENT_PORT);
				socket.send(pack);
			} catch (IOException ex) {
				Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void reset() {

	}

	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	private void stopRunning() {
		running = false;
		this.interrupt();

		udpData.stopRunning();
		control.stopServer();

		try {
			this.join();
		} catch (InterruptedException ex) {
			Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	private Integer packetCounter = 0;

	private void sendPacket(String packetout, InetAddress address, int port, boolean broadcast) {

		byte[] buffer = packetout.getBytes();

		try {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
			socket.send(packet);

		} catch (SocketException ex) {
			Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
			errorList.add(ex.getMessage() + "   ERROR");
			changeState(MachineState.ERROR);
		}

		packetCounter++;
	}

	@Override
	public void run() {

		running = true;
		long sleeptime = 1000;
		myReceiver.start();
		udpData.start();
		control.start();

		changeState(MachineState.SEARCHING);

		while (running) {

			if (null != getCurrentState())
				switch (getCurrentState()) {
				case SEARCHING:
					sleeptime = 1000;
					// in this state we search for emotibits by advertising on the broadcast address
					try {

						List<InetAddress> broadcasters = listAllBroadcastAddresses();
						if (broadcasters != null && !broadcasters.isEmpty()) {
							String hello = Emotibit.createPacket(Emotibit.TTS(Emotibit.TypeTag.HELLO_EMOTIBIT),
									packetCounter, new ArrayList<String>());
							InetAddress address = broadcasters.get(0);
							sendPacket(hello, address, Emotibit.EMOTIBIT_ADVERTISEMENT_PORT, true);
						}
					} catch (SocketException ex) {
						Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
					}
					break;
				case READY:
					// in this state we broadcast for emotibits and we also ping the known emotibits
					sleeptime = 1000;
					for (InetAddress i : deviceMap.keySet()) {

						String ping = Emotibit.createPacket(Emotibit.TTS(Emotibit.TypeTag.HELLO_EMOTIBIT),
								packetCounter, new ArrayList<String>());
						sendPacket(ping, i, Emotibit.EMOTIBIT_ADVERTISEMENT_PORT, false);

					}
					break;
				case CONNECTED:
					// in this state we need to keep our connection alive
					sleeptime = 400;
					if (connectedDevice != null) {

						// String ping = Emotibit.createPacket(Emotibit.HELLO_EMOTIBIT, packetCounter,
						// new ArrayList<String>());
						// sendPacket(ping, connectedDevice.getAddress(),
						// Emotibit.EMOTIBIT_ADVERTISEMENT_PORT, false);
						ArrayList<String> payload = new ArrayList<String>();
						payload.add(Emotibit.PTS(Emotibit.PayloadLabel.DATA_PORT));
						payload.add(String.valueOf(Emotibit.EMOTIBIT_ADVERTISEMENT_PORT + 2));
						String ping = Emotibit.createPacket(Emotibit.TTS(Emotibit.TypeTag.PING), packetCounter,
								payload);
						sendPacket(ping, connectedDevice.getAddress(), Emotibit.EMOTIBIT_ADVERTISEMENT_PORT, false);

					}
					break;
				default:
					break;
				}

			try {
				sleep(sleeptime);
			} catch (InterruptedException ex) {
				// Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
			}

		}

		myReceiver.stopRunning();

	}

	public static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
		List<InetAddress> broadcastList = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();

			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				continue;
			}

			networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
					.forEach(broadcastList::add);
		}
		return broadcastList;
	}

	public static List<InetAddress> listAllInterfaceAddresses() throws SocketException {
		List<InetAddress> broadcastList = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();

			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				continue;
			}

			networkInterface.getInterfaceAddresses().stream().map(a -> a.getAddress()).filter(Objects::nonNull)
					.forEach(broadcastList::add);
		}
		return broadcastList;
	}

	private class EmotibitReceiver extends Thread {

		private Boolean running = false;
		private EmotibitEcho myParent;

		public EmotibitReceiver(EmotibitEcho listener) {
			myParent = listener;

			try {
				socket = new DatagramSocket(Emotibit.EMOTIBIT_ADVERTISEMENT_PORT);
				socket.setBroadcast(true);
			} catch (SocketException ex) {
				Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);

			}

		}

		public void stopRunning() {
			running = false;
			this.interrupt();
		}

		private byte[] buf = new byte[MIN_BUFFER_SIZE];

		@Override
		public void run() {
			try {
				running = true;
				List<InetAddress> localAddresses = listAllInterfaceAddresses();

				while (running) {
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					try {
						socket.receive(packet);
					} catch (IOException ex) {
						Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
					}
					String received = new String(packet.getData(), 0, packet.getLength());
					InetAddress incoming = packet.getAddress();

					if (!localAddresses.contains(incoming)) {
						Emotibit.Header h = Emotibit.Header.parseHeader(received.trim());
						if (h != null) {
//                            System.out.println("parsed " + h.toString());
//                            System.out.println("originial packet : " + received);
							if (h.typeTag.equals(Emotibit.TTS(Emotibit.TypeTag.HELLO_HOST))) {
								Map<String, String> payload = Emotibit.Header.getPayload(received);
								String state = payload.get(Emotibit.PTS(Emotibit.PayloadLabel.DATA_PORT));
								if (state != null) {
									int stateval = Integer.parseInt(state);
									myParent.emotibitDiscovered(incoming, stateval);

								}
							}
						}
					}
				}

			} catch (SocketException ex) {
				Logger.getLogger(EmotibitEcho.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

	}

	@Override
	public void dataBlock(Map<TypeTag, Map<Long, Double>> data) {
		myListener.dataBlock(data);
	}

}

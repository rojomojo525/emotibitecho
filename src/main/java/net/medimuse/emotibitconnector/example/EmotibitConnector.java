package net.medimuse.emotibitconnector.example;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.medimuse.emotibitconnector.EmotibitDevice;
import net.medimuse.emotibitconnector.EmotibitEcho;
import net.medimuse.emotibitconnector.EmotibitEchoListener;
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
public class EmotibitConnector extends JFrame implements EmotibitEchoListener, EmotibitEcho.StateMachine {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4547566775986324162L;

	private final EmotibitEcho myEcho;

	final JTextArea errorLog;
	JButton diconnectBtn;

	public EmotibitConnector() throws Exception {
		myEcho = new EmotibitEcho(this, this);

		errorLog = new JTextArea(30, 30);

		initialize();

	}

	protected void logStateMachine(String msg) {
		String txt = errorLog.getText();

		txt += msg + "\n";

		errorLog.setText(txt);

	}

	private void initialize() {

		diconnectBtn = new JButton("Disconnect");
		diconnectBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				myEcho.sendStateMessage(EmotibitEcho.StateChangeMessage.REQUEST_DISCONNECT);
			}
		});
		diconnectBtn.enableInputMethods(false);

		final JScrollPane scroll = new JScrollPane(errorLog);

		setLayout(new FlowLayout(FlowLayout.CENTER));
		getContentPane().add(scroll);
		getContentPane().add(diconnectBtn);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		setSize(new Dimension(600, 600));

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e); // To change body of generated methods, choose Tools | Templates.
				myEcho.sendStateMessage(EmotibitEcho.StateChangeMessage.REQUEST_STOP);
			}
		});

		myEcho.start();

	}

	/**
	 *
	 */
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					EmotibitConnector ec = new EmotibitConnector();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

	}

	@Override
	public void emotibitDiscovered(InetAddress address) {

	}

	private void addDeviceButton(EmotibitDevice device) {
		DeviceLaunchButton d = new DeviceLaunchButton(device);
		getContentPane().add(d);
		d.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				myEcho.connectToEmotibit(device.getAddress());
			}
		});
	}

	@Override
	public void stateChangeMessage(EmotibitEcho.StateChangeMessage msg) {
		switch (msg) {
		case REQUEST_CONNECT:
			logStateMachine("Requested conection ");
			break;
		case REQUEST_DISCONNECT:
			logStateMachine("Disconnected from Emotibit ");
			break;
		case STATUS_CONNECTED:
			logStateMachine("Connected to Emotibit ");
			diconnectBtn.enableInputMethods(false);
			break;
		case STATUS_DISCOVERED:
			logStateMachine("Discovered Emotibit ");
			Map<InetAddress, EmotibitDevice> map = myEcho.getDeviceMap();
			for (InetAddress i : map.keySet()) {
				addDeviceButton(map.get(i));
			}
			break;
		case REQUEST_STOP:
			System.exit(0);

		}
	}

	@Override
	public void stateChange(EmotibitEcho.MachineState newState) {
		switch (newState) {
		case INITIALIZING:
			logStateMachine("STATE : INITIALIZING ");
			break;
		case CONNECTING:
			logStateMachine("STATE : CONNECTING ");
			break;
		case SEARCHING:
			logStateMachine("STATE : SEARCHING ");
			break;
		case CONNECTED:
			logStateMachine("STATE : CONNECTED ");
			break;
		case READY:
			logStateMachine("STATE : DISCONNECTED ");
			break;
		case DISCONNECTING:
			logStateMachine("STATE : DISCONNECTED ");
			break;
		case ERROR:
			logStateMachine("STATE : ERROR ");
			break;

		}
	}

	@Override
	public void stateError(EmotibitEcho.MachineError error) {
		switch (error) {
		case WRONG_CONNECT:
			logStateMachine("ERROR : WRONG_CONNECT ");
		case CONNECTION_TIMEOUT:
			break;

		}
	}

	@Override
	public void dataBlock(Map<TypeTag, Map<Long, Double>> data) {

	}

}

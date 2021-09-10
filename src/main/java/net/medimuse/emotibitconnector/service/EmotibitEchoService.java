/**
 * 
 */
package net.medimuse.emotibitconnector.service;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import net.medimuse.emotibitconnector.EmotibitDevice;
import net.medimuse.emotibitconnector.EmotibitEcho;
import net.medimuse.emotibitconnector.EmotibitEchoListener;
import net.medimuse.emotibitconnector.Emotibit.TypeTag;
import net.medimuse.emotibitconnector.EmotibitEcho.MachineError;
import net.medimuse.emotibitconnector.EmotibitEcho.MachineState;
import net.medimuse.emotibitconnector.EmotibitEcho.StateChangeMessage;
import net.medimuse.emotibitconnector.EmotibitEcho.StateMachine;

/**
 * @author peterslack
 *
 */
@Component
public class EmotibitEchoService implements IEmotibitEchoService, StateMachine, EmotibitEchoListener {
	private final EmotibitEcho myEcho;

	@Reference
	EventAdmin eventAdmin;

	/**
	 * @throws Exception
	 * 
	 */
	public EmotibitEchoService() throws Exception {
		myEcho = new EmotibitEcho(this, this);
	}

	@Activate
	public void activate() {
		// System.out.println("Activated");
		// myEcho.start();
		myEcho.sendStateMessage(EmotibitEcho.StateChangeMessage.REQUEST_START);
	}

	@Deactivate
	public void deactivate() {
		// System.out.println("DEACTIVATE");
		myEcho.sendStateMessage(EmotibitEcho.StateChangeMessage.REQUEST_STOP);
	}

	public static void tickle() {
	}

	@Override
	public EmotibitEcho getEcho() {
		return myEcho;
	}

	@Override
	public void emotibitDiscovered(InetAddress address) {
		// TODO Auto-generated method stub
		if (eventAdmin != null) {
			Map<String, InetAddress> vals = new HashMap<String, InetAddress>();
			vals.put(EmotibitEventConstants.PROPERTY_KEY_TARGET, address);
			eventAdmin.sendEvent(new Event(EmotibitEventConstants.TOPIC_DISCOVERED, vals));
		}
	}

	@Override
	public void stateChangeMessage(StateChangeMessage msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateChange(MachineState newState) {
		// TODO Auto-generated method stub
		if (eventAdmin != null) {
			Map<String, MachineState> vals = new HashMap<String, MachineState>();
			vals.put(EmotibitEventConstants.PROPERTY_KEY_TARGET, newState);
			eventAdmin.sendEvent(new Event(EmotibitEventConstants.TOPIC_STATE_MACHINE, vals));
		}

	}

	@Override
	public void stateError(MachineError error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectEmotibit(String address) {
		Map<InetAddress, EmotibitDevice> devices = myEcho.getDeviceMap();

		for (InetAddress addy : devices.keySet()) {

			if (addy.toString().contains(address)) {
				myEcho.connectToEmotibit(addy);
				continue;
			}
		}

	}

	@Override
	public void disconnectEmotibit() {
		myEcho.sendStateMessage(StateChangeMessage.REQUEST_DISCONNECT);
	}

	@Override
	public void dataBlock(Map<TypeTag, Map<Long, Double>> data) {
		if (eventAdmin != null) {
			Map<TypeTag, Map<Long, Double>> copy = new HashMap<TypeTag, Map<Long, Double>>(data);

			Map<String, Object> datapost = new HashMap<String, Object>();
			datapost.put(EmotibitEventConstants.PROPERTY_KEY_TARGET, copy);
			eventAdmin.postEvent(new Event(EmotibitEventConstants.TOPIC_DATA_PACKET, datapost));
		}

	}

}

package net.medimuse.emotibitconnector.example;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.swing.JButton;

import net.medimuse.emotibitconnector.EmotibitDevice;

/**
 *
 * @author peterslack
 */
public class DeviceLaunchButton extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6291314096113861742L;
	EmotibitDevice myDevice;

	public EmotibitDevice getDevice() {
		return myDevice;
	}

	public DeviceLaunchButton(EmotibitDevice device) {

		myDevice = device;

		initialize();

	}

	private void initialize() {
		if (myDevice.getState() != -1) {
			setEnabled(false);
		}

		setText(myDevice.getAddress().toString());
	}

}

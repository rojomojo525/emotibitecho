package net.medimuse.emotibitconnector;

import java.net.InetAddress;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author peterslack
 */
public class EmotibitDevice {
    
    private InetAddress address;
    private Integer state;
    private Float batteryLevel =0.0f;
    private Boolean recording = false;
    
    
    public static enum PowerMode {
    
        /**
         * In normal mode, the EmotiBit works with complete functionality, being able to record and transmit data.
         */
        NORMAL,
        /**
         * In Low power mode, the EmotiBit can record but cannot transmit data in real-time. 
         * It, however, continues to get the time-sync pulses.
         */
        LOW_POWER,
        
        /**
         * This mode causes the EmotiBit to shut down the onboard WiFi shield. 
         * This saves power and enables long recording sessions. However, since the 
         * WiFi shield is Off, the EmotiBit cannot get time-sync pulses, 
         * which can lead to less accurate time stamping. A long press of the 
         * EmotiBit button toggles normal mode and WiFi off mode. If using the 
         * EmotiBit in WiFi off mode, we recommend leaving the EmotiBit running for a 
         * couple of minutes towards the end of the record session in normal mode. 
         * This can potentially help with time-syncing issues.
         */
        WIFI_OFF,
    
        /**
         * In hibernate mode, EmotiBit stops any tasks it is performing and goes to sleep. 
         * We recommend switching the EmotiBit into Hibernate mode instead of un-plugging
         *the EmotiBit battery when not in use.
         */
        HIBERNATE
    
    }
    
    
    
    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

	public Float getBatteryLevel() {
		return batteryLevel;
	}

	public void setBatteryLevel(Float batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public Boolean getRecording() {
		return recording;
	}

	public void setRecording(Boolean recording) {
		this.recording = recording;
	}
    
    
    
}

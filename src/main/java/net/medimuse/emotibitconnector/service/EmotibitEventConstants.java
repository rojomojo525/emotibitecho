/**
 * 
 */
package net.medimuse.emotibitconnector.service;

/**
 * @author peterslack
 *
 */
public final class EmotibitEventConstants {


	public static final String TOPIC_BASE = "net/maplepost/events/";
    public static final String TOPIC_STATE_MACHINE = TOPIC_BASE + "STATEMACHINE";
    
    public static final String TOPIC_DISCOVERED= TOPIC_BASE + "DISCOVERED";
    public static final String TOPIC_DATA_PACKET= TOPIC_BASE + "DATA";
    public static final String TOPIC_ALL = TOPIC_BASE + "*";

    public static final String PROPERTY_KEY_TARGET = "target";
	
	/**
	 * 
	 */
	public EmotibitEventConstants() {
		
	}

}

package net.medimuse.emotibitconnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author peterslack
 */
public class Emotibit {

	public static enum TypeTag {
		//// Advertising TypeTags
		HELLO_EMOTIBIT, HELLO_HOST, EMOTIBIT_CONNECT, PING, PONG,
		//// Data Tag Types
		EDA, EDL, EDR, PPG_INFRARED, PPG_RED, PPG_GREEN, SPO2, TEMPERATURE_0, TEMPERATURE_1, THERMOPILE, HUMIDITY_0,
		ACCELEROMETER_X, ACCELEROMETER_Y, ACCELEROMETER_Z, GYROSCOPE_X, GYROSCOPE_Y, GYROSCOPE_Z, MAGNETOMETER_X,
		MAGNETOMETER_Y, MAGNETOMETER_Z, BATTERY_VOLTAGE, BATTERY_PERCENT,
		//// Control TypeTags
		EMOTIBIT_DISCONNECT

	}

	public static enum PayloadLabel {
		CONTROL_PORT, DATA_PORT, RECORDING_STATUS, POWER_STATUS
	}

	public static final Map<String, TypeTag> typeTagStringMap;
	public static final Map<TypeTag, String> typeTagTypeMap;

	public static final Map<String, PayloadLabel> payloadStringMap;
	public static final Map<PayloadLabel, String> payloadTypeMap;
	public static final Map<TypeTag, Double> sampleRateMap;

	static {
		typeTagStringMap = new HashMap<>();
		typeTagTypeMap = new HashMap<>();
		sampleRateMap = new HashMap<>();

		typeTagStringMap.put("EC", TypeTag.EMOTIBIT_CONNECT);
		typeTagStringMap.put("HE", TypeTag.HELLO_EMOTIBIT);
		typeTagStringMap.put("HH", TypeTag.HELLO_HOST);
		typeTagStringMap.put("PN", TypeTag.PING);
		typeTagStringMap.put("PO", TypeTag.PONG);

		typeTagStringMap.put("EA", TypeTag.EDA);
		typeTagStringMap.put("EL", TypeTag.EDL);
		typeTagStringMap.put("ER", TypeTag.EDR);
		typeTagStringMap.put("PI", TypeTag.PPG_INFRARED);
		typeTagStringMap.put("PR", TypeTag.PPG_RED);
		typeTagStringMap.put("PG", TypeTag.PPG_GREEN);
		typeTagStringMap.put("O2", TypeTag.SPO2);
		typeTagStringMap.put("T0", TypeTag.TEMPERATURE_0);
		typeTagStringMap.put("T1", TypeTag.TEMPERATURE_1);
		typeTagStringMap.put("TH", TypeTag.THERMOPILE);
		typeTagStringMap.put("H0", TypeTag.HUMIDITY_0);
		typeTagStringMap.put("AX", TypeTag.ACCELEROMETER_X);
		typeTagStringMap.put("AY", TypeTag.ACCELEROMETER_Y);
		typeTagStringMap.put("AZ", TypeTag.ACCELEROMETER_Z);

		typeTagStringMap.put("GX", TypeTag.GYROSCOPE_X);
		typeTagStringMap.put("GY", TypeTag.GYROSCOPE_Y);
		typeTagStringMap.put("GZ", TypeTag.GYROSCOPE_Z);

		typeTagStringMap.put("MX", TypeTag.MAGNETOMETER_X);
		typeTagStringMap.put("MY", TypeTag.MAGNETOMETER_Y);
		typeTagStringMap.put("MZ", TypeTag.MAGNETOMETER_Z);

		sampleRateMap.put(TypeTag.EDA, 15.0);
		sampleRateMap.put(TypeTag.EDL, 15.0);
		sampleRateMap.put(TypeTag.EDR, 15.0);

		sampleRateMap.put(TypeTag.THERMOPILE, 7.5);
		sampleRateMap.put(TypeTag.TEMPERATURE_0, 7.5);
		sampleRateMap.put(TypeTag.TEMPERATURE_1, 7.5);
		sampleRateMap.put(TypeTag.HUMIDITY_0, 7.5);

		sampleRateMap.put(TypeTag.PPG_GREEN, 25.0);
		sampleRateMap.put(TypeTag.PPG_RED, 25.0);
		sampleRateMap.put(TypeTag.PPG_INFRARED, 25.0);

		sampleRateMap.put(TypeTag.ACCELEROMETER_X, 25.0);
		sampleRateMap.put(TypeTag.ACCELEROMETER_Y, 25.0);
		sampleRateMap.put(TypeTag.ACCELEROMETER_Z, 25.0);

		sampleRateMap.put(TypeTag.GYROSCOPE_X, 25.0);
		sampleRateMap.put(TypeTag.GYROSCOPE_Y, 25.0);
		sampleRateMap.put(TypeTag.GYROSCOPE_Z, 25.0);

		sampleRateMap.put(TypeTag.MAGNETOMETER_X, 25.0);
		sampleRateMap.put(TypeTag.MAGNETOMETER_Y, 25.0);
		sampleRateMap.put(TypeTag.MAGNETOMETER_Z, 25.0);

		typeTagStringMap.put("ED", TypeTag.EMOTIBIT_DISCONNECT);

		for (String mnemonic : typeTagStringMap.keySet()) {
			typeTagTypeMap.put(typeTagStringMap.get(mnemonic), mnemonic);
		}

		payloadStringMap = new HashMap<>();
		payloadTypeMap = new HashMap<>();

		payloadStringMap.put("CP", PayloadLabel.CONTROL_PORT);
		payloadStringMap.put("DP", PayloadLabel.DATA_PORT);
		payloadStringMap.put("RS", PayloadLabel.RECORDING_STATUS);
		payloadStringMap.put("PS", PayloadLabel.POWER_STATUS);

		for (String mnemonic : payloadStringMap.keySet()) {
			payloadTypeMap.put(payloadStringMap.get(mnemonic), mnemonic);
		}

	}

	/**
	 * gets the sample rate for given sample tag
	 * 
	 * @param type the type tag to retrieve sample rate for
	 * @return the sample rate in Hz, null if the type is not a signal type
	 */
	public static Double getSignalSamplerate(TypeTag type) {
		return sampleRateMap.get(type);
	}

	/**
	 * Used to determine if the TypeTag is a data signal
	 * 
	 * @param type
	 * @return true if this is a signal type false if not
	 */
	public static Boolean isSignalType(TypeTag type) {
		return (getSignalSamplerate(type) != null);
	}

	/**
	 * Return the String corresponding to the type tag
	 * 
	 * @param tag the type tag to get
	 * @return will return null if tag is unknown
	 */
	public static String TTS(TypeTag tag) {
		return typeTagTypeMap.get(tag);
	}

	/**
	 * Return the tag enumeration given the string representation
	 * 
	 * @param tag the string mnemonic of the tag
	 * @return will return null if not found otherwise the typetag is returned
	 */
	public static TypeTag STT(String tag) {
		return typeTagStringMap.get(tag);
	}

	/**
	 * Return the String corresponding to the payload label tag
	 * 
	 * @param tag the type tag to get
	 * @return will return null if tag is unknown
	 */
	public static String PTS(PayloadLabel tag) {
		return payloadTypeMap.get(tag);
	}

	/**
	 * Return the tag enumeration given the string representation
	 * 
	 * @param tag the string mnemonic of the tag
	 * @return will return null if not found otherwise the payload label is returned
	 */
	public static PayloadLabel STP(String tag) {
		return payloadStringMap.get(tag);
	}

	public static final char PACKET_DELIMITER_CSV = '\n';
	public static final int EMOTIBIT_ADVERTISEMENT_PORT = 3131;

//const char* EmotiBitPacket::TypeTag::HELLO_EMOTIBIT = "HE\0";
//const char* EmotiBitPacket::TypeTag:: = "HH\0";
//const char* EmotiBitPacket::TypeTag:: = "EC\0";
//
//const char* EmotiBitPacket::PayloadLabel::CONTROL_PORT = "CP\0";
//const char* EmotiBitPacket::PayloadLabel::DATA_PORT = "DP\0";
//const char* EmotiBitPacket::PayloadLabel::RECORDING_STATUS = "RS\0";
//const char* EmotiBitPacket::PayloadLabel::POWER_STATUS = "PS\0";
//
//const char EmotiBitPacket::PACKET_DELIMITER_CSV = '\n';
//    

// EmotiBit Data TagTypes
//const char* EmotiBitPacket::TypeTag:: = "EA\0";
//const char* EmotiBitPacket::TypeTag:: = "EL\0";
//const char* EmotiBitPacket::TypeTag::EDR = "ER\0";
//const char* EmotiBitPacket::TypeTag::PPG_INFRARED = "PI\0";
//const char* EmotiBitPacket::TypeTag::PPG_RED = "PR\0";
//const char* EmotiBitPacket::TypeTag::PPG_GREEN = "PG\0";
//const char* EmotiBitPacket::TypeTag::SPO2 = "O2\0";
//const char* EmotiBitPacket::TypeTag::TEMPERATURE_0 = "T0\0";
//const char* EmotiBitPacket::TypeTag::TEMPERATURE_1 = "T1\0";
//const char* EmotiBitPacket::TypeTag::THERMOPILE = "TH\0";
//const char* EmotiBitPacket::TypeTag::HUMIDITY_0 = "H0\0";
//const char* EmotiBitPacket::TypeTag::ACCELEROMETER_X = "AX\0";
//const char* EmotiBitPacket::TypeTag::ACCELEROMETER_Y = "AY\0";
//const char* EmotiBitPacket::TypeTag::ACCELEROMETER_Z = "AZ\0";
//const char* EmotiBitPacket::TypeTag::GYROSCOPE_X = "GX\0";
//const char* EmotiBitPacket::TypeTag::GYROSCOPE_Y = "GY\0";
//const char* EmotiBitPacket::TypeTag::GYROSCOPE_Z = "GZ\0";
//const char* EmotiBitPacket::TypeTag::MAGNETOMETER_X = "MX\0";
//const char* EmotiBitPacket::TypeTag::MAGNETOMETER_Y = "MY\0";
//const char* EmotiBitPacket::TypeTag::MAGNETOMETER_Z = "MZ\0";
//const char* EmotiBitPacket::TypeTag::BATTERY_VOLTAGE = "BV\0";
//const char* EmotiBitPacket::TypeTag::BATTERY_PERCENT = "B%\0";
//const char* EmotiBitPacket::TypeTag::DATA_CLIPPING = "DC\0";
//const char* EmotiBitPacket::TypeTag::DATA_OVERFLOW = "DO\0";
//const char* EmotiBitPacket::TypeTag::SD_CARD_PERCENT = "SD\0";
//const char* EmotiBitPacket::TypeTag::RESET = "RS\0"; // still necessary?
//const char* EmotiBitPacket::TypeTag::EMOTIBIT_DEBUG = "DB\0";
//const char* EmotiBitPacket::TypeTag::ACK = "AK\0";
//const char* EmotiBitPacket::TypeTag::REQUEST_DATA = "RD\0";
//const char* EmotiBitPacket::TypeTag::TIMESTAMP_LOCAL = "TL\0";
//const char* EmotiBitPacket::TypeTag::TIMESTAMP_UTC = "TU\0";
//const char* EmotiBitPacket::TypeTag::TIMESTAMP_CROSS_TIME = "TX\0";
//const char* EmotiBitPacket::TypeTag::EMOTIBIT_MODE = "EM\0";
//const char* EmotiBitPacket::TypeTag::EMOTIBIT_INFO = "EI\0";
//// Computer data TypeTags (sent over reliable channel e.g. Control)
//const char* EmotiBitPacket::TypeTag::GPS_LATLNG = "GL\0";
//const char* EmotiBitPacket::TypeTag::GPS_SPEED = "GS\0";
//const char* EmotiBitPacket::TypeTag::GPS_BEARING = "GB\0";
//const char* EmotiBitPacket::TypeTag::GPS_ALTITUDE = "GA\0";
//const char* EmotiBitPacket::TypeTag::USER_NOTE = "UN\0";
//const char* EmotiBitPacket::TypeTag::LSL_MARKER = "LM\0";
//// Control TypeTags
//const char* EmotiBitPacket::TypeTag::RECORD_BEGIN = "RB\0";
//const char* EmotiBitPacket::TypeTag::RECORD_END = "RE\0";
//const char* EmotiBitPacket::TypeTag::MODE_NORMAL_POWER = "MN\0";				// Stops sending data timestamping should be accurate
//const char* EmotiBitPacket::TypeTag::MODE_LOW_POWER = "ML\0";				// Stops sending data timestamping should be accurate
//const char* EmotiBitPacket::TypeTag::MODE_MAX_LOW_POWER = "MM\0";		// Stops sending data timestamping accuracy drops
//const char* EmotiBitPacket::TypeTag::MODE_WIRELESS_OFF = "MO\0";				// Stops sending data timestamping should be accurate
//const char* EmotiBitPacket::TypeTag::MODE_HIBERNATE = "MH\0";				// Full shutdown of all operation
//const char* EmotiBitPacket::TypeTag::EMOTIBIT_DISCONNECT = "ED\0";
//// Advertising TypeTags
//const char* EmotiBitPacket::TypeTag::PING = "PN\0";
//const char* EmotiBitPacket::TypeTag::PONG = "PO\0";
//const char* EmotiBitPacket::TypeTag::HELLO_EMOTIBIT = "HE\0";
//const char* EmotiBitPacket::TypeTag::HELLO_HOST = "HH\0";
//const char* EmotiBitPacket::TypeTag::EMOTIBIT_CONNECT = "EC\0";
//
//const char* EmotiBitPacket::PayloadLabel::CONTROL_PORT = "CP\0";
//const char* EmotiBitPacket::PayloadLabel::DATA_PORT = "DP\0";
//const char* EmotiBitPacket::PayloadLabel::RECORDING_STATUS = "RS\0";
//const char* EmotiBitPacket::PayloadLabel::POWER_STATUS = "PS\0";
//
//const char EmotiBitPacket::PACKET_DELIMITER_CSV = '\n';
//

	public static class Header {

		public long timeStamp;
		public int packetNumber;
		public int dataLength;
		public String typeTag;
		public int protocolversion;
		public int dataReliability;
		private ArrayList<String> extraData = new ArrayList<String>();

		public Header() {
		}

		public Header(String typeTag, long timestamp, int packetNumber, int dataLength, int protocolVersion,
				int dataReliability) {

			this.typeTag = typeTag;
			this.timeStamp = timestamp;
			this.packetNumber = packetNumber;
			this.dataLength = dataLength;
			this.protocolversion = protocolVersion;
			this.dataReliability = dataReliability;
		}

		public List<String> getDataStrings() {
			return extraData;
		}

		public String toString() {
			String headerString = "";

			headerString += String.valueOf(timeStamp);
			headerString += ",";
			headerString += String.valueOf(packetNumber);
			headerString += ",";
			headerString += String.valueOf(dataLength);
			headerString += ",";
			headerString += String.valueOf(typeTag);
			headerString += ",";
			headerString += String.valueOf(protocolversion);
			headerString += ",";
			headerString += String.valueOf(dataReliability);
			return headerString;
		}

		public static Header parseHeader(String incoming) {
			String[] values = incoming.split(",");

			if (values.length < 6) {
				return null;
			}

			String tag = values[3];
			Long ts = Long.parseLong(values[0]);
			Integer pn = Integer.parseInt(values[1]);
			Integer dl = Integer.parseInt(values[2]);
			Integer pv = Integer.parseInt(values[4]);
			Integer dr = Integer.parseInt(values[5]);

			Header h = new Header(tag, ts, pn, dl, pv, dr);

			// add the packet data beyond the header
			if (values.length > 6) {
				List<String> packetData = h.getDataStrings();

				for (int i = 6; i < values.length; i++) {
					packetData.add(values[i]);
				}
			}

			return h;
		}

		public static Map<String, String> getPayload(String incoming) {
			HashMap<String, String> map = new HashMap<String, String>();
			String[] values = incoming.split(",");

			if (values.length <= 6) {
				return map;
			} else {
				for (int i = 6; i < values.length; i = i + 2) {
					map.put(values[i].trim(), values[i + 1].trim());
				}
			}

			return map;
		}

	}

	public static String createPacket(String ptype, int counter, ArrayList<String> payload) {

		Header h;
		h = new Header(ptype, System.currentTimeMillis(), counter, payload.size(), 1, 100);
		String pkt = h.toString();
		for (String v : payload) {
			pkt += "," + v;
		}

		pkt += String.valueOf(PACKET_DELIMITER_CSV);

		return pkt;

	}

	public Header createEmptyHeader() {

		return new Header();

	}

}

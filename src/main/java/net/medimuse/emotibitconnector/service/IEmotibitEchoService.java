package net.medimuse.emotibitconnector.service;

import net.medimuse.emotibitconnector.EmotibitEcho;

public interface IEmotibitEchoService {
	
	public EmotibitEcho getEcho();
	public void connectEmotibit(String address);
	public void disconnectEmotibit();

}

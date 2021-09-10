package net.medimuse.emotibitconnector;

import java.net.InetAddress;
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
public interface EmotibitEchoListener {
    public void emotibitDiscovered(InetAddress address);
    public void dataBlock(Map<Emotibit.TypeTag, Map<Long,Double>> data );
}

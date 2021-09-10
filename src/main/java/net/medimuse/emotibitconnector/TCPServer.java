package net.medimuse.emotibitconnector;


import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
public class TCPServer extends Thread {

    private ServerSocket mySocket = null;
    private Socket sock;
    private final EmotibitEcho myParent;
    private InputStream sockInput;
    private OutputStream sockOutput;
    private InetAddress connectedDeviceAddress = null;
    
    private ControlState currentState = ControlState.INITIALIZING; 
    
    public static enum ControlState {
        INITIALIZING,
        WAITING,
        CONNECTED,
        DISCONNECTING
    }
    
    public void changeState(ControlState newState) {
        currentState = newState;
        switch (currentState) {
            case CONNECTED:
                System.out.println("CONTROL : CONNECTED");
                break;
            case DISCONNECTING:
                System.out.println("CONTROL : DISCONNECTING");
                if (sock.isConnected()) {
                    try {
                        sock.close();
                    } catch (IOException ex) {
                        Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case INITIALIZING:
                System.out.println("CONTROL : INITIALIZING");
                break;
            case WAITING:
                System.out.println("CONTROL : WAITING");
                break;
            default:
                break;
        }
    }
  
    
    public TCPServer(int port, EmotibitEcho parent) {
        myParent = parent;
        changeState(ControlState.INITIALIZING);
        try {
            mySocket = new ServerSocket(port);
            
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public InetAddress getConnectedDeviceAddress() {
        return connectedDeviceAddress;
    }
    
    public void stopServer() {
        try {
            this.interrupt();
        } catch (Exception e) {
            
        }
        if (mySocket != null && !mySocket.isClosed() ) {
        
            try {
                mySocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            
        
    }
    
    public void write(String data) {
        if (mySocket != null && ! mySocket.isClosed()) {
          PrintWriter writer = new PrintWriter(sockOutput, true);
          writer.print(data);
        }
    }
    
    @Override
    public void run() {
        if (mySocket != null) {
            while (true) {
                try {
        
                    changeState(ControlState.WAITING);
                    try {
                        sock = mySocket.accept();
                    } catch (IOException ex) {
                        //Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (mySocket.isClosed()) {
                        break;
                    }
                    sock.setKeepAlive(true);
                    
                    connectedDeviceAddress = sock.getInetAddress();
                    // send the conected message to our parent
                    myParent.sendStateMessage(EmotibitEcho.StateChangeMessage.STATUS_CONNECTED);

                    try {
                        sockInput = sock.getInputStream();
                        sockOutput = sock.getOutputStream();
                    } catch (IOException ex) {
                        Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    changeState(ControlState.CONNECTED);
                    
                    byte[] bytes = new byte[1024];
                    try {
                        int read;
                        while ((read = sockInput.read(bytes)) != -1 && currentState == ControlState.CONNECTED) { // blocking
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(TCPServer.class.getName()).log(Level.INFO, "Control Connection was terminated", "");
                    }
                    sock.close();
                    connectedDeviceAddress = null;
                    // send the disconnected message to our parent
                    myParent.changeState(EmotibitEcho.MachineState.READY);

                    
                } catch (IOException ex) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }
    }

}

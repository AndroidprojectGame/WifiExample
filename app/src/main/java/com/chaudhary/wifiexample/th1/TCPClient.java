package com.chaudhary.wifiexample.th1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    public static final String SERVER_IP = "192.168.4.1"; //your computer IP address
    public static final int SERVER_PORT = 15002;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    Context context;


    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(Context context) {
        //mMessageListener = listener;
        this.context=context;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    mBufferOut.println(message);
                    mBufferOut.write(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread=new Thread(runnable);
        thread.start();

    }

    public void stopClient() {
        mRun = false;
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {
        mRun = true;
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.e("TCP Client", "C: Connecting...");
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            try {
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = "";
                int charsRead = 0;
                char[] buffer = new char[2048];
                while ((charsRead = mBufferIn.read(buffer)) != -1) {
                    // message += new String(buffer).substring(0, charsRead);
                    message = new String(buffer).substring(0, charsRead);
                    //mMessageListener.messageReceived(message);
                    Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + message + "'");
                    Intent i = new Intent("broadCastName");
                    i.putExtra("message", message);
                    context.sendBroadcast(i);
                }
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }


    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
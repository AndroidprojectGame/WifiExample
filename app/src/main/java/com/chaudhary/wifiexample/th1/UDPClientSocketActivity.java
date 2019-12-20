package com.chaudhary.wifiexample.th1;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaudhary.wifiexample.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientSocketActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextViewReplyFromServer;
    private EditText mEditTextSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udpclient_socket);
        Button buttonSend = (Button) findViewById(R.id.btn_send);
        mEditTextSendMessage = (EditText) findViewById(R.id.edt_send_message);
        mTextViewReplyFromServer = (TextView) findViewById(R.id.tv_reply_from_server);
        buttonSend.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                sendMessage(mEditTextSendMessage.getText().toString());
                break;
        }
    }

    public void sendMessage(final String message) {
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            String stringData;
            @Override
            public void run() {
                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket();
                    // IP Address below is the IP address of that Device where server socket is opened.
                    InetAddress serverAddr = InetAddress.getByName("192.168.225.66");
                    DatagramPacket dp;
                    dp = new DatagramPacket(message.getBytes(), message.length(), serverAddr, 4445);
                    ds.send(dp);
                    byte[] lMsg = new byte[1000];
                    dp = new DatagramPacket(lMsg, lMsg.length);
                    ds.receive(dp);
                    stringData = new String(lMsg, 0, dp.getLength());

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String s = mTextViewReplyFromServer.getText().toString();
                        if (stringData.trim().length() != 0)
                            mTextViewReplyFromServer.setText(s + "\nFrom Server : " + stringData);
                    }
                });
            }
        });
        thread.start();
    }
}

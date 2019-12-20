package com.chaudhary.wifiexample.th1;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaudhary.wifiexample.MainActivity;
import com.chaudhary.wifiexample.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThirdActivity extends AppCompatActivity {

    WifiManager mainWifiObj;
    WifiScanReceiver wifiReciever;

    ListView list,dataList;
    String wifis[];
    EditText pass;

    private TCPClient mTcpClient;
    public static final int SERVERPORT = 15002;
    public static final String SERVER_IP = "192.168.4.1";


    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;




    public String   s_dns1 ;
    public String   s_dns2;
    public String   s_gateway;
    public String   s_ipAddress;
    public String   s_leaseDuration;
    public String   s_netmask;
    public String   s_serverAddress;
    DhcpInfo d;
    WifiManager wifii;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        arrayList = new ArrayList<String>();

        list = (ListView) findViewById(R.id.list);
        dataList= (ListView) findViewById(R.id.dataList);
        mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        mainWifiObj.startScan();

        // listening to single list item on click
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String ssid = ((TextView) view).getText().toString();
                connectToWifi(ssid);
                Toast.makeText(ThirdActivity.this, "Wifi SSID : " + ssid, Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onPause() {
        unregisterReceiver(wifiReciever);
        unregisterReceiver(broadcastReceiver);
        //mTcpClient.stopClient();
        //mTcpClient = null;
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));
        super.onResume();
    }

    public void connect(View view) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Not Conneted", Toast.LENGTH_SHORT).show();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        new ConnectTask().execute("");
        list.setVisibility(View.GONE);
        dataList.setVisibility(View.VISIBLE);
        //startActivity(new Intent(ThirdActivity.this, DatActivity.class));
}

    public void sendMessage(View view) {
        mTcpClient.sendMessage("$lum_jio_8450|pass@8450#");    }

    public void sendUdpConnection(View view) {
        try {
            client();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            wifis = new String[wifiScanList.size()];
            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = ((wifiScanList.get(i)).toString());
            }
            String filtered[] = new String[wifiScanList.size()];
            int counter = 0;
            for (String eachWifi : wifis) {
                String[] temp = eachWifi.split(",");
                filtered[counter] = temp[0].substring(5).trim();//+"\n" + temp[2].substring(12).trim()+"\n" +temp[3].substring(6).trim();//0->SSID, 2->Key Management 3-> Strength
                counter++;

            }
            list.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.label, filtered));
        }
    }




    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = (TextView) dialog.findViewById(R.id.textSSID1);

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);
        Button dialogcanButton = (Button) dialog.findViewById(R.id.cancleButton);
        pass = (EditText) dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiSSID);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPassword = pass.getText().toString();
                //finallyConnect(checkPassword, wifiSSID);
                connect(wifiSSID, checkPassword);
                dialog.dismiss();
            }
        });

        dialogcanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void connect(String networkSSID, String networkPass) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\"" + networkPass + "\"";
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
            }
        }
    }



    public class ConnectTask extends AsyncTask<String, String, TCPClient> {
        @Override
        protected TCPClient doInBackground(String... message) {
            mTcpClient=new TCPClient(ThirdActivity.this);
            mTcpClient.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
           // arrayList.add(values[0]);
            //mAdapter.notifyDataSetChanged();
        }
    }




    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String message = b.getString("message");
            Log.e("newmesage", "" + message);
            mAdapter = new MyCustomAdapter(ThirdActivity.this, arrayList);
            dataList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            arrayList.add(message);
        }
    };



    public void client() throws IOException{
        try {
            //Create a DatagramSocket object, and designate the port monitor
            DatagramSocket socket = new DatagramSocket(4445);
            //Create a InetAddress
            InetAddress serverAddress = InetAddress.getByName("192.168.225.66");
            String str = "&MIP>192.168.225.99#";
            byte data[] = str.getBytes();
            //Create a DatagramPacket object, and specify the data packets to the network and port
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 4445);
            socket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

//        String message="hello";
//        DatagramSocket serverSocket = new DatagramSocket();
//        InetAddress IPAddress = InetAddress.getByName("192.168.225.66");
//        byte[] sendData = message.getBytes();
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4445);
//        serverSocket.send(sendPacket);
//
//        byte[] receiveData = new byte[15]; //max length 15.
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        DatagramSocket clientSocket = new DatagramSocket(4445);
//        clientSocket.receive(receivePacket);
//        String receivedMessage = new String(receivePacket.getData()).trim();

//        String messageStr="Hello Android!";
//        int server_port = 4445;
//        DatagramSocket s = new DatagramSocket();
//        InetAddress local = InetAddress.getByName("192.168.225.66");
//        int msg_length=messageStr.length();
//        byte[] message = messageStr.getBytes();
//        DatagramPacket p = new DatagramPacket(message, msg_length,local,server_port);
//        s.send(p);
//
//
//        String text;
//        int server_port1 = 4445;
//        byte[] message1 = new byte[1500];
//        DatagramPacket p1 = new DatagramPacket(message1, message1.length);
//        DatagramSocket s1 = new DatagramSocket(server_port1);
//        s1.receive(p1);
//        text = new String(message1, 0, p1.getLength());
//        Log.d("Udp tutorial","message:" + text);
//        s1.close();
//        System.out.println("=====recive medssage>>>>>>>>>"+text);
    }



    public void checkNetwork(View view) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        wifii = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        d = wifii.getDhcpInfo();
        s_dns1 = "DNS 1: " + String.valueOf(d.dns1);
        s_dns2 = "DNS 2: " + String.valueOf(d.dns2);
        s_gateway = "Default Gateway: " + String.valueOf(d.gateway);
        s_ipAddress = "IP Address: " + String.valueOf(d.ipAddress);
        s_leaseDuration = "Lease Time: " + String.valueOf(d.leaseDuration);
        s_netmask = "Subnet Mask: " + String.valueOf(d.netmask);
        s_serverAddress = "Server IP: " + String.valueOf(d.serverAddress);
        Log.v("s_dns1",s_dns1);
        Log.e("s_dns2",s_dns2);
        Log.e("s_gateway",s_gateway);
        Log.e("s_ipAddress",s_ipAddress);
        Log.e("s_leaseDuration",s_leaseDuration);
        Log.e("s_netmask",s_netmask);
        Log.e("s_serverAddress",s_serverAddress);
        String connections = "";
        InetAddress host;
        try
        {
            host = InetAddress.getByName(intToIp(d.dns1));
            byte[] ip = host.getAddress();

            for(int i = 1; i <= 254; i++)
            {
                ip[3] = (byte) i;
                InetAddress address = InetAddress.getByAddress(ip);
                if(address.isReachable(100))
                {
                    System.out.println("======isReachable=>>>>>>"+address + "  111111  "+address.getCanonicalHostName());
                    connections+= address+"\n";
                }
                else if(!address.getHostAddress().equals(address.getHostName()))
                {
                    System.out.println("======isNotReachable=>>>>>>"+address + " machine is known in a DNS lookup");
                }
            }
        }
        catch(UnknownHostException e1)
        {
            e1.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        System.out.println(connections);
    }


    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }
}

package com.chaudhary.wifiexample;

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
import android.os.StrictMode;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaudhary.wifiexample.th1.DatActivity;
import com.chaudhary.wifiexample.th1.ThirdActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {


    Context context;



    public String getWifiIPAddress() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return  Formatter.formatIpAddress(ip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        WifiInfo connectionInfo = wm.getConnectionInfo();
        int ipAddress = connectionInfo.getIpAddress();
        String ipString = Formatter.formatIpAddress(ipAddress);
        context=this;

        System.out.println("=====ip===="+ipString+"====name="+connectionInfo.getSSID());
        new NetworkSniffTask(this).execute();
    }

    public void connectWithWifi(View view) {
        startActivity(new Intent(this, ThirdActivity.class));
    }

    public void createSocket(View view) {
        startActivity(new Intent(this, DatActivity.class));
    }


    static class NetworkSniffTask extends AsyncTask<Void, Void, Void> {
        //private static final String TAG = SyncStateContract.Constants.TAG + "nstask";
        private WeakReference<Context> mContextRef;

        public NetworkSniffTask(Context context) {
            mContextRef = new WeakReference<Context>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("Tag", "Let's sniff the network");

            try {
                Context context = mContextRef.get();
                if (context != null) {

                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo connectionInfo = wm.getConnectionInfo();
                    int ipAddress = connectionInfo.getIpAddress();
                    String ipString = Formatter.formatIpAddress(ipAddress);


                    Log.d("Tag", "activeNetwork: " + String.valueOf(activeNetwork));
                    Log.d("Tag", "ipString: " + String.valueOf(ipString));

                    String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                    Log.d("Tag", "prefix: " + prefix);

                    for (int i = 0; i < 255; i++) {
                        String testIp = prefix + String.valueOf(i);
                        InetAddress address = InetAddress.getByName(testIp);
                        boolean reachable = address.isReachable(1000);
                        String hostName = address.getCanonicalHostName();
                        if (reachable)
                            Log.i("Tag", "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIp) + ") is reachable!");
                    }
                }
            } catch (Throwable t) {
                Log.e("Tag", "Well that's not good.", t);
            }
            return null;
        }
    }

    public String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);
    }

}

package com.chaudhary.wifiexample.th1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.chaudhary.wifiexample.R;

import java.util.ArrayList;

public class DatActivity extends AppCompatActivity {


    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;

    private TCPClient mTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat);
        mList=(ListView)findViewById(R.id.listview);
        arrayList = new ArrayList<String>();
        mTcpClient=new TCPClient(DatActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String message = b.getString("message");
            Log.e("data", "" + message);
            mAdapter = new MyCustomAdapter(DatActivity.this, arrayList);
            mList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            arrayList.add(message);
        }
    };


    public void sendMessage(View view) {
        mTcpClient.sendMessage("$lum_jio_2440|pass@2440#");
        //startActivity(new Intent(this,UDPClientSocketActivity.class));
    }
}

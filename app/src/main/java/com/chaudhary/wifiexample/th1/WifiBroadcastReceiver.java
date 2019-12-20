package com.chaudhary.wifiexample.th1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        String action = intent.getAction();
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION .equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (SupplicantState.isValidState(state) && state == SupplicantState.COMPLETED) {
                boolean connected = checkConnectedToDesiredWifi();
                System.out.println("======connection===sucess====="+connected);
            }
        }
    }

    /** Detect you are connected to a specific network. */
    private boolean checkConnectedToDesiredWifi() {
        boolean connected = false;
        String desiredMacAddress = "router mac address";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null) {
            // get current router Mac address
            String bssid = wifi.getBSSID();
            connected = desiredMacAddress.equals(bssid);
        }
        return connected;
    }
}

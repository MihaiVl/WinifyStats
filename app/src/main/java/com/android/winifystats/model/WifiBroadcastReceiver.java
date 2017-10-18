package com.android.winifystats.model;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.winifystats.MainActivity;

/**
 * Created by izaya_orihara on 7/19/17.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {


    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            if (SupplicantState.isValidState(state)
                    && state == SupplicantState.COMPLETED && checkConnectedToDesiredWifi()) {
                Log.v("WifiBroadcastReceiver", "Wifi connected");


            }

        }

    }

    private boolean checkConnectedToDesiredWifi() {
        boolean connected = false;

        String name = "Winify";

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();

        if (wifi != null) {
            String wifiName = wifi.getSSID();
            connected = name.equals(wifiName);
        }
        return connected;
    }
}

package com.grimaldos.ftbsports;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckConnection {
    public static boolean checkConnection(ConnectivityManager conMgr) {
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null)
            return false;
        if (!netInfo.isConnected())
            return false;
        if (!netInfo.isAvailable())
            return false;
        return true;
    }
}
package com.news.browser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.news.browser.base.BaseApplication;

/**
 * Created by zy1584 on 2017-8-8.
 */

public class NetChangeReceiver extends BroadcastReceiver {

    private static final int WHAT_UPDATE = 1;// 自升级检查

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isCon = wifiNetInfo.isConnected();
        if (isCon){// 自升级检测
            BaseApplication.startCheckAppUpdate(context, false);
        }
    }

}

package com.news.browser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.news.browser.base.BaseApplication;
import com.news.browser.utils.NetUtils;

/**
 * Created by zy1584 on 2017-8-8.
 */

public class NetChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean connected = NetUtils.isConnected(context);
        if (connected) {
            BaseApplication.startCheckAppUpdate(context, false);
        }
    }

}

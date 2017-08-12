package com.news.browser.http.subscriber;

import android.content.Context;

import com.news.browser.base.BaseSubscriber;
import com.news.browser.http.exception.ApiException;
import com.news.browser.utils.NetUtils;
import com.orhanobut.logger.Logger;


/**
 * Created by tzqiang on 2016/11/6.
 */

public abstract class CommonSubscriber<T> extends BaseSubscriber<T> {

    private Context context;

    public CommonSubscriber(Context context) {
        this.context = context;
    }

    private static final String TAG = "CommonSubscriber";

    @Override
    public void onStart() {

        if (!NetUtils.isConnected(context)) {
            Logger.t(TAG).e("网络不可用");
        } else {
            Logger.t(TAG).e("网络可用");
        }
    }



    @Override
    protected void onError(ApiException e) {
        Logger.t(TAG).e(e, "错误信息为 " + "code:" + e.getMessage());
    }

    @Override
    public void onCompleted() {
        Logger.t(TAG).e("成功了");
    }

}

package com.news.browser.base;


import android.text.TextUtils;

import com.news.browser.http.Http;
import com.news.browser.http.HttpService;
import com.news.browser.mvp.IModel;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.SPUtils;

public class BaseModel implements IModel {
    protected static HttpService httpService;

    //初始化httpService
    static {
        httpService = Http.getHttpService();
    }

    protected String getUrl(String sub) {
        String newIp = (String) SPUtils.get(GlobalParams.IP, GlobalParams.HOLDER_HOST);
        String newPort = (String) SPUtils.get(GlobalParams.PORT, GlobalParams.HOLDER_PORT + "");
        if (TextUtils.isEmpty(newIp)){
            newIp = GlobalParams.HOLDER_HOST;
        }
        if (TextUtils.isEmpty(newPort)){
            newPort = GlobalParams.HOLDER_PORT + "";
        }
        return "http://" + newIp + ":" + newPort + "/" + sub;
    }

}

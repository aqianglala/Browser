package com.example.zy1584.mybase.base;


import com.example.zy1584.mybase.http.Http;
import com.example.zy1584.mybase.http.HttpService;
import com.example.zy1584.mybase.mvp.IModel;
import com.example.zy1584.mybase.utils.GlobalParams;
import com.example.zy1584.mybase.utils.SPUtils;

public class BaseModel implements IModel {
    protected static HttpService httpService;

    //初始化httpService
    static {
        httpService = Http.getHttpService();
    }

    protected String getUrl(String sub) {
        String newIp = (String) SPUtils.get(GlobalParams.IP, GlobalParams.HOLDER_HOST);
        String newPort = (String) SPUtils.get(GlobalParams.PORT, GlobalParams.HOLDER_PORT + "");
        return "http://" + newIp + ":" + newPort + "/" + sub;
    }

}

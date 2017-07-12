package com.example.zy1584.mybase.base;


import com.example.zy1584.mybase.http.Http;
import com.example.zy1584.mybase.http.HttpService;
import com.example.zy1584.mybase.mvp.IModel;

public class BaseModel implements IModel {
    protected static HttpService httpService;

    //初始化httpService
    static {
        httpService = Http.getHttpService();
    }

}

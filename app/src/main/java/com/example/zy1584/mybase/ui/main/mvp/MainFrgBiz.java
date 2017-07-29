package com.example.zy1584.mybase.ui.main.mvp;

import com.example.zy1584.mybase.base.BaseModel;

import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class MainFrgBiz extends BaseModel {

    public Observable<ResponseBody> getChannelList(Map<String, String> params){
        return httpService.getChannelList(params);
    }
}

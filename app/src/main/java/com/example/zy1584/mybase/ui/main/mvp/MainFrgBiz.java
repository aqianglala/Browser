package com.example.zy1584.mybase.ui.main.mvp;

import com.example.zy1584.mybase.base.BaseModel;
import com.example.zy1584.mybase.utils.GlobalParams;

import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class MainFrgBiz extends BaseModel {

    public Observable<ResponseBody> getChannelList(Map<String, String> params){
        String url = getUrl(GlobalParams.CHANNEL);
        return httpService.getChannelList(url, params);
    }
}

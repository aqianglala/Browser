package com.example.zy1584.mybase.ui.news.mvp.channel;

import com.example.zy1584.mybase.base.BaseModel;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;

import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsChannelBiz extends BaseModel {

    public Observable<NewsChannelBean> getNewsChannelList(Map<String, String> params){
        return httpService.getChannelNewsList(params);
    }

    public Observable<ResponseBody> reportActionType(Map<String, String> params){
        return httpService.reportActionType(params);
    }
}

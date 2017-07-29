package com.example.zy1584.mybase.ui.news.mvp.recommend;

import com.example.zy1584.mybase.base.BaseModel;
import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.ui.news.RecommendBean;

import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsRecommendBiz extends BaseModel {

    public Observable<RecommendBean> getNewsRecommendList(Map<String, String> params){
        return httpService.getRecommendNewsList(params);
    }

    public Observable<ADResponseBean> getADList(Map<String, String> params){
        return httpService.getADList(params);
    }

    public Observable<ClickLinkResponseBean> reportClick(String url){
        return httpService.reportClick(url);
    }

    public Observable<ResponseBody> reportADExposed(String url){
        return httpService.reportADExposed(url);
    }
}

package com.example.zy1584.mybase.ui.news.mvp.recommend;

import com.example.zy1584.mybase.base.BaseModel;
import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.bean.RecommendBean;
import com.example.zy1584.mybase.utils.GlobalParams;

import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsRecommendBiz extends BaseModel {

    public Observable<RecommendBean> getNewsRecommendList(Map<String, String> params){
        String url = getUrl(GlobalParams.RECOMMEND);
        return httpService.getRecommendNewsList(url, params);
    }

    public Observable<ADResponseBean> getADList(Map<String, String> params){
        String url = getUrl(GlobalParams.ADVERTISING);
        return httpService.getADList(url, params);
    }

    public Observable<ClickLinkResponseBean> reportClick(String url){
        return httpService.reportClick(url);
    }

    public Observable<ResponseBody> reportADExposed(String url){
        return httpService.reportADExposed(url);
    }
}

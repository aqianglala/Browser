package com.news.browser.ui.main.mvp;

import com.news.browser.base.BaseModel;
import com.news.browser.bean.HomeNavigationBean;
import com.news.browser.utils.GlobalParams;

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

    public Observable<HomeNavigationBean> getHomeNavigationList(Map<String, String> params){
        String url = getUrl(GlobalParams.NAVIGATION);
        return httpService.getHomeNavigationList(url, params);
    }
}

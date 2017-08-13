package com.news.browser.ui.hotSite.mvp;


import com.news.browser.base.BaseModel;
import com.news.browser.bean.HotSiteBean;
import com.news.browser.utils.GlobalParams;

import java.util.Map;

import rx.Observable;


/**
 * Created by zy1584 on 2017-3-30.
 */

public class HotSiteBiz extends BaseModel {

    public Observable<HotSiteBean> getHotSite(Map<String, String> params){
        String url = getUrl(GlobalParams.HOT_SITE);
        return httpService.getHotSite(url, params);
    }
}

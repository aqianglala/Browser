package com.news.browser.ui.main.mvp;

import com.news.browser.base.BaseModel;
import com.news.browser.bean.EngineBean;
import com.news.browser.utils.GlobalParams;

import java.util.Map;

import rx.Observable;

/**
 * Created by zy1584 on 2017-8-8.
 */

public class BrowserActBiz extends BaseModel {

    public Observable<EngineBean> getSearchEngine(Map<String, String> params){
        String url = getUrl(GlobalParams.SEARCH_ENGINE);
        return httpService.getSearchEngine(url, params);
    }
}

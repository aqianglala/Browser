package com.example.zy1584.mybase.ui.main.mvp;

import com.example.zy1584.mybase.base.BaseModel;
import com.example.zy1584.mybase.bean.EngineBean;
import com.example.zy1584.mybase.utils.GlobalParams;

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

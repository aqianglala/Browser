package com.news.browser.ui.navigation.mvp;

import com.news.browser.bean.HotTagBean;
import com.news.browser.base.BaseModel;
import com.news.browser.utils.GlobalParams;

import java.util.Map;

import rx.Observable;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class HotBookmarkBiz extends BaseModel {

    public Observable<HotTagBean> getHotTags(Map<String, String> params){
        String url = getUrl(GlobalParams.HOME_TAG);
        return httpService.getHotBookmarkList(url, params);
    }
}

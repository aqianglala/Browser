package com.example.zy1584.mybase.ui.navigation.mvp;

import com.example.zy1584.mybase.base.BaseModel;
import com.example.zy1584.mybase.bean.HotTagBean;
import com.example.zy1584.mybase.utils.GlobalParams;

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

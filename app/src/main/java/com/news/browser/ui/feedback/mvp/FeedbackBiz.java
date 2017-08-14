package com.news.browser.ui.feedback.mvp;


import com.news.browser.base.BaseModel;
import com.news.browser.bean.FeedbackTypeBean;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;


/**
 * Created by zy1584 on 2017-3-30.
 */

public class FeedbackBiz extends BaseModel {

    public Observable<FeedbackTypeBean> getFeedbackType(Map<String, String> params){
        return httpService.getFeedbackType(params);
    }

    public Observable<ResponseBody> sendFeedback(RequestBody Body){
        return httpService.sendFeedback(Body);
    }
}

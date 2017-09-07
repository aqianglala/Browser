package com.news.browser.http;


import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.bean.EngineBean;
import com.news.browser.bean.FeedbackTypeBean;
import com.news.browser.bean.HomeNavigationBean;
import com.news.browser.bean.HotSiteBean;
import com.news.browser.bean.HotTagBean;
import com.news.browser.bean.RecommendBean;
import com.news.browser.bean.UpgradeBean;
import com.news.browser.ui.news.bean.NewsChannelBean;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by zy1584 on 2017-3-27.
 */

public interface HttpService {

//    @GET("http://openapi.inews.qq.com/getQQNewsUnreadList")
    @GET
    Observable<RecommendBean> getRecommendNewsList(@Url String url, @QueryMap Map<String, String> params);

//    @GET("http://op.inews.qq.com/mcms/h5/info/navigation")
    @GET
    Observable<ResponseBody> getChannelList(@Url String url, @QueryMap Map<String, String> params);

//    @GET("http://op.inews.qq.com/mcms/h5/info/channel_data")
    @GET
    Observable<NewsChannelBean> getChannelNewsList(@Url String url, @QueryMap Map<String, String> params);

    @FormUrlEncoded
    @POST("http://openapi.kuaibao.qq.com/reportActionType")
    Observable<ResponseBody> reportActionType(@FieldMap Map<String, String> params);

//    @GET("http://mi.gdt.qq.com/api/v3")
    @GET
    Observable<ResponseBody> getADList(@Url String url, @QueryMap Map<String, String> params);

    @GET
    Observable<ClickLinkResponseBean> reportClick(@Url String url);

    @GET
    Observable<ResponseBody> reportConversion(@Url String url);

    @GET
    Observable<ResponseBody> reportADExposed(@Url String url);

    @GET("http://ha.doov.com.cn:9066/address/query")
    Call<ResponseBody> requestServerAddress(@QueryMap Map<String, String> params);

    @GET
    Observable<HomeNavigationBean> getHomeNavigationList(@Url String url, @QueryMap Map<String, String> params);

    @GET
    Observable<HotTagBean> getHotBookmarkList(@Url String url, @QueryMap Map<String, String> params);

    @GET
    Observable<UpgradeBean> checkUpdateInfo(@Url String url, @QueryMap Map<String, String> params);

    @GET
    Observable<EngineBean> getSearchEngine(@Url String url, @QueryMap Map<String, String> params);

    @GET
    Observable<HotSiteBean> getHotSite(@Url String url, @QueryMap Map<String, String> params);

    @GET("http://ufb.doov.cn:8888/UserFeedBack/BrowserTypeSelect.do")
    Observable<FeedbackTypeBean> getFeedbackType(@QueryMap Map<String, String> params);

    @POST("http://ufb.doov.cn:8888/UserFeedBack/BugListSubmit.do")
    Observable<ResponseBody> sendFeedback(@Body RequestBody Body);

    @POST
    Observable<ResponseBody> uploadRecord(@Url String url, @Body RequestBody body);

}

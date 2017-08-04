package com.example.zy1584.mybase.http;


import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.bean.RecommendBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
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
    Observable<ADResponseBean> getADList(@Url String url, @QueryMap Map<String, String> params);

    @GET
    Observable<ClickLinkResponseBean> reportClick(@Url String url);

    @GET
    Observable<ResponseBody> reportConversion(@Url String url);

    @GET
    Observable<ResponseBody> reportADExposed(@Url String url);

    @GET("http://ha.doov.com.cn:9066/address/query")
    Call<ResponseBody> requestServerAddress(@QueryMap Map<String, String> params);

}

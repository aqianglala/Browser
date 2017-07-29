package com.example.zy1584.mybase.http;


import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.ui.news.RecommendBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;

import java.util.Map;

import okhttp3.ResponseBody;
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

    //登录接口
//    @FormUrlEncoded
//    @POST("demo/login")
//    Observable<BaseHttpResult<LoginBean>> login(@Field("userName") String username, @Field
//            ("passWord") String pwd);

    @GET("http://openapi.inews.qq.com/getQQNewsUnreadList")
    Observable<RecommendBean> getRecommendNewsList(@QueryMap Map<String, String> parmas);

    @GET("http://op.inews.qq.com/mcms/h5/info/navigation")
    Observable<ResponseBody> getChannelList(@QueryMap Map<String, String> parmas);

    @GET("http://op.inews.qq.com/mcms/h5/info/channel_data")
    Observable<NewsChannelBean> getChannelNewsList(@QueryMap Map<String, String> parmas);

    @FormUrlEncoded
    @POST("http://openapi.kuaibao.qq.com/reportActionType")
    Observable<ResponseBody> reportActionType(@FieldMap Map<String, String> params);

    @GET("http://mi.gdt.qq.com/api/v3")
    Observable<ADResponseBean> getADList(@QueryMap(encoded = true) Map<String, String> parmas);

    @GET
    Observable<ClickLinkResponseBean> reportClick(@Url String url);

    @GET
    Observable<ResponseBody> reportConversion(@Url String url);

    @GET
    Observable<ResponseBody> reportADExposed(@Url String url);

}

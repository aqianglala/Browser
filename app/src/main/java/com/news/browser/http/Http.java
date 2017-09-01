package com.news.browser.http;


import android.text.TextUtils;

import com.news.browser.base.BaseApplication;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.SPUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by tzqiang on 2017/3/27.
 */

public class Http {

    public static String BASE_URL = "http://news-at.zhihu.com/";

    private static OkHttpClient client;
    private static HttpService httpService;
    private static Retrofit retrofit;

    private static long connectTimeout = 15; // 默认为10s
    private static long readTimeout = 15; // 默认为10s
    private static long writeTimeout = 20; // 默认为10s

    /**
     * @return retrofit的底层利用反射的方式, 获取所有的api接口的类
     */
    public static HttpService getHttpService() {
        if (httpService == null) {
            httpService = getRetrofit().create(HttpService.class);
        }
        return httpService;
    }


    /**
     * 设置公共参数
     */
    private static Interceptor addQueryParameterInterceptor() {
        Interceptor addQueryParameterInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request request;
                HttpUrl modifiedUrl = originalRequest.url().newBuilder()
                        // Provide your custom parameter here
                        .addQueryParameter("phoneSystem", "")
                        .addQueryParameter("phoneModel", "")
                        .build();
                request = originalRequest.newBuilder().url(modifiedUrl).build();
                return chain.proceed(request);
            }
        };
        return addQueryParameterInterceptor;
    }

    /**
     * 设置头
     */
    private static Interceptor addHeaderInterceptor() {
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder()
                        // Provide your custom header here
//                        .header("token", (String) SPUtils.get("token", ""))
                        .method(originalRequest.method(), originalRequest.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
        return headerInterceptor;
    }

    /**
     * 设置缓存
     * 服务器不支持缓存时，可通过修改返回的头信息实现缓存，慎用
     */
    private static Interceptor addCacheInterceptor() {
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!NetUtils.isConnected(BaseApplication.getContext())) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if (!NetUtils.isConnected(BaseApplication.getContext())) {
                    int maxAge = 0;
                    // 有网络时 设置缓存超时时间0个小时 ,意思就是不读取缓存数据,只对get有用,post没有缓冲
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Retrofit")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                            .build();
                } else {
                    // 无网络时，设置超时为4周  只对get有用,post没有缓冲
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" +
                                    maxStale)
                            .removeHeader("nyn")
                            .build();
                }
                return response;
            }
        };
        return cacheInterceptor;
    }

    /**
     * 地址校验
     */
    private static Interceptor addServerAddressInterceptor() {
        Interceptor serverAddressInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request newRequest = null;

                HttpUrl url = originalRequest.url();
                String host = url.host();
                int port = url.port();
                if (GlobalParams.HOLDER_HOST.equals(host) && port == GlobalParams.HOLDER_PORT) {// 请求地址
                    if (requestServerAddress()) {
                        String newIp = (String) SPUtils.get(GlobalParams.IP, "");
                        String newPort = (String) SPUtils.get(GlobalParams.PORT, "");
                        HttpUrl newUrl = originalRequest.url().newBuilder()
                                .host(newIp)
                                .port(Integer.parseInt(newPort))
                                .build();
                        Request.Builder requestBuilder = originalRequest.newBuilder()
                                .url(newUrl);
                        newRequest = requestBuilder.build();
                    }
                } else if (GlobalParams.HOLDER_HOST_RECORD.equals(host) && port == GlobalParams.HOLDER_PORT_RECORD) {
                    if (requestRecordServerAddress()) {
                        String newIp = (String) SPUtils.get(GlobalParams.RECORD_IP, "");
                        String newPort = (String) SPUtils.get(GlobalParams.RECORD_PORT, "");
                        HttpUrl newUrl = originalRequest.url().newBuilder()
                                .host(newIp)
                                .port(Integer.parseInt(newPort))
                                .build();
                        Request.Builder requestBuilder = originalRequest.newBuilder()
                                .url(newUrl);
                        newRequest = requestBuilder.build();
                    }
                } else {
                    newRequest = originalRequest;
                }

                return chain.proceed(newRequest);
            }
        };
        return serverAddressInterceptor;
    }

    private synchronized static boolean requestServerAddress() {
        try {
            HashMap<String, String> params = NetProtocol.getImpl().getServerAddressMap();
            ResponseBody body = httpService.requestServerAddress(params).execute().body();
            if (body != null) {
                JSONArray jsonArray = new JSONArray(body.string());
                JSONObject obj = (JSONObject) jsonArray.get(0);
                String ip = obj.optString("IP");
                String port = obj.optString("Port");
                if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                    SPUtils.put(GlobalParams.IP, ip);
                    SPUtils.put(GlobalParams.PORT, port);
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized static boolean requestRecordServerAddress() {
        try {
            HashMap<String, String> params = NetProtocol.getImpl().getRecordServerAdressMap();
            ResponseBody body = httpService.requestServerAddress(params).execute().body();
            if (body != null) {
                JSONArray jsonArray = new JSONArray(body.string());
                JSONObject obj = (JSONObject) jsonArray.get(0);
                String ip = obj.optString("IP");
                String port = obj.optString("Port");
                if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                    SPUtils.put(GlobalParams.RECORD_IP, ip);
                    SPUtils.put(GlobalParams.RECORD_PORT, port);
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (Http.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // 设置 请求的缓存的大小跟位置
                    File cacheFile = new File(BaseApplication.getContext().getCacheDir(), "cache");
                    Cache cache = new Cache(cacheFile, 1024 * 1024 * 50); //50Mb 缓存的大小

                    client = new OkHttpClient
                            .Builder()
//                            .addInterceptor(addQueryParameterInterceptor())  // 参数添加
//                            .addInterceptor(addHeaderInterceptor()) // 添加header
//                            .addNetworkInterceptor(addCacheInterceptor())
                            .addInterceptor(httpLoggingInterceptor) //日志,所有的请求响应度看到
                            .addInterceptor(addServerAddressInterceptor())// 地址验证
                            .cache(cache)  //添加缓存
//                            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
//                            .readTimeout(readTimeout, TimeUnit.SECONDS)
//                            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                            .build();

                    // 获取retrofit的实例
                    retrofit = new Retrofit
                            .Builder()
                            .baseUrl(BASE_URL)  //自己配置
                            .client(client)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

}

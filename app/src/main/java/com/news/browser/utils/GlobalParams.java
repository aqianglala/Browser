package com.news.browser.utils;


/**
 * 保存一些全局的变量
 *
 * @author Administrator
 */
public class GlobalParams {

//    数据名称
    public static final String IS_INCOGNITO = "is_incognito";// 是否为隐身模式
    public static final String IS_YUZHUANG = "is_yuzhuang";
    public static final String IS_NET_ALLOW = "is_net_allow";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String IP = "ip";
    public static final String PORT = "port";

//    服务器ip和端口的占位符
    public static final String HOLDER_HOST = "1.1.1.1";
    public static final int HOLDER_PORT = 1111;

//    接口地址
    public static final String RECOMMEND = "browser/recommend";
    public static final String CHANNEL = "browser/channel";
    public static final String CHANNEL_DATA = "browser/channel_data";
    public static final String ADVERTISING = "browser/advertising";// 广告
    public static final String NAVIGATION = "browser/home/navigation"; // 首页导航
    public static final String HOME_TAG = "browser/home/tag"; // 主页标签
    public static final String HOT_SITE = "browser/hotsite/list"; // 热门网站
    public static final String UPGRADE_INFO = "browser/upgrade/info"; // loading图
    public static final String SEARCH_ENGINE = "browser/search/engine"; // 搜索引擎

//    数据缓存
    public static final String DATA_NAVIGATION = "data_navigation"; // 导航缓存
    public static final String DATA_CHANNEL = "data_channel"; // 频道缓存
    public static final String DATA_RECOMMEND_NEWS = "data_recommend_news"; // 推荐新闻缓存
    public static final String DATA_RECOMMEND_AD = "data_recommend_ad"; // 推荐广告缓存

    public static final String LAST_UPDATE_TIME = "last_update_time"; // 上次获取新闻的时间

}

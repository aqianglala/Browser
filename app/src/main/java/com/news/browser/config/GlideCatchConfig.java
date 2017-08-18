package com.news.browser.config;

/**
 * Glide缓存配置文件
 */

public class GlideCatchConfig {

    // 图片缓存最大容量，150M，根据自己的需求进行修改
    public static final int GLIDE_CACHE_SIZE = 150 * 1000 * 1000;

    // 图片缓存子目录
    public static final String GLIDE_CACHE_DIR = "image_cache";

}

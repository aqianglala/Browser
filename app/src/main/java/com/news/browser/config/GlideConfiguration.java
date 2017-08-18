package com.news.browser.config;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * GlideConfiguration
 */

public class GlideConfiguration implements GlideModule {


    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //自定义缓存目录
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context,
                GlideCatchConfig.GLIDE_CACHE_DIR,
                GlideCatchConfig.GLIDE_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        //nil
    }
}

package com.news.browser.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.news.browser.R;
import com.news.browser.widget.GlideRoundTransform;

/**
 * Created by zy1584 on 2017-8-21.
 */

public class GlideUtils {


    public static void loadNewsImage(Context context, String url, ImageView imageView){
        Glide.with(context)
                .load(url)
                .placeholder(R.color.empty_news_bg)
                .error(R.color.empty_news_bg)
                .into(imageView);
    }

    public static void loadIconImage(Context context, String url, ImageView imageView){
        Glide.with(context)
                .load(url)
                .transform(new GlideRoundTransform(context))
                .placeholder(R.drawable.icon_default)
                .error(R.drawable.icon_default)
                .into(imageView);
    }
}

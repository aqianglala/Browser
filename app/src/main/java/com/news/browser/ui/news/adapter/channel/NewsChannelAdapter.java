package com.news.browser.ui.news.adapter.channel;

import android.content.Context;

import com.news.browser.bean.BaseNewsItem;
import com.news.browser.ui.news.adapter.ItemADBigImgDelegate;
import com.news.browser.ui.news.adapter.ItemEmptyDelegate;
import com.news.browser.ui.news.interfaces.OnADItemClickListener;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.List;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsChannelAdapter extends MultiItemTypeAdapter<BaseNewsItem> {
    public NewsChannelAdapter(Context context, List<BaseNewsItem> datas, OnADItemClickListener listener) {
        super(context, datas);
        addItemViewDelegate(new ItemBigImgDelegate());
        addItemViewDelegate(new ItemSmallImgDelegate());
        addItemViewDelegate(new ItemThreeImgDelegate());
        addItemViewDelegate(new ItemADBigImgDelegate(listener));
        addItemViewDelegate(new ItemEmptyDelegate());
    }
}

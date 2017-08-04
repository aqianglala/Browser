package com.example.zy1584.mybase.ui.news.adapter.channel;

import android.content.Context;

import com.example.zy1584.mybase.bean.BaseNewsItem;
import com.example.zy1584.mybase.ui.news.adapter.ItemADBigImgDelegate;
import com.example.zy1584.mybase.ui.news.interfaces.OnADItemClickListener;
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
    }
}

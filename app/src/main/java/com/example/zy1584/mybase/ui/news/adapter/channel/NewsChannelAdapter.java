package com.example.zy1584.mybase.ui.news.adapter.channel;

import android.content.Context;

import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.List;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsChannelAdapter extends MultiItemTypeAdapter<ContentBean> {
    public NewsChannelAdapter(Context context, List<ContentBean> datas) {
        super(context, datas);
        addItemViewDelegate(new ItemBigImgDelegate());
        addItemViewDelegate(new ItemSmallImgDelegate());
        addItemViewDelegate(new ItemThreeImgDelegate());
    }
}

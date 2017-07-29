package com.example.zy1584.mybase.ui.news.adapter.recommend;

import android.content.Context;

import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean ;
import com.example.zy1584.mybase.bean.BaseNewsItem;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsRecommendAdapter extends MultiItemTypeAdapter<BaseNewsItem> {
    public NewsRecommendAdapter(Context context, List<BaseNewsItem> datas, onADItemClickListener listener) {
        super(context, datas);
        addItemViewDelegate(new ItemBigImgDelegate());
        addItemViewDelegate(new ItemSmallImgDelegate());
        addItemViewDelegate(new ItemThreeImgDelegate());
        addItemViewDelegate(new ItemADBigImgDelegate(listener));
    }

    public interface onADItemClickListener{
        void onADItemClick(ViewHolder holder, ListBean item, int position, int dowX, int downY,
                           int upX, int upY);
    }
}

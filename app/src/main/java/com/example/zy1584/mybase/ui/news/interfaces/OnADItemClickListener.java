package com.example.zy1584.mybase.ui.news.interfaces;

import com.example.zy1584.mybase.bean.ADResponseBean;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-7-31.
 */

public interface OnADItemClickListener {
    void onADItemClick(ViewHolder holder, ADResponseBean.DataBean._$8050018672826551Bean.ListBean item, int position, int dowX, int downY,
                       int upX, int upY);
}

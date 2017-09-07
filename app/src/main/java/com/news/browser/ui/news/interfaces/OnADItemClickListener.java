package com.news.browser.ui.news.interfaces;

import com.news.browser.bean.ADBean;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-7-31.
 */

public interface OnADItemClickListener {
    void onADItemClick(ViewHolder holder, ADBean item, int position, int dowX, int downY,
                       int upX, int upY);
}

package com.news.browser.ui.hotSite.adapter;

import android.content.Context;

import com.news.browser.bean.BaseItem;
import com.news.browser.bean.HotSiteBean;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-13.
 */

public class HotSiteAdapter extends MultiItemTypeAdapter<BaseItem> {
    public HotSiteAdapter(Context context, List<BaseItem> datas, OnButtonClickListener listener) {
        super(context, datas);
        addItemViewDelegate(new ItemHotSiteDelegate(listener));
        addItemViewDelegate(new ItemHotSiteTitleDelegate());
    }

    public interface OnButtonClickListener{
        void onAddClick(ViewHolder holder, HotSiteBean.DataBean bean, int position);

        void onOpenClick(ViewHolder holder, HotSiteBean.DataBean bean, int position);
    }
}

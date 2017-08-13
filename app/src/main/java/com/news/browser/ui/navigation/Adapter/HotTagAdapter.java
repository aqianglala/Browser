package com.news.browser.ui.navigation.Adapter;

import android.content.Context;

import com.news.browser.bean.HotTagBean;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-7.
 */

public class HotTagAdapter extends MultiItemTypeAdapter<HotTagBean.DataBean>{

    private final ItemDelegate itemDelegate;
    private final ItemAddDelegate itemAddDelegate;

    public HotTagAdapter(Context context, List<HotTagBean.DataBean> datas, OnSiteRemoveClickListener listener) {
        super(context, datas);
        itemDelegate = new ItemDelegate(listener);
        itemAddDelegate = new ItemAddDelegate();
        addItemViewDelegate(itemDelegate);
        addItemViewDelegate(itemAddDelegate);
    }

    private boolean isEditable;
    public void setEditable(boolean isEditable){
        this.isEditable = isEditable;
        itemDelegate.setEditable(isEditable);
    }

    public boolean isEditable() {
        return isEditable;
    }

    public interface OnSiteRemoveClickListener{
        void onRemoveClick(ViewHolder holder, HotTagBean.DataBean dataBean, int position);
    }
}

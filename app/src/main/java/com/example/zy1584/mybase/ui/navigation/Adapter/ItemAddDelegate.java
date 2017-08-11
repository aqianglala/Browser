package com.example.zy1584.mybase.ui.navigation.Adapter;

import android.view.View;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.bean.HotTagBean;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-8-7.
 */

public class ItemAddDelegate implements ItemViewDelegate<HotTagBean.DataBean> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_hot_tag;
    }

    @Override
    public boolean isForViewType(HotTagBean.DataBean item, int position) {
        if (item == null){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, HotTagBean.DataBean dataBean, int position) {
        holder.getView(R.id.tv_name).setVisibility(View.INVISIBLE);
        holder.setImageResource(R.id.iv_icon, R.drawable.ic_hot_tag_add);
    }
}

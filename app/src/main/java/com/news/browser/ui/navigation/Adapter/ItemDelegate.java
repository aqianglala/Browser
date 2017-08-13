package com.news.browser.ui.navigation.Adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.news.browser.R;
import com.news.browser.bean.HotTagBean;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


/**
 * Created by zy1584 on 2017-8-7.
 */

public class ItemDelegate implements ItemViewDelegate<HotTagBean.DataBean> {

    private boolean isEditable;
    private HotTagAdapter.OnSiteRemoveClickListener listener;

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public ItemDelegate(HotTagAdapter.OnSiteRemoveClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_hot_tag;
    }

    @Override
    public boolean isForViewType(HotTagBean.DataBean item, int position) {
        if (item != null){
            return true;
        }
        return false;
    }

    @Override
    public void convert(final ViewHolder holder, final HotTagBean.DataBean dataBean, final int position) {
        holder.setText(R.id.tv_name, dataBean.getName());
        ImageView iv_icon = holder.getView(R.id.iv_icon);
        Context context = holder.getConvertView().getContext();
        Glide.with(context).load(dataBean.getIconUrl()).into(iv_icon);
        if (isEditable && dataBean.getIsErase() == 1){
            holder.setVisible(R.id.iv_delete, true);
        }else{
            holder.setVisible(R.id.iv_delete, false);
        }
        holder.setOnClickListener(R.id.iv_delete, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRemoveClick(holder, dataBean, position);
            }
        });
    }
}

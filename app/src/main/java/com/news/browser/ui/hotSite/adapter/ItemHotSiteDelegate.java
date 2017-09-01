package com.news.browser.ui.hotSite.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.news.browser.R;
import com.news.browser.bean.BaseItem;
import com.news.browser.bean.HotSiteBean;
import com.news.browser.db.HotTagDatabase;
import com.news.browser.utils.GlideUtils;
import com.news.browser.utils.UIUtils;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-8-13.
 */

public class ItemHotSiteDelegate implements ItemViewDelegate<BaseItem> {

    private final HotTagDatabase hotTagDatabase;

    private HotSiteAdapter.OnButtonClickListener listener;
    public ItemHotSiteDelegate(HotSiteAdapter.OnButtonClickListener listener) {
        hotTagDatabase = HotTagDatabase.getInstance();
        this.listener = listener;
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_hot_site;
    }

    @Override
    public boolean isForViewType(BaseItem item, int position) {
        if (item instanceof HotSiteBean.DataBean){
            return true;
        }
        return false;
    }

    @Override
    public void convert(final ViewHolder holder, BaseItem baseItem, final int position) {
        final HotSiteBean.DataBean dataBean = (HotSiteBean.DataBean) baseItem;
        Context context = holder.getConvertView().getContext();
        ImageView iv_icon = holder.getView(R.id.iv_icon);
        GlideUtils.loadIconImage(context, dataBean.getIconUrl(), iv_icon);
        holder.setText(R.id.tv_name, dataBean.getName());
        holder.setText(R.id.tv_url, dataBean.getAddrUrl());

        final Button btn_action = holder.getView(R.id.btn_action);
        boolean contain = hotTagDatabase.isContain(dataBean.getName(), dataBean.getAddrUrl());
        setButton(btn_action, contain);
        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = btn_action.getText().toString();
                if (s.equals("打开")){
                    listener.onOpenClick(holder, dataBean, position);
                }else if (s.equals("添加")){
                    setButton((Button) v, true);
                    listener.onAddClick(holder, dataBean, position);
                }
            }
        });
    }

    private void setButton(Button btn_action, boolean contain) {
        if (contain){
            btn_action.setBackgroundResource(R.drawable.shape_btn_bg_white);
            btn_action.setTextColor(UIUtils.getColor(R.color.text_gray));
            btn_action.setText("打开");
        }else{
            btn_action.setBackgroundResource(R.drawable.shape_btn_bg_blue);
            btn_action.setTextColor(UIUtils.getColor(R.color.text_blue));
            btn_action.setText("添加");
        }
    }
}

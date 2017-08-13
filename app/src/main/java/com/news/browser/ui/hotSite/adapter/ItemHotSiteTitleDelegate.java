package com.news.browser.ui.hotSite.adapter;

import com.news.browser.R;
import com.news.browser.bean.BaseItem;
import com.news.browser.bean.HotSiteTitleBean;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-8-13.
 */

public class ItemHotSiteTitleDelegate implements ItemViewDelegate<BaseItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_hot_site_title;
    }

    @Override
    public boolean isForViewType(BaseItem item, int position) {
        if (item instanceof HotSiteTitleBean){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseItem baseItem, int position) {
        HotSiteTitleBean bean = (HotSiteTitleBean) baseItem;
        holder.setText(R.id.tv_group, bean.getTitle());
    }
}

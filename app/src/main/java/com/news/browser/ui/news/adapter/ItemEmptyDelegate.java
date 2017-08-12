package com.news.browser.ui.news.adapter;

import com.news.browser.R;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.ui.news.bean.EmptyNewsBean;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemEmptyDelegate implements ItemViewDelegate<BaseNewsItem> {

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_empty;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof EmptyNewsBean){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
    }

}

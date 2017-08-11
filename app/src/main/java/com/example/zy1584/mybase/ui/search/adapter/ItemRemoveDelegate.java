package com.example.zy1584.mybase.ui.search.adapter;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.bean.SearchHistoryItem;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-8-11.
 */

public class ItemRemoveDelegate implements ItemViewDelegate<SearchHistoryItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.include_remove;
    }

    @Override
    public boolean isForViewType(SearchHistoryItem item, int position) {
        if (item == null){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, SearchHistoryItem item, int position) {
    }
}

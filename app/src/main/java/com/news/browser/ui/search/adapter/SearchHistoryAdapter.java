package com.news.browser.ui.search.adapter;

import android.content.Context;

import com.news.browser.bean.SearchHistoryItem;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-11.
 */

public class SearchHistoryAdapter extends MultiItemTypeAdapter<SearchHistoryItem> {
    public SearchHistoryAdapter(Context context, List<SearchHistoryItem> datas) {
        super(context, datas);
        addItemViewDelegate(new ItemHistoryDelegate());
        addItemViewDelegate(new ItemRemoveDelegate());
    }
}

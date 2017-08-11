package com.example.zy1584.mybase.ui.search.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.bean.SearchHistoryItem;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-8-11.
 */

public class ItemHistoryDelegate implements ItemViewDelegate<SearchHistoryItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_search_history;
    }

    @Override
    public boolean isForViewType(SearchHistoryItem item, int position) {
        if (item != null){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, SearchHistoryItem item, int position) {
        String title = item.getTitle();
        String url = item.getUrl();
        ImageView iv_icon = holder.getView(R.id.iv_icon);

        if (TextUtils.isEmpty(title)){
            holder.setVisible(R.id.tv_name, false);
        }else{
            holder.setVisible(R.id.tv_name, true);
            holder.setText(R.id.tv_name, title);
        }
        if (TextUtils.isEmpty(url)){
            holder.setVisible(R.id.tv_url, false);
            iv_icon.setImageResource(R.drawable.ic_search_gray);
        }else{
            holder.setVisible(R.id.tv_url, true);
            holder.setText(R.id.tv_url, url);
            iv_icon.setImageResource(R.drawable.ic_url);
        }
    }
}

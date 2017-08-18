package com.news.browser.ui.bookmark;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.base.BasePresenter;
import com.news.browser.bus.RXEvent;
import com.news.browser.ui.main.db.BookmarkManager;
import com.news.browser.ui.main.db.HistoryItem;
import com.news.browser.utils.RxBus;
import com.news.browser.widget.DividerItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by zy1584 on 2017-8-1.
 */

public class BookmarkFragment extends BaseFragment implements MultiItemTypeAdapter.OnItemClickListener,
        BaseControlView {

    private final List<HistoryItem> mBookmarks = new ArrayList<>();
    private final HashMap<HistoryItem, Boolean> mStatusMap = new HashMap<>();
    private BookmarkManager mBookmarkManager;
    private BookmarkAdapter mBookmarkAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.ll_delete)
    LinearLayout ll_delete;

    @OnClick(R.id.ll_delete)
    void delete(){
        Iterator<Map.Entry<HistoryItem, Boolean>> entries = mStatusMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<HistoryItem, Boolean> entry = entries.next();
            if (entry.getValue()){
                mBookmarkManager.deleteBookmark(entry.getKey());
                mBookmarks.remove(entry.getKey());
                entries.remove();
            }
        }
        mBookmarkAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bookmark;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mBookmarkManager = BookmarkManager.getInstance();
        List<HistoryItem> list = mBookmarkManager.getBookmarksFromFolder(null, true);
        mBookmarks.addAll(list);
        for (HistoryItem item : mBookmarks) {
            mStatusMap.put(item, false);
        }
        mBookmarkAdapter = new BookmarkAdapter(mActivity, R.layout.item_list_bookmark_history, mBookmarks);
        initRecyclerView(mRecyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mBookmarkAdapter);

        mBookmarkAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        boolean editStatus = mBookmarkAdapter.isEditStatus();
        HistoryItem item = mBookmarks.get(position);
        if (item == null) return;
        if (editStatus) {
            Boolean isSelected = mStatusMap.get(item);
            mStatusMap.put(item, !isSelected);
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.setImageResource(R.id.iv_choose, isSelected ? R.drawable.ic_unselected : R.drawable.ic_selected_circle);
        } else {// 打开网页
            mActivity.getSupportFragmentManager().popBackStack();
            RxBus.getInstance().post(new RXEvent(RXEvent.TAG_SEARCH, item.getUrl()));
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    @Override
    public void selectAll() {
        for (HistoryItem item : mBookmarks) {
            mStatusMap.put(item, true);
        }
        mBookmarkAdapter.notifyDataSetChanged();
    }

    @Override
    public void unSelectAll() {
        for (HistoryItem item : mBookmarks) {
            mStatusMap.put(item, false);
        }
        mBookmarkAdapter.notifyDataSetChanged();
    }

    @Override
    public void setEditable(boolean isEditable) {
        ll_delete.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        mBookmarkAdapter.setEditStatus(isEditable);
        mBookmarkAdapter.notifyDataSetChanged();
    }

    class BookmarkAdapter extends CommonAdapter<HistoryItem> {

        private boolean isEditStatus;

        public boolean isEditStatus() {
            return isEditStatus;
        }

        public void setEditStatus(boolean editStatus) {
            isEditStatus = editStatus;
        }

        public BookmarkAdapter(Context context, int layoutId, List<HistoryItem> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(ViewHolder holder, HistoryItem historyItem, int position) {
            holder.setText(R.id.tv_title, historyItem.getTitle());
            holder.setText(R.id.tv_url, historyItem.getUrl());
            holder.setVisible(R.id.iv_choose, isEditStatus ? true : false);
            holder.setImageResource(R.id.iv_choose, mStatusMap.get(historyItem)
                    ? R.drawable.ic_selected_circle
                    : R.drawable.ic_unselected);
        }
    }

}

package com.example.zy1584.mybase.ui.bookmark;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.bus.RXEvent;
import com.example.zy1584.mybase.ui.main.db.HistoryDatabase;
import com.example.zy1584.mybase.ui.main.db.HistoryItem;
import com.example.zy1584.mybase.utils.RxBus;
import com.example.zy1584.mybase.widget.DividerItemDecoration;
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
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zy1584 on 2017-8-1.
 */

public class HistoryFragment extends BaseFragment implements MultiItemTypeAdapter.OnItemClickListener,
BaseControlView{

    private final List<HistoryItem> mHistories = new ArrayList<>();
    private final HashMap<HistoryItem, Boolean> mStatusMap = new HashMap<>();
    private HistoryDatabase mHistoryDatabase;
    private HistoryAdapter mHistoryAdapter;

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
                mHistoryDatabase.deleteHistoryItem(entry.getKey().getUrl());
                mHistories.remove(entry.getKey());
                entries.remove();
            }
        }
        mHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_history;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mHistoryDatabase = HistoryDatabase.getInstance();
        getHistoryList().subscribe(new Action1<List<HistoryItem>>() {
            @Override
            public void call(List<HistoryItem> historyItems) {
                mHistories.addAll(historyItems);
                for (HistoryItem item : mHistories) {
                    mStatusMap.put(item, false);
                }
                mHistoryAdapter = new HistoryAdapter(mActivity, R.layout.item_list_bookmark_history, mHistories);
                initRecyclerView(mRecyclerView);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
                mRecyclerView.setAdapter(mHistoryAdapter);

                mHistoryAdapter.setOnItemClickListener(HistoryFragment.this);
            }
        });
    }

    private Observable<List<HistoryItem>> getHistoryList() {
        return Observable.create(new Observable.OnSubscribe<List<HistoryItem>>() {
            @Override
            public void call(Subscriber<? super List<HistoryItem>> subscriber) {
                List<HistoryItem> historyList = mHistoryDatabase.getLastHundredItems();// 获取历史记录
                subscriber.onNext(historyList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        boolean editStatus = mHistoryAdapter.isEditStatus();
        HistoryItem item = mHistories.get(position);
        if (editStatus) {
            Boolean isSelected = mStatusMap.get(item);
            mStatusMap.put(item, !isSelected);
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.setImageResource(R.id.iv_choose, isSelected ? R.drawable.ic_unselected : R.drawable.ic_selected);
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
        for (HistoryItem item : mHistories) {
            mStatusMap.put(item, true);
        }
        mHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void unSelectAll() {
        for (HistoryItem item : mHistories) {
            mStatusMap.put(item, false);
        }
        mHistoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void setEditable(boolean isEditable) {
        ll_delete.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        mHistoryAdapter.setEditStatus(isEditable);
        mHistoryAdapter.notifyDataSetChanged();
    }

    class HistoryAdapter extends CommonAdapter<HistoryItem> {

        private boolean isEditStatus;

        public boolean isEditStatus() {
            return isEditStatus;
        }

        public void setEditStatus(boolean editStatus) {
            isEditStatus = editStatus;
        }

        public HistoryAdapter(Context context, int layoutId, List<HistoryItem> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(ViewHolder holder, HistoryItem historyItem, int position) {
            holder.setText(R.id.tv_title, historyItem.getTitle());
            holder.setText(R.id.tv_url, historyItem.getUrl());
            holder.setVisible(R.id.iv_choose, isEditStatus ? true : false);
            holder.setImageResource(R.id.iv_choose, mStatusMap.get(historyItem)
                    ? R.drawable.ic_selected
                    : R.drawable.ic_unselected);
        }
    }
}

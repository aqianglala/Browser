package com.news.browser.ui.bookmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.news.browser.R;
import com.news.browser.async.ImageDownloadTask;
import com.news.browser.base.BaseFragment;
import com.news.browser.base.BasePresenter;
import com.news.browser.bus.RXEvent;
import com.news.browser.ui.main.db.HistoryDatabase;
import com.news.browser.ui.main.db.HistoryItem;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.Utils;
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
        BaseControlView {

    private final List<HistoryItem> mHistories = new ArrayList<>();
    private final HashMap<HistoryItem, Boolean> mStatusMap = new HashMap<>();
    private HistoryDatabase mHistoryDatabase;
    private HistoryAdapter mHistoryAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.ll_delete)
    LinearLayout ll_delete;
    private BookmarkHistoryFragment mParentFragment;

    @OnClick(R.id.ll_delete)
    void delete() {
        Iterator<Map.Entry<HistoryItem, Boolean>> entries = mStatusMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<HistoryItem, Boolean> entry = entries.next();
            if (entry.getValue()) {
                mHistoryDatabase.deleteHistoryItem(entry.getKey().getUrl());
                mHistories.remove(entry.getKey());
                entries.remove();
            }
        }
        mHistoryAdapter.notifyDataSetChanged();
        if (mHistories.size() == 0) {
            mParentFragment.cancel();
        }
        mParentFragment.setEditButtonVisibility(mHistories.size() > 0 ? true : false);
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
        mParentFragment = (BookmarkHistoryFragment) getParentFragment();
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
                mRecyclerView.addItemDecoration(getDefaultDivider());
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

    public boolean isShowEditButton() {
        if (mHistories.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        boolean editStatus = mHistoryAdapter.isEditStatus();
        HistoryItem item = mHistories.get(position);
        if (editStatus) {
            Boolean isSelected = mStatusMap.get(item);
            mStatusMap.put(item, !isSelected);
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.setImageResource(R.id.iv_choose, isSelected ? R.drawable.ic_unselected : R.drawable.ic_selected_circle);
            mParentFragment.setIsSelectAll(isSelectAll());
        } else {// 打开网页
            mActivity.getSupportFragmentManager().popBackStack();
            RxBus.getInstance().post(new RXEvent(RXEvent.TAG_SEARCH, item.getUrl()));
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    public boolean isSelectAll() {
        boolean isSelectAll = true;
        for (Boolean value : mStatusMap.values()) {
            if (!value) {
                isSelectAll = false;
                break;
            }
        }
        return isSelectAll;
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
        protected void convert(ViewHolder holder, final HistoryItem historyItem, int position) {
            holder.setText(R.id.tv_title, historyItem.getTitle());
            holder.setText(R.id.tv_url, historyItem.getUrl());
            holder.setVisible(R.id.iv_choose, isEditStatus ? true : false);
            holder.setImageResource(R.id.iv_choose, mStatusMap.get(historyItem)
                    ? R.drawable.ic_selected_circle
                    : R.drawable.ic_unselected);

            final ImageView iv_icon = holder.getView(R.id.iv_icon);

            if (historyItem.getBitmap() == null) {
                ImageDownloadTask.getIcon(mActivity, historyItem.getUrl())
                        .subscribe(new Action1<Bitmap>() {
                            @Override
                            public void call(Bitmap bitmap) {
                                if (bitmap != null) {
                                    Bitmap fav = Utils.padFavicon(bitmap);
                                    iv_icon.setImageBitmap(fav);
                                    historyItem.setBitmap(fav);
                                }else{
                                    iv_icon.setImageResource(R.drawable.icon_default);
                                }
                            }
                        });
            } else {
                iv_icon.setImageBitmap(historyItem.getBitmap());
            }
        }
    }
}

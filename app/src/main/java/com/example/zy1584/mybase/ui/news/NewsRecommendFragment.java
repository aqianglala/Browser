package com.example.zy1584.mybase.ui.news;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;
import com.example.zy1584.mybase.bean.BaseNewsItem;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.bean.RecommendBean;
import com.example.zy1584.mybase.bean.RecommendBean.NewslistBean;
import com.example.zy1584.mybase.preference.PreferenceManager;
import com.example.zy1584.mybase.ui.download.DownloadHandler;
import com.example.zy1584.mybase.ui.main.BrowserActivity;
import com.example.zy1584.mybase.ui.news.adapter.recommend.NewsRecommendAdapter;
import com.example.zy1584.mybase.ui.news.interfaces.OnADItemClickListener;
import com.example.zy1584.mybase.ui.news.mvp.recommend.NewsRecommendContract;
import com.example.zy1584.mybase.ui.news.mvp.recommend.NewsRecommendPresenter;
import com.example.zy1584.mybase.utils.UIUtils;
import com.example.zy1584.mybase.widget.DividerItemDecoration;
import com.orhanobut.logger.Logger;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

import static com.example.zy1584.mybase.R.id.recyclerView;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsRecommendFragment extends BaseFragment<NewsRecommendPresenter> implements NewsRecommendContract.View,
        LoadMoreWrapper.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener,
        MultiItemTypeAdapter.OnItemClickListener, OnADItemClickListener {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(recyclerView)
    RecyclerView mRecyclerView;

    private PreferenceManager mPreferenceManager;
    private LinearLayoutManager mLayoutManager;
    private LoadMoreWrapper mAdapter;
    private List<BaseNewsItem> mData = new ArrayList<>();
    private List<NewslistBean> mNewsList = new ArrayList<>();
    private boolean isLoading;
    private boolean isRefresh;
    private final TimingHandler mTimingHandler = new TimingHandler();

    private class TimingHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg != null && msg.obj != null){
                ListBean bean = (ListBean) msg.obj;
                if (!bean.isHasExpose() && bean.isTiming()){
                    bean.setTiming(false);
                    removeCallbacksAndMessages(bean);
                    mPresenter.reportADExpose(bean);
                }
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news;
    }

    @Override
    protected NewsRecommendPresenter loadPresenter() {
        return new NewsRecommendPresenter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mSwipeRefreshLayout.setColorSchemeColors(UIUtils.getColor(R.color.colorPrimary));

        NewsRecommendAdapter originalAdapter = new NewsRecommendAdapter(mActivity, mData, this);
        originalAdapter.setOnItemClickListener(this);
        mAdapter = new LoadMoreWrapper(originalAdapter);
        mAdapter.setLoadMoreView(R.layout.layout_load_more);
        mAdapter.setOnLoadMoreListener(this);

        mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(mActivity));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new OnRVScrollListener());
    }

    @Override
    protected void setListener() {
        super.setListener();
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        isLoading = true;
        isRefresh = true;
        mPreferenceManager = PreferenceManager.getInstance();
        mPresenter.getNewsRecommendList();
    }

    @Override
    public void onReceiveRecommendList(RecommendBean bean) {
        isLoading = false;
        List<RecommendBean.NewslistBean> newslist = bean.getNewslist();
        if (newslist == null) return;
        if (isRefresh){
            mData.clear();
            mData.addAll(newslist);
            mNewsList.clear();
            mNewsList.addAll(newslist);
        }else{
            mData.addAll(newslist);
            mNewsList.addAll(newslist);
        }
        mPresenter.getADList();// 加载广告
    }

    @Override
    public void onGetRecommendListError(Throwable e) {
        isLoading = false;
        if (isRefresh){
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onReceiveADList(ADResponseBean bean) {
        List<ListBean> list = bean.getData().get_$8050018672826551().getList();
        if (list != null){
            ListBean listBean = list.get(0);
            Random random = new Random();
            int index = mData.size() - 7 + random.nextInt(5);
            Logger.t("index").e("data size :" + mData.size() + "    index:" + index);
            mData.add(index, listBean);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onGetADListError(Throwable e) {
        notifyDataSetChanged();
    }

    @Override
    public void onReceiveReportClick(final ClickLinkResponseBean responseBean, ListBean listBean) {
        final String clickid = responseBean.getData().getClickid();
        final String dstlink = responseBean.getData().getDstlink();
        final String conversion_link = listBean.getConversion_link();
        String fileName = URLUtil.guessFileName(dstlink, null, null);
        DownloadHandler.promptDownload(mActivity, mPreferenceManager, fileName, dstlink, null, null,
                null, clickid, conversion_link);
    }

    @Override
    public void onLoadMoreRequested() {
        if (!isLoading){
            isLoading = true;
            isRefresh = false;
            mPresenter.getNewsRecommendList();
        }
    }

    @Override
    public void onRefresh() {
        if (!isLoading){
            isLoading = true;
            isRefresh = true;
            mPresenter.getNewsRecommendList();
        }
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        BaseNewsItem item = mData.get(position);
        if (item instanceof NewslistBean){
            NewslistBean bean = (NewslistBean) item;
            BrowserActivity browserAct = (BrowserActivity) mActivity;
            browserAct.searchTheWeb(bean.getUrl());
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    private void notifyDataSetChanged(){
        if (mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onADItemClick(ViewHolder holder, ListBean item, int position, int dowX, int downY, int upX, int upY) {
        String click_link = item.getClick_link();
        if (TextUtils.isEmpty(click_link)) return;

        int width = holder.getConvertView().getWidth();
        int height = holder.getConvertView().getHeight();
        String url = click_link.replace("__WIDTH__", String.valueOf(width))
                .replace("__HEIGHT__", String.valueOf(height))
                .replace("__DOWN_X__", String.valueOf(dowX))
                .replace("__DOWN_Y__", String.valueOf(downY))
                .replace("__UP_X__", String.valueOf(upX))
                .replace("__UP_Y__", String.valueOf(upY));
        int interact_type = item.getInteract_type();
        if (interact_type == 0){
            BrowserActivity browserAct = (BrowserActivity) mActivity;
            browserAct.searchTheWeb(url);
        }else if (interact_type == 1){
            mPresenter.reportClick(url, item);
        }
    }

    private class OnRVScrollListener extends RecyclerView.OnScrollListener{
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mData.size() == 0) return;
            int first = mLayoutManager.findFirstVisibleItemPosition();
            int last = mLayoutManager.findLastVisibleItemPosition();
            for (int i = first; i <= last; i ++){
                if (i >= mData.size()) continue;// footer
                BaseNewsItem item = mData.get(i);
                if (item instanceof ListBean){// 广告
                    ListBean bean = (ListBean) item;
                    if (bean.isHasExpose()) continue; // 已经上报
                    View childAt = mLayoutManager.getChildAt(i - first);
                    int percents = bean.getVisibilityPercents(childAt);
                    if (percents >= 50){
                        if (!bean.isTiming()){
                            Message msg = mTimingHandler.obtainMessage();
                            if (msg != null) {
                                msg.obj = bean;
                                bean.setTiming(true);
                                mTimingHandler.sendMessageDelayed(msg, 1000);
                            }
                        }
                    }else{
                        bean.setTiming(false);
                        mTimingHandler.removeCallbacksAndMessages(bean);
                    }
                }
            }
        }
    }
}

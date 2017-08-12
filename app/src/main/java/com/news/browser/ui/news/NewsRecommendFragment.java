package com.news.browser.ui.news;

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

import com.google.gson.Gson;
import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.ADResponseBean;
import com.news.browser.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.bean.RecommendBean;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.download.DownloadHandler;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.news.adapter.recommend.NewsRecommendAdapter;
import com.news.browser.ui.news.bean.EmptyNewsBean;
import com.news.browser.ui.news.interfaces.OnADItemClickListener;
import com.news.browser.ui.news.mvp.recommend.NewsRecommendContract;
import com.news.browser.ui.news.mvp.recommend.NewsRecommendPresenter;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.SPUtils;
import com.news.browser.utils.UIUtils;
import com.news.browser.widget.DividerItemDecoration;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;


/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsRecommendFragment extends BaseFragment<NewsRecommendPresenter> implements NewsRecommendContract.View,
        LoadMoreWrapper.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener,
        MultiItemTypeAdapter.OnItemClickListener, OnADItemClickListener {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private PreferenceManager mPreferenceManager;
    private LinearLayoutManager mLayoutManager;
    private LoadMoreWrapper mAdapter;
    private List<BaseNewsItem> mData = new ArrayList<>();
    private List<RecommendBean.NewslistBean> mNewsList = new ArrayList<>();
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
        mPreferenceManager = PreferenceManager.getInstance();
        // 从缓存中读取，如果无缓存，直接请求，如果有缓存，判断是否离上次更新超过5分钟，超过则请求
        String json_news = (String) SPUtils.get(GlobalParams.DATA_RECOMMEND_NEWS, "");
        String json_ad = (String) SPUtils.get(GlobalParams.DATA_RECOMMEND_AD, "");

        readFromCache(json_news, json_ad);

        if (TextUtils.isEmpty(json_news)){
            boolean connected = NetUtils.isConnected(mActivity);
            if (connected){
                isLoading = true;
                isRefresh = true;
                mPresenter.getNewsRecommendList();
            }
        }else{
            long last_update_time = (long) SPUtils.get(GlobalParams.LAST_UPDATE_TIME, 0l);
            if (last_update_time == 0){
                isLoading = true;
                isRefresh = true;
                mPresenter.getNewsRecommendList();
            }else{
                long currentTimeMillis = System.currentTimeMillis();
                long dt = currentTimeMillis - last_update_time;
                if (dt > 300 * 1000){
                    isLoading = true;
                    isRefresh = true;
                    mPresenter.getNewsRecommendList();
                }else{
                    isLoading = false;
                }
            }
        }
    }

    private void readFromCache(String json_news, String json_ad) {
        if (!TextUtils.isEmpty(json_news)){
            RecommendBean recommendBean = new Gson().fromJson(json_news, RecommendBean.class);
            List<RecommendBean.NewslistBean> newslist = recommendBean.getNewslist();
            if (isRefresh){
                mData.clear();
                mData.addAll(newslist);
                mNewsList.clear();
                mNewsList.addAll(newslist);
            }else{
                mData.addAll(newslist);
                mNewsList.addAll(newslist);
            }
        }

        if (!TextUtils.isEmpty(json_news) && !TextUtils.isEmpty(json_ad)){
            ADResponseBean adResponseBean = new Gson().fromJson(json_ad, ADResponseBean.class);
            List<ListBean> list = adResponseBean.getData().get_$8050018672826551().getList();
            if (list != null){
                ListBean listBean = list.get(0);
                Random random = new Random();
                int index = mData.size() - 7 + random.nextInt(5);// 随机位置
                mData.add(index, listBean);
            }
        }
        if (TextUtils.isEmpty(json_news) && TextUtils.isEmpty(json_ad)){
            for (int i = 0; i < 10; i++){
                mData.add(new EmptyNewsBean());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onReceiveRecommendList(RecommendBean bean) {
        List<RecommendBean.NewslistBean> newslist = bean.getNewslist();
        if (newslist != null && newslist.size() > 0) {
            if (isRefresh){
                // 缓存新闻
                SPUtils.put(GlobalParams.DATA_RECOMMEND_NEWS, new Gson().toJson(bean));
                // 保存更新时间
                SPUtils.put(GlobalParams.LAST_UPDATE_TIME, System.currentTimeMillis());

                mData.clear();
                mData.addAll(newslist);
                mNewsList.clear();
                mNewsList.addAll(newslist);
            }else{
                mData.addAll(newslist);
                mNewsList.addAll(newslist);
            }
            mPresenter.getADList();// 加载广告
        }else{
            isLoading = false;
            if (isRefresh){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
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
        isLoading = false;
        List<ListBean> list = bean.getData().get_$8050018672826551().getList();
        if (list != null && list.size() > 0){
            // 缓存广告
            if (mData != null && mData.size() == 10){
                SPUtils.put(GlobalParams.DATA_RECOMMEND_AD, new Gson().toJson(bean));
            }
            ListBean listBean = list.get(0);
            Random random = new Random();
            int index = mData.size() - 7 + random.nextInt(5);// 随机位置
            mData.add(index, listBean);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onGetADListError(Throwable e) {
        isLoading = false;
        if (isRefresh){
            mSwipeRefreshLayout.setRefreshing(false);
        }
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
            if (mData.size() > 0 && !(mData.get(0) instanceof EmptyNewsBean)){
                isLoading = true;
                isRefresh = false;
                mPresenter.getNewsRecommendList();
            }
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
        if (item instanceof RecommendBean.NewslistBean){
            RecommendBean.NewslistBean bean = (RecommendBean.NewslistBean) item;
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

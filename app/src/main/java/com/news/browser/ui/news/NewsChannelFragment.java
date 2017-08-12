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
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.download.DownloadHandler;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.news.adapter.channel.NewsChannelAdapter;
import com.news.browser.ui.news.bean.EmptyNewsBean;
import com.news.browser.ui.news.bean.NewsChannelBean;
import com.news.browser.ui.news.interfaces.OnADItemClickListener;
import com.news.browser.ui.news.mvp.channel.NewsChannelContract;
import com.news.browser.ui.news.mvp.channel.NewsChannelPresenter;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.SPUtils;
import com.news.browser.utils.UIUtils;
import com.news.browser.widget.DividerItemDecoration;
import com.orhanobut.logger.Logger;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

import static com.news.browser.utils.SPUtils.get;


/**
 * Created by zy1584 on 2017-7-26.
 */

public class NewsChannelFragment extends BaseFragment<NewsChannelPresenter> implements NewsChannelContract.View,
        LoadMoreWrapper.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener,
        MultiItemTypeAdapter.OnItemClickListener, OnADItemClickListener {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private PreferenceManager mPreferenceManager;
    private LinearLayoutManager mLayoutManager;
    private LoadMoreWrapper mAdapter;
    private List<NewsChannelBean.DataBean.ListBean.ContentBean> mNewsList = new ArrayList<>();
    private List<BaseNewsItem> mData = new ArrayList<>();
    private boolean isLoading;
    private boolean isRefresh;
    private String mChannelCode;

    public static final String CHANNEL_CODE = "channel_code";
    public static final String action_type_click = "click";

    public static final String CACHE_SUFFIX_NEWS = "_news";
    public static final String CACHE_SUFFIX_AD = "_ad";

    private final TimingHandler mTimingHandler = new TimingHandler();

    private class TimingHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg != null && msg.obj != null){
                ADResponseBean.DataBean._$8050018672826551Bean.ListBean bean = (ADResponseBean.DataBean._$8050018672826551Bean.ListBean) msg.obj;
                if (!bean.isHasExpose() && bean.isTiming()){
                    bean.setTiming(false);
                    removeCallbacksAndMessages(bean);
                    mPresenter.reportADExpose(bean);
                }
            }
        }
    }

    public static NewsChannelFragment newInstance(String channelCode) {
        NewsChannelFragment f = new NewsChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_CODE, channelCode);
        f.setArguments(bundle);
        return f;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_news;
    }

    @Override
    protected NewsChannelPresenter loadPresenter() {
        return new NewsChannelPresenter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mSwipeRefreshLayout.setColorSchemeColors(UIUtils.getColor(R.color.colorPrimary));

        NewsChannelAdapter originalAdapter = new NewsChannelAdapter(mActivity, mData, this);
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
        Bundle bundle = getArguments();
        if (bundle != null){
            mChannelCode = bundle.getString(CHANNEL_CODE);
            // 从缓存中加载
            if (!TextUtils.isEmpty(mChannelCode)){
                readFromCache();

                boolean connected = NetUtils.isConnected(mActivity);
                if (connected){
                    isLoading = true;
                    isRefresh = true;
                    mPreferenceManager = PreferenceManager.getInstance();
                    mPresenter.getNewsChannelList(0, 10, mChannelCode);
                }
            }
        }
    }

    private void readFromCache() {
        String json_news = (String) get(mChannelCode + CACHE_SUFFIX_NEWS, "");
        String json_ad = (String) SPUtils.get(mChannelCode + CACHE_SUFFIX_AD, "");

        if (!TextUtils.isEmpty(json_news)){
            NewsChannelBean newsChannelBean = new Gson().fromJson(json_news, NewsChannelBean.class);
            List<NewsChannelBean.DataBean.ListBean> originalList = newsChannelBean.getData().getList();
            List<NewsChannelBean.DataBean.ListBean.ContentBean> newsList = new ArrayList<>();
            for (NewsChannelBean.DataBean.ListBean b : originalList){
                newsList.add(b.getContent());
            }
            mData.clear();
            mData.addAll(newsList);

            mNewsList.clear();
            mNewsList.addAll(newsList);
        }

        if (!TextUtils.isEmpty(json_news) && !TextUtils.isEmpty(json_ad)){
            ADResponseBean adResponseBean = new Gson().fromJson(json_ad, ADResponseBean.class);
            List<ADResponseBean.DataBean._$8050018672826551Bean.ListBean> list = adResponseBean.getData().get_$8050018672826551().getList();
            ADResponseBean.DataBean._$8050018672826551Bean.ListBean listBean = list.get(0);
            Random random = new Random();
            int index = mData.size() - 7 + random.nextInt(5);
            Logger.t("index").e("data size :" + mData.size() + "    index:" + index);
            mData.add(index, listBean);
        }

        if (TextUtils.isEmpty(json_news) && TextUtils.isEmpty(json_ad)){
            for (int i = 0; i < 10; i++){
                mData.add(new EmptyNewsBean());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onReceiveNewsChannelList(NewsChannelBean bean) {
        List<NewsChannelBean.DataBean.ListBean.ContentBean> newsList = new ArrayList<>();
        List<NewsChannelBean.DataBean.ListBean> originalList = bean.getData().getList();
        if (originalList != null && originalList.size() > 0){

            for (NewsChannelBean.DataBean.ListBean b : originalList){
                newsList.add(b.getContent());
            }
            if (isRefresh){
                // 缓存新闻
                SPUtils.put(mChannelCode + CACHE_SUFFIX_NEWS, new Gson().toJson(bean));

                mData.clear();
                mData.addAll(newsList);

                mNewsList.clear();
                mNewsList.addAll(newsList);
            }else{
                mData.addAll(newsList);
                mNewsList.addAll(newsList);
            }
            mPresenter.getADList();// 加载广告
        }else {
            isLoading = false;
            if (isRefresh){
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onGetNewsChannelListError(Throwable e) {
        isLoading = false;
        if (isRefresh){
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onReceiveADList(ADResponseBean bean) {
        List<ADResponseBean.DataBean._$8050018672826551Bean.ListBean> list = bean.getData().get_$8050018672826551().getList();
        if (list != null && list.size() > 0){
            // 缓存广告
            if (mData.size() == 10){
                SPUtils.put(mChannelCode + CACHE_SUFFIX_AD, new Gson().toJson(bean));
            }
            ADResponseBean.DataBean._$8050018672826551Bean.ListBean listBean = list.get(0);
            Random random = new Random();
            int index = mData.size() - 7 + random.nextInt(5);
            Logger.t("index").e("data size :" + mData.size() + "    index:" + index);
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
    public void onReceiveReportClick(final ClickLinkResponseBean responseBean, ADResponseBean.DataBean._$8050018672826551Bean.ListBean listBean) {
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
                mPresenter.getNewsChannelList(mNewsList.size(), 10, mChannelCode);
            }
        }
    }

    @Override
    public void onRefresh() {
        if (!isLoading){
            isLoading = true;
            isRefresh = true;
            mPresenter.getNewsChannelList(0, 10, mChannelCode);
        }
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        BaseNewsItem item = mData.get(position);
        if (item instanceof NewsChannelBean.DataBean.ListBean.ContentBean){
            NewsChannelBean.DataBean.ListBean.ContentBean bean = (NewsChannelBean.DataBean.ListBean.ContentBean) item;
            BrowserActivity browserAct = (BrowserActivity) mActivity;
            browserAct.searchTheWeb(bean.getUrl());
            mPresenter.reportActionType(mNewsList.get(position), action_type_click);
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onADItemClick(ViewHolder holder, ADResponseBean.DataBean._$8050018672826551Bean.ListBean item, int position, int dowX, int downY, int upX, int upY) {
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

    private void notifyDataSetChanged(){
        if (mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mAdapter.notifyDataSetChanged();
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
                if (item instanceof ADResponseBean.DataBean._$8050018672826551Bean.ListBean){// 广告
                    ADResponseBean.DataBean._$8050018672826551Bean.ListBean bean = (ADResponseBean.DataBean._$8050018672826551Bean.ListBean) item;
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

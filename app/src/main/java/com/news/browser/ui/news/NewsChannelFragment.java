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
import com.news.browser.bean.ADBean;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.data.AccessRecordTool;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.download.DownloadHandler;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.news.adapter.channel.NewsChannelAdapter;
import com.news.browser.ui.news.bean.EmptyNewsBean;
import com.news.browser.ui.news.bean.NewsChannelBean;
import com.news.browser.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.news.browser.ui.news.interfaces.OnADItemClickListener;
import com.news.browser.ui.news.mvp.channel.NewsChannelContract;
import com.news.browser.ui.news.mvp.channel.NewsChannelPresenter;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.SPUtils;
import com.news.browser.utils.UIUtils;
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
    private List<ContentBean> mNewsList = new ArrayList<>();
    private List<BaseNewsItem> mData = new ArrayList<>();
    private boolean isLoading;
    private boolean isRefresh;
    private boolean isNeedClearCache;
    private String mChannelCode;
    private String mChannelName;

    public static final String CHANNEL_CODE = "channel_code";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String action_type_click = "click";

    public static final String CACHE_SUFFIX_NEWS = "_news";
    public static final String CACHE_SUFFIX_AD = "_ad";

    private final TimingHandler mTimingHandler = new TimingHandler();

    private class TimingHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg == null || msg.obj == null) return;
            switch (msg.what) {
                case WHAT_AD_THIRD:
                    ADBean bean = (ADBean) msg.obj;
                    if (!bean.isHasExpose2Third() && bean.isThirdTiming()) {
                        removeMessages(WHAT_AD_THIRD, bean);
                        mPresenter.reportADExpose(bean);
                    }
                    break;
                case WHAT_AD_SELF:
                    ADBean selfBean = (ADBean) msg.obj;
                    if (!selfBean.isHasExpose2Self() && selfBean.isSelfTiming()) {
                        removeMessages(WHAT_AD_SELF, selfBean);
                        // 自营统计上报数据：广告
                        AccessRecordTool.getInstance().reportADExpose(selfBean, mChannelName, selfBean.getTitle(), AccessRecordTool.EV_SHOW, AccessRecordTool.TYPE_AD);
                    }
                    break;
            }
        }
    }

    public static NewsChannelFragment newInstance(String channelCode, String channelName) {
        NewsChannelFragment f = new NewsChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_CODE, channelCode);
        bundle.putString(CHANNEL_NAME, channelName);
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
        mRecyclerView.addItemDecoration(getDefaultDivider());
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
        if (bundle != null) {
            mChannelCode = bundle.getString(CHANNEL_CODE);
            mChannelName = bundle.getString(CHANNEL_NAME);
            // 从缓存中加载
            if (!TextUtils.isEmpty(mChannelCode)) {
                readFromCache();

                boolean connected = NetUtils.isConnected(mActivity);
                if (connected) {
                    isLoading = true;
                    isRefresh = true;
                    mPreferenceManager = PreferenceManager.getInstance();
                    isNeedClearCache = true;
                    mPresenter.getNewsChannelList(0, 10, mChannelCode);
                }
            }
        }
    }

    private void readFromCache() {
        String json_news = (String) get(mChannelCode + CACHE_SUFFIX_NEWS, "");
        String json_ad = (String) SPUtils.get(mChannelCode + CACHE_SUFFIX_AD, "");

        if (!TextUtils.isEmpty(json_news)) {
            NewsChannelBean newsChannelBean = new Gson().fromJson(json_news, NewsChannelBean.class);
            List<NewsChannelBean.DataBean.ListBean> originalList = newsChannelBean.getData().getList();
            List<ContentBean> newsList = new ArrayList<>();
            for (NewsChannelBean.DataBean.ListBean b : originalList) {
                newsList.add(b.getContent());
            }
            mData.clear();
            mData.addAll(newsList);

            mNewsList.clear();
            mNewsList.addAll(newsList);
        }

        if (!TextUtils.isEmpty(json_news) && !TextUtils.isEmpty(json_ad)) {
            ADBean listBean = new Gson().fromJson(json_ad, ADBean.class);
            Random random = new Random();
            int index = mData.size() - 7 + random.nextInt(5);
            Logger.t("index").e("data size :" + mData.size() + "    index:" + index);
            mData.add(index, listBean);
        }

        if (TextUtils.isEmpty(json_news) && TextUtils.isEmpty(json_ad)) {
            for (int i = 0; i < 10; i++) {
                mData.add(new EmptyNewsBean());
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onReceiveNewsChannelList(NewsChannelBean bean) {
        List<ContentBean> newsList = new ArrayList<>();
        List<NewsChannelBean.DataBean.ListBean> originalList = bean.getData().getList();
        if (originalList != null && originalList.size() > 0) {
            for (NewsChannelBean.DataBean.ListBean b : originalList) {
                newsList.add(b.getContent());
            }
            if (isRefresh) {
                if ((mData.size() == 10 && mData.get(0) instanceof EmptyNewsBean) || isNeedClearCache){
                    mData.clear();
                    mNewsList.clear();
                    isNeedClearCache = false;
                }
                // 缓存新闻
                if (newsList.size() > 0){
                    SPUtils.put(mChannelCode + CACHE_SUFFIX_NEWS, new Gson().toJson(bean));
                }

                mData.addAll(0, newsList);
                mNewsList.addAll(newsList);
            } else {
                mData.addAll(newsList);
                mNewsList.addAll(newsList);
            }
            mPresenter.getADList();// 加载广告
        } else {
            isLoading = false;
            stopRefresh();
        }
    }

    @Override
    public void onGetNewsChannelListError(Throwable e) {
        isLoading = false;
        stopRefresh();
    }

    @Override
    public void onReceiveADList(ADBean bean) {
        isLoading = false;
        int index;
        Random random = new Random();
        if (isRefresh){
            // 缓存广告
            SPUtils.put(mChannelCode + CACHE_SUFFIX_AD, new Gson().toJson(bean));
            index = 3 + random.nextInt(5);
        }else{
            index = mData.size() - 7 + random.nextInt(5);
        }
        Logger.t("index").e("data size :" + mData.size() + "    index:" + index);
        mData.add(index, bean);
        notifyDataSetChanged();
    }

    @Override
    public void onGetADListError(Throwable e) {
        isLoading = false;
        notifyDataSetChanged();
    }

    private void stopRefresh() {
        if (isRefresh) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onReceiveReportClick(final ClickLinkResponseBean responseBean, ADBean listBean) {
        final String clickid = responseBean.getData().getClickid();
        final String dstlink = responseBean.getData().getDstlink();
        final String conversion_link = listBean.getConversion_link();
        String fileName = URLUtil.guessFileName(dstlink, null, null);
        DownloadHandler.promptDownload(mActivity, mPreferenceManager, fileName, dstlink, null, null,
                null, clickid, conversion_link);
    }

    @Override
    public void onLoadMoreRequested() {
        if (!isLoading) {
            if (mData.size() > 0 && !(mData.get(0) instanceof EmptyNewsBean)) {
                isLoading = true;
                isRefresh = false;
                mPresenter.getNewsChannelList(mNewsList.size(), 10, mChannelCode);
            }
        }
    }

    @Override
    public void onRefresh() {
        if (!isLoading) {
            isLoading = true;
            isRefresh = true;
            mPresenter.getNewsChannelList(mNewsList.size(), 10, mChannelCode);
        }
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        BaseNewsItem item = mData.get(position);
        if (item instanceof ContentBean) {
            ContentBean bean = (ContentBean) item;
            BrowserActivity browserAct = (BrowserActivity) mActivity;
            browserAct.searchTheWeb(bean.getUrl());
            mPresenter.reportActionType(bean, action_type_click);
            AccessRecordTool.getInstance().reportADOrNewsClick(mChannelName, bean.getTitle(), AccessRecordTool.EV_CLICK, AccessRecordTool.TYPE_NEWS);
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onADItemClick(ViewHolder holder, ADBean item, int position, int dowX, int downY, int upX, int upY) {
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
        if (interact_type == 0) {
            BrowserActivity browserAct = (BrowserActivity) mActivity;
            browserAct.searchTheWeb(url);
        } else if (interact_type == 1) {
            mPresenter.reportClick(url, item);
        }
        AccessRecordTool.getInstance().reportADOrNewsClick(mChannelName, item.getTitle(), AccessRecordTool.EV_CLICK, AccessRecordTool.TYPE_AD);
    }

    private void notifyDataSetChanged() {
        stopRefresh();
        mAdapter.notifyDataSetChanged();
    }

    private static final int WHAT_AD_THIRD = 1;
    private static final int WHAT_AD_SELF = 2;

    private class OnRVScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mData.size() == 0) return;
            if (!isCurrentFragment()) return;
            int first = mLayoutManager.findFirstVisibleItemPosition();
            int last = mLayoutManager.findLastVisibleItemPosition();
            for (int i = first; i <= last; i++) {
                if (i >= mData.size()) continue;// footer
                BaseNewsItem item = mData.get(i);
                if (item instanceof ADBean) {// 广告
                    ADBean bean = (ADBean) item;
                    if (!bean.isHasExpose2Third()) {
                        View childAt = mLayoutManager.getChildAt(i - first);
                        int percents = bean.getVisibilityPercents(childAt);
                        if (percents >= 50) {
                            if (!bean.isThirdTiming()) {
                                Message msg = mTimingHandler.obtainMessage();
                                if (msg != null) {
                                    msg.obj = bean;
                                    msg.what = WHAT_AD_THIRD;
                                    bean.setThirdTiming(true);
                                    mTimingHandler.sendMessageDelayed(msg, 1000);
                                }
                            }
                        } else {
                            mTimingHandler.removeCallbacksAndMessages(bean);
                        }
                    }
                    // 自营统计
                    if (!bean.isHasExpose2Self()) {
                        View childAt = mLayoutManager.getChildAt(i - first);
                        int percents = bean.getVisibilityPercents(childAt);
                        if (percents >= 50) {
                            if (!bean.isSelfTiming()) {
                                Message msg = mTimingHandler.obtainMessage();
                                if (msg != null) {
                                    msg.obj = bean;
                                    msg.what = WHAT_AD_SELF;
                                    bean.setSelfTiming(true);
                                    mTimingHandler.sendMessageDelayed(msg, 1000);
                                }
                            }
                        } else {
                            mTimingHandler.removeCallbacksAndMessages(bean);
                        }
                    }

                } else if (item instanceof ContentBean) {

                }
            }
        }
    }

}

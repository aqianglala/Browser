package com.example.zy1584.mybase.ui.news;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.ui.main.BrowserActivity;
import com.example.zy1584.mybase.ui.news.adapter.channel.NewsChannelAdapter;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.example.zy1584.mybase.ui.news.mvp.channel.NewsChannelContract;
import com.example.zy1584.mybase.ui.news.mvp.channel.NewsChannelPresenter;
import com.example.zy1584.mybase.utils.UIUtils;
import com.example.zy1584.mybase.widget.DividerItemDecoration;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class NewsChannelFragment extends BaseFragment<NewsChannelPresenter> implements NewsChannelContract.View,
        LoadMoreWrapper.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener,
        MultiItemTypeAdapter.OnItemClickListener {

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private LoadMoreWrapper mAdapter;
    private List<ContentBean> mNewsList = new ArrayList<>();
    private boolean isLoading;
    private boolean isRefresh;
    private String mChannelCode;

    public static final String CHANNEL_CODE = "channel_code";
    public static final String action_type_click = "click";

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

        NewsChannelAdapter originalAdapter = new NewsChannelAdapter(mActivity, mNewsList);
        originalAdapter.setOnItemClickListener(this);
        mAdapter = new LoadMoreWrapper(originalAdapter);
        mAdapter.setLoadMoreView(R.layout.layout_load_more);
        mAdapter.setOnLoadMoreListener(this);

        initRecyclerView(mRecyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mAdapter);
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
            isLoading = true;
            isRefresh = true;
            mPresenter.getNewsChannelList(0, 10, mChannelCode);
        }
    }

    @Override
    public void onSuccess(NewsChannelBean bean) {
        isLoading = false;
        if (bean == null) return;
        List<ListBean> originalList = bean.getData().getList();
        List<ContentBean> newslist = new ArrayList<>();
        for (ListBean b : originalList){
            newslist.add(b.getContent());
        }
        if (newslist == null) return;

        if (isRefresh){
            mNewsList.clear();
            mNewsList.addAll(newslist);
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        }else{
            mNewsList.addAll(newslist);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onError(Throwable e) {
        isLoading = false;
        if (isRefresh){
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoadMoreRequested() {
        if (!isLoading){
            isLoading = true;
            isRefresh = false;
            mPresenter.getNewsChannelList(mNewsList.size(), 10, mChannelCode);
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
        String url = mNewsList.get(position).getUrl();
        BrowserActivity browserAct = (BrowserActivity) mActivity;
        browserAct.searchTheWeb(url);
        mPresenter.reportActionType(mNewsList.get(position), action_type_click);
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }
}

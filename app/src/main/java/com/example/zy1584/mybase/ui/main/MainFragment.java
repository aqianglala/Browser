package com.example.zy1584.mybase.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.zy1584.mybase.BuildConfig;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.bean.HomeNavigationBean;
import com.example.zy1584.mybase.ui.main.adapter.TestFragmentAdapter;
import com.example.zy1584.mybase.ui.main.bean.ChannelBean;
import com.example.zy1584.mybase.ui.main.mvp.MainFrgContract;
import com.example.zy1584.mybase.ui.main.mvp.MainFrgPresenter;
import com.example.zy1584.mybase.ui.news.NewsChannelFragment;
import com.example.zy1584.mybase.ui.news.NewsRecommendFragment;
import com.example.zy1584.mybase.utils.DensityUtils;
import com.example.zy1584.mybase.utils.FileUtils;
import com.example.zy1584.mybase.utils.GlobalParams;
import com.example.zy1584.mybase.utils.LocationUtils;
import com.example.zy1584.mybase.utils.SPUtils;
import com.example.zy1584.mybase.widget.GridSpacingItemDecoration;
import com.example.zy1584.mybase.widget.NewsViewPager;
import com.example.zy1584.mybase.widget.behavior.uc.UcNewsHeaderPagerBehavior;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.functions.Action1;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class MainFragment extends BaseFragment<MainFrgPresenter> implements UcNewsHeaderPagerBehavior.OnPagerStateListener,
        MainFrgContract.View{
    private static final int REQUEST_QR_CODE = 1;

    private NewsViewPager mNewsPager;
    private TabLayout mTableLayout;
    private List<BaseFragment> mFragments = new ArrayList<>();
    private UcNewsHeaderPagerBehavior mPagerBehavior;
    private TestFragmentAdapter mNewsPagerAdapter;

    private List<HomeNavigationBean.DataBean> mNavigationList = new ArrayList<>();

    private NavigationAdapter mNavigationAdapter;

    @BindView(R.id.rv_navigation)
    RecyclerView rv_navigation;

    @OnClick(R.id.iv_qr_code)
    void openQrcode(){
        BrowserActivity browserActivity = (BrowserActivity) mActivity;
        browserActivity.openQrcode();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected MainFrgPresenter loadPresenter() {
        return new MainFrgPresenter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mPagerBehavior = (UcNewsHeaderPagerBehavior) ((CoordinatorLayout.LayoutParams) mContentView.findViewById(R.id.id_uc_news_header_pager).getLayoutParams()).getBehavior();
        mPagerBehavior.setPagerStateListener(this);
        mNewsPager = (NewsViewPager) mContentView.findViewById(R.id.id_uc_news_content);
        mTableLayout = (TabLayout) mContentView.findViewById(R.id.id_uc_news_tab);

        mFragments.add(new NewsRecommendFragment());
        mTableLayout.addTab(mTableLayout.newTab().setText("推荐"));
        mTableLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTableLayout.addOnTabSelectedListener(new TabSelectedListener());
        mNewsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTableLayout));
        mNewsPagerAdapter = new TestFragmentAdapter(mFragments, getFragmentManager());
        mNewsPager.setAdapter(mNewsPagerAdapter);

        initNavigation();
    }

    private void initNavigation() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 6);
        rv_navigation.setLayoutManager(gridLayoutManager);
        rv_navigation.setHasFixedSize(true);
        rv_navigation.addItemDecoration(new GridSpacingItemDecoration(6, DensityUtils.dpToPx(10), true));
    }

    @Override
    protected void setListener() {
        super.setListener();
        mContentView.findViewById(R.id.iv_search).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_hint).setOnClickListener(this);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mPresenter.getChannelList();
        String data_navigation = (String) SPUtils.get(GlobalParams.DATA_NAVIGATION, "");
        if (TextUtils.isEmpty(data_navigation)){
            mPresenter.getHomeNavigationList();
        }else{
            HomeNavigationBean bean = new Gson().fromJson(data_navigation, HomeNavigationBean.class);
            setNavigationData(bean);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.iv_search:
            case R.id.tv_hint:
                ((BrowserActivity)mActivity).jumpToSearch();
                toast("进入搜索页！");
                break;
        }
    }

    @Override
    public void onPagerClosed() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPagerClosed: ");
        }
        mNewsPager.setPagingEnabled(true);
    }

    @Override
    public void onPagerOpened() {
        mNewsPager.setPagingEnabled(false);
    }

    public boolean onBackPressed() {
        if (mPagerBehavior != null && mPagerBehavior.isClosed()) {
            mPagerBehavior.openPager();
            return true;
        }
        return false;
    }


    @Override
    public void onReceiveChannelList(Map<String, ChannelBean> channelMap) {
        if (channelMap.size() == 0) return;
        String[] defaultChannels = getResources().getStringArray(R.array.default_channel);
        List<String> channels = new LinkedList(Arrays.asList(defaultChannels));
        String province = (String) SPUtils.get(LocationUtils.PROVINCE, "");
        String city = (String) SPUtils.get(LocationUtils.CITY, "");
        if (!TextUtils.isEmpty(province)){
            for (ChannelBean bean : channelMap.values()){
                if (bean == null) continue;
                if (city.contains(bean.getChanName()) || province.contains(bean.getChanName())){
                    channels.add(2, bean.getChanId());
                    break;
                }
            }
        }
        for (String key : channels){
            ChannelBean bean = channelMap.get(key);
            if (bean != null){
                NewsChannelFragment fragment = NewsChannelFragment.newInstance(bean.getChanCode());
                mFragments.add(fragment);
                mTableLayout.addTab(mTableLayout.newTab().setText(bean.getChanName()));
            }
        }
        mNewsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetChannelListError(Throwable e) {

    }

    @Override
    public void onReceiveNavigationList(HomeNavigationBean bean) {
        // 缓存数据
        String json = new Gson().toJson(bean);
        SPUtils.put(GlobalParams.DATA_NAVIGATION, json);

        setNavigationData(bean);

    }

    private void setNavigationData(HomeNavigationBean bean) {
        mNavigationList.clear();
        mNavigationList.addAll(bean.getData());
        if (mNavigationAdapter == null){
            mNavigationAdapter = new NavigationAdapter(mActivity, R.layout.item_list_navigatioin, mNavigationList);
            rv_navigation.setAdapter(mNavigationAdapter);
            mNavigationAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    HomeNavigationBean.DataBean dataBean = mNavigationList.get(position);
                    BrowserActivity browserAct = (BrowserActivity) mActivity;
                    browserAct.searchTheWeb(dataBean.getAddrUrl());
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
        }else{
            mNavigationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetHomeNavigationListError(Throwable e) {
        FileUtils.loadFile(mActivity, FileUtils.HOME_NAVIGATION_FILE_NAME)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        HomeNavigationBean bean = new Gson().fromJson(s, HomeNavigationBean.class);
                        setNavigationData(bean);
                    }
                });
    }

    class TabSelectedListener implements TabLayout.OnTabSelectedListener {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mNewsPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    class NavigationAdapter extends CommonAdapter<HomeNavigationBean.DataBean>{

        public NavigationAdapter(Context context, int layoutId, List<HomeNavigationBean.DataBean> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(ViewHolder holder, HomeNavigationBean.DataBean dataBean, int position) {
            holder.setText(R.id.tv_name, dataBean.getName());
            ImageView iv_icon = holder.getView(R.id.iv_icon);
            Glide.with(mActivity).load(dataBean.getIconUrl()).into(iv_icon);
        }
    }
}

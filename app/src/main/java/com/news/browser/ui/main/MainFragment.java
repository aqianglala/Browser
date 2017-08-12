package com.news.browser.ui.main;

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
import com.google.gson.Gson;
import com.news.browser.BuildConfig;
import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.HomeNavigationBean;
import com.news.browser.ui.main.adapter.TestFragmentAdapter;
import com.news.browser.ui.main.bean.ChannelBean;
import com.news.browser.ui.main.mvp.MainFrgContract;
import com.news.browser.ui.main.mvp.MainFrgPresenter;
import com.news.browser.ui.news.NewsChannelFragment;
import com.news.browser.ui.news.NewsRecommendFragment;
import com.news.browser.utils.DensityUtils;
import com.news.browser.utils.FileUtils;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.LocationUtils;
import com.news.browser.utils.SPUtils;
import com.news.browser.widget.GridSpacingItemDecoration;
import com.news.browser.widget.NewsViewPager;
import com.news.browser.widget.behavior.uc.UcNewsHeaderPagerBehavior;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
        MainFrgContract.View {
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
        getChannelList();
        getNavigation();
    }

    /**
     * 网络--本地
     */
    private void getNavigation() {
        mPresenter.getHomeNavigationList();
    }

    /**
     * 缓存--网络--本地文件
     */
    private void getChannelList() {
        String jsonStr = (String) SPUtils.get(GlobalParams.DATA_CHANNEL, "");
        if (TextUtils.isEmpty(jsonStr)){
            mPresenter.getChannelList();
        }else{
            Map<String, ChannelBean> beanMap = parseChannelListJson(jsonStr);
            setChannelList(beanMap);
        }
    }

    private Map<String, ChannelBean> parseChannelListJson(String jsonStr) {
        Map<String, ChannelBean> result = new HashMap();
        try {
            JSONObject object = new JSONObject(jsonStr);

            JSONObject data = object.getJSONObject("data");
            Iterator<String> iterator = data.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                JSONObject obj = data.getJSONObject(key);
                String chanCode = obj.optString("chanCode");
                String chanId = obj.optString("chanId");
                String chanName = obj.optString("chanName");
                String chanName_m = obj.optString("chanName_m");
                String isKB = obj.optString("isKB");
                ChannelBean bean = new ChannelBean(chanCode, chanId, chanName, chanName_m, isKB);
                result.put(key, bean);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
        setChannelList(channelMap);
    }

    private void setChannelList(Map<String, ChannelBean> channelMap) {
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

    /**
     * 加载失败则从本地文件中读取
     * @param e
     */
    @Override
    public void onGetChannelListError(Throwable e) {
        FileUtils.loadFile(mActivity, FileUtils.CHANNEL_LIST_FILE_NAME)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Map<String, ChannelBean> beanMap = parseChannelListJson(s);
                        setChannelList(beanMap);
                    }
                });
    }

    @Override
    public void onReceiveNavigationList(HomeNavigationBean bean) {
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

    /**
     * 加载失败则从本地文件中读取
     * @param e
     */
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

package com.example.zy1584.mybase.ui.main;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.zy1584.mybase.BuildConfig;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.ui.main.adapter.TestFragmentAdapter;
import com.example.zy1584.mybase.ui.main.bean.ChannelBean;
import com.example.zy1584.mybase.ui.main.mvp.MainFrgContract;
import com.example.zy1584.mybase.ui.main.mvp.MainFrgPresenter;
import com.example.zy1584.mybase.ui.news.NewsChannelFragment;
import com.example.zy1584.mybase.ui.news.NewsRecommendFragment;
import com.example.zy1584.mybase.utils.LocationUtils;
import com.example.zy1584.mybase.utils.SPUtils;
import com.example.zy1584.mybase.widget.NewsViewPager;
import com.example.zy1584.mybase.widget.behavior.uc.UcNewsHeaderPagerBehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class MainFragment extends BaseFragment<MainFrgPresenter> implements UcNewsHeaderPagerBehavior.OnPagerStateListener,
        MainFrgContract.View{
    private NewsViewPager mNewsPager;
    private TabLayout mTableLayout;
    private List<BaseFragment> mFragments = new ArrayList<>();
    private UcNewsHeaderPagerBehavior mPagerBehavior;

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
        Snackbar.make(mNewsPager, "pager closed", Snackbar.LENGTH_SHORT).show();
        mNewsPager.setPagingEnabled(true);
    }

    @Override
    public void onPagerOpened() {
        Snackbar.make(mNewsPager, "pager opened", Snackbar.LENGTH_SHORT).show();
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
        mTableLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTableLayout.addOnTabSelectedListener(new TabSelectedListener());
        mNewsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTableLayout));
        mNewsPager.setAdapter(new TestFragmentAdapter(mFragments, getFragmentManager()));
    }

    @Override
    public void onGetChannelListError(Throwable e) {

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

}

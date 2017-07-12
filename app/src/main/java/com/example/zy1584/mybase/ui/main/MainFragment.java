package com.example.zy1584.mybase.ui.main;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.View;

import com.example.zy1584.mybase.BuildConfig;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.ui.main.adapter.TestFragmentAdapter;
import com.example.zy1584.mybase.ui.news.NewsListFragment;
import com.example.zy1584.mybase.widget.NewsViewPager;
import com.example.zy1584.mybase.widget.behavior.uc.UcNewsHeaderPagerBehavior;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class MainFragment extends BaseFragment implements UcNewsHeaderPagerBehavior.OnPagerStateListener {
    private NewsViewPager mNewsPager;
    private TabLayout mTableLayout;
    private List<NewsListFragment> mFragments;
    private UcNewsHeaderPagerBehavior mPagerBehavior;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mPagerBehavior = (UcNewsHeaderPagerBehavior) ((CoordinatorLayout.LayoutParams) mContentView.findViewById(R.id.id_uc_news_header_pager).getLayoutParams()).getBehavior();
        mPagerBehavior.setPagerStateListener(this);
        mNewsPager = (NewsViewPager) mContentView.findViewById(R.id.id_uc_news_content);
        mTableLayout = (TabLayout) mContentView.findViewById(R.id.id_uc_news_tab);
        mFragments = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mFragments.add(NewsListFragment.newInstance(String.valueOf(i), false));
            mTableLayout.addTab(mTableLayout.newTab().setText("Tab" + i));
        }
        mTableLayout.setTabMode(TabLayout.MODE_FIXED);
        mTableLayout.addOnTabSelectedListener(new TabSelectedListener());
        mNewsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTableLayout));
        mNewsPager.setAdapter(new TestFragmentAdapter(mFragments, getFragmentManager()));
    }

    @Override
    protected void setListener() {
        super.setListener();
        mContentView.findViewById(R.id.iv_search).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_hint).setOnClickListener(this);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {

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

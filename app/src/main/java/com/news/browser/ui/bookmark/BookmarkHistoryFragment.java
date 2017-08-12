package com.news.browser.ui.bookmark;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.base.BasePresenter;
import com.news.browser.widget.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;



public class BookmarkHistoryFragment extends BaseFragment {

    private List<BaseFragment> mFragments = new ArrayList<>();

    private BookmarkFragment mBookmarkFragment;
    private HistoryFragment mHistoryFragment;
    private FragmentPagerAdapter mPagerAdapter;

    @BindView(R.id.viewPager)
    CustomViewPager mViewPager;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.tv_edit)
    TextView tv_edit;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.iv_back)
    ImageView iv_back;

    @OnClick(R.id.tv_edit)
    void edit(){
        String text = tv_edit.getText().toString();
        int currentItem = mViewPager.getCurrentItem();
        BaseControlView currentFragment = (BaseControlView) mFragments.get(currentItem);
        if (text.equals("编辑")){
            tv_edit.setText("全选");
            setTabLayoutCanClick(false);
            mViewPager.setPagingEnabled(false);

            iv_back.setVisibility(View.GONE);
            tv_cancel.setVisibility(View.VISIBLE);

            currentFragment.setEditable(true);
            currentFragment.unSelectAll();
        }else if (text.equals("全选")){
            tv_edit.setText("取消全选");
            currentFragment.selectAll();
        }else if (text.equals("取消全选")){
            tv_edit.setText("全选");
            currentFragment.unSelectAll();
        }
    }

    @OnClick(R.id.iv_back)
    void back(){
        mActivity.getSupportFragmentManager().popBackStack();
    }

    @OnClick(R.id.tv_cancel)
    void cancel(){
        tv_edit.setText("编辑");
        iv_back.setVisibility(View.VISIBLE);
        tv_cancel.setVisibility(View.GONE);

        setTabLayoutCanClick(true);
        mViewPager.setPagingEnabled(true);

        int currentItem = mViewPager.getCurrentItem();
        BaseControlView currentFragment = (BaseControlView) mFragments.get(currentItem);
        currentFragment.setEditable(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bookmark_history;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mTabLayout.addTab(mTabLayout.newTab().setText("书签"));
        mTabLayout.addTab(mTabLayout.newTab().setText("历史"));
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.addOnTabSelectedListener(new TabSelectedListener());

        mBookmarkFragment = new BookmarkFragment();
        mHistoryFragment = new HistoryFragment();
        mFragments.add(mBookmarkFragment);
        mFragments.add(mHistoryFragment);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
    }

    public void setTabLayoutCanClick(boolean canClick){
        LinearLayout tabStrip= (LinearLayout) mTabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            if(tabView !=null){
                tabView.setClickable(canClick);
            }
        }
    }

    class TabSelectedListener implements TabLayout.OnTabSelectedListener {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    class FragmentPagerAdapter extends FragmentStatePagerAdapter{

        public FragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}

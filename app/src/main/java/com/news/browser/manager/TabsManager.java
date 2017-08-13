package com.news.browser.manager;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.main.BrowserFragment;
import com.news.browser.ui.main.view.LightningViewTitle;

import java.util.ArrayList;

/**
 * Created by zy1584 on 2017-7-15.
 */

public class TabsManager {
    private ArrayList<BrowserFragment> mTabList = new ArrayList<>();
    private TabNumberChangedListener mTabNumberListener;
    private BrowserActivity mActivity;
    private BrowserFragment mCurrentTab;
    private int mCurrentIndex;
    private LightningViewTitle mHomeTitleInfo;

    public TabsManager(BrowserActivity activity) {
        mTabList.add(null);
        mActivity = activity;
        mHomeTitleInfo = new LightningViewTitle(mActivity);
    }

    public synchronized BrowserFragment newTab(String url, boolean isIncognito, boolean show){
        BrowserFragment fragment = null;
        if (TextUtils.isEmpty(url)){// 隐藏所有的BrowserFragment
            hideAllBrowserFrg();
            mTabList.add(null);
        }else{
            fragment = BrowserFragment.newInstance(url, isIncognito);
            mTabList.add(fragment);
            mActivity.add(fragment);
        }
        if (show) {
            switchToTab(mTabList.size() - 1);
        }
        if (mTabNumberListener != null){
            mTabNumberListener.tabNumberChanged(mTabList.size());
        }
        return fragment;
    }

    /**
     * 如果删除当前的tab，则切换到前一个tab
     * 如果删除的不是当前的tab，则切换到当前tab
     * @param position
     */
    public synchronized void deleteTab(int position){
        BrowserFragment remove = mTabList.remove(position);
        mActivity.remove(remove);
        if (mCurrentIndex == position){
            if (position == 0 && mTabList.size() > 0){
                switchToTab(position);
            }else if (position > 0){
                switchToTab(position - 1);
            }
        }
    }

    public synchronized void clearAllTab(){
        for (BrowserFragment f: mTabList){
            mActivity.remove(f);
        }
        mTabList.clear();
        mCurrentIndex = 0;
        mCurrentTab = null;
    }

    public synchronized void switchToTab(int position){
        BrowserFragment fragment = mTabList.get(position);
        if (mCurrentIndex == position) return;

        hideAllBrowserFrg();
        if (fragment != null){
            mActivity.show(fragment);
            if (fragment.isCreated()){
                fragment.updateProgress(fragment.getProgress());
                fragment.updateUrl(fragment.getUrl(), true);
            }
            mActivity.setBackButtonEnabled(fragment.canGoBack());
            mActivity.setForwardButtonEnabled(fragment.canGoForward());
        }
        mCurrentTab = mTabList.get(position);
        mCurrentIndex = position;
    }

    public synchronized void updateCurrentTab(BrowserFragment fragment){
        if (fragment == null) return;
        mTabList.set(mCurrentIndex, fragment);
        mCurrentTab = fragment;
    }

    public synchronized void updateTabList(BrowserFragment fragment){
        if (fragment == null) return;
        int index = mTabList.indexOf(fragment);
        if (index != -1){
            mTabList.set(index, null);
            if (index == mCurrentIndex){
                mCurrentTab = null;
            }
        }
    }

    @Nullable
    public synchronized BrowserFragment getCurrentTab() {
        return mCurrentTab;
    }

    public synchronized int indexOfCurrentTab() {
        return mTabList.indexOf(mCurrentTab);
    }

    public void setTabNumberChangedListener(@Nullable TabNumberChangedListener listener) {
        mTabNumberListener = listener;
    }

    public interface TabNumberChangedListener {
        void tabNumberChanged(int newNumber);
    }

    public int size(){
        return mTabList.size();
    }

    public ArrayList<BrowserFragment> getmTabList() {
        return mTabList;
    }

    public void hideAllBrowserFrg(){
        for (BrowserFragment fragment : mTabList) {
            mActivity.hide(fragment);
        }
    }

    public void resumeAll() {
        for (BrowserFragment fragment : mTabList) {
            if (fragment != null){
                fragment.resumeTimers();
            }
        }
    }

    public void pauseAll() {
        for (BrowserFragment fragment : mTabList) {
            if (fragment != null){
                fragment.pauseTimers();
            }
        }
    }

    public void setShot(Bitmap bitmap){
        if (mCurrentTab == null){
            mHomeTitleInfo.setmShot(bitmap);
        }else{
            mCurrentTab.getTitleInfo().setmShot(bitmap);
        }
    }

    public LightningViewTitle getmHomeTitleInfo() {
        return mHomeTitleInfo;
    }
}

package com.news.browser.manager;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.news.browser.bus.RXEvent;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.main.BrowserFragment;
import com.news.browser.ui.main.view.BrowserViewTitle;
import com.news.browser.utils.RxBus;

import java.util.ArrayList;

/**
 * Created by zy1584 on 2017-7-15.
 */

public class TabsManager {
    private ArrayList<BrowserFragment> mTabList = new ArrayList<>();
    private TabNumberChangedListener mTabNumberListener;
    private BrowserActivity mActivity;
    private BrowserFragment mCurrentFragment;
    private int mCurrentIndex;
    private BrowserViewTitle mHomeTitleInfo;

    public TabsManager(BrowserActivity activity) {
        mTabList.add(null);
        mActivity = activity;
        mHomeTitleInfo = new BrowserViewTitle(mActivity);
    }

    public synchronized BrowserFragment newTab(String url, boolean isIncognito, boolean show) {
        BrowserFragment fragment = null;
        if (TextUtils.isEmpty(url)) {// 隐藏所有的BrowserFragment
            hideAllBrowserFrg();
            mTabList.add(null);
        } else {
            fragment = BrowserFragment.newInstance(url, isIncognito);
            mTabList.add(fragment);
            mActivity.addBrowserFragment(fragment);
        }
        RxBus.getInstance().post(new RXEvent(RXEvent.TAG_UPDATE_TAB_SIZE, null));

        if (show) {
            switchToTab(mTabList.size() - 1);
        }
        return fragment;
    }

    /**
     * 如果删除当前的tab，则切换到前一个tab
     * 如果删除的不是当前的tab，则切换到当前tab
     *
     * @param position
     */
    public synchronized void deleteTab(int position) {
        BrowserFragment remove = mTabList.remove(position);
        if (mTabList.size() == 0) {
            mTabList.add(null);
        }
        RxBus.getInstance().post(new RXEvent(RXEvent.TAG_UPDATE_TAB_SIZE, null));
        mActivity.remove(remove);
        if (mCurrentIndex == position) {
            if (position == 0 && mTabList.size() > 0) {
                switchToTab(position);
            } else if (position > 0) {
                switchToTab(position - 1);
            }
        }
    }

    public synchronized void clearAllTab() {
        for (BrowserFragment f : mTabList) {
            mActivity.remove(f);
        }
        mTabList.clear();
        mCurrentIndex = 0;
        mCurrentFragment = null;
        mTabList.add(null);
        RxBus.getInstance().post(new RXEvent(RXEvent.TAG_UPDATE_TAB_SIZE, null));
        if (mTabNumberListener != null) {
            mTabNumberListener.tabNumberChanged(0);
        }
    }

    public synchronized void switchToTab(int position) {
        BrowserFragment fragment = mTabList.get(position);
        if (mCurrentIndex == position) return;
        BrowserFragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            pauseFragment(currentFragment);
        }

        hideAllBrowserFrg();
        if (fragment != null) {
            mActivity.showBrowserFragment(fragment);
            if (fragment.isCreated()) {
                resumeFragment(fragment);
            }
            mActivity.setBackButtonEnabled(fragment.canGoBack());
            mActivity.setForwardButtonEnabled(fragment.canGoForward());
        }
        mCurrentFragment = mTabList.get(position);
        mCurrentIndex = position;
        if (mTabNumberListener != null) {
            mTabNumberListener.tabNumberChanged(position);
        }
    }

    private void resumeFragment(BrowserFragment currentTab) {
        currentTab.resumeTimers();
        currentTab.onResume();
        currentTab.updateUrl(currentTab.getUrl(), true);
        currentTab.updateProgress(currentTab.getProgress());
    }

    private void pauseFragment(BrowserFragment currentTab) {
        currentTab.pauseTimers();
        currentTab.onPause();
    }

    /**
     * 当前窗口中浏览
     *
     * @param fragment
     */
    public synchronized void updateCurrentTab(BrowserFragment fragment) {
        if (fragment == null) return;
        mTabList.set(mCurrentIndex, fragment);
        mCurrentFragment = fragment;
    }

    /**
     * 回到主页，将当前tab置为null
     *
     * @param fragment
     */
    public synchronized void backToHome(BrowserFragment fragment) {
        if (fragment == null) return;
        int index = mTabList.indexOf(fragment);
        if (index != -1) {
            mTabList.set(index, null);
            if (index == mCurrentIndex) {
                mCurrentFragment = null;
            }
        }
    }

    @Nullable
    public synchronized BrowserFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public synchronized int getCurrentIndex() {
        return mCurrentIndex;
    }

    public synchronized int indexOfCurrentTab() {
        return mTabList.indexOf(mCurrentFragment);
    }

    public void setTabNumberChangedListener(@Nullable TabNumberChangedListener listener) {
        mTabNumberListener = listener;
    }

    public interface TabNumberChangedListener {
        void tabNumberChanged(int newNumber);
    }

    public int size() {
        return mTabList.size();
    }

    public ArrayList<BrowserFragment> getTabList() {
        return mTabList;
    }

    public void hideAllBrowserFrg() {
        for (BrowserFragment fragment : mTabList) {
            mActivity.hideBrowserFragment(fragment);
        }
    }

    public void resumeAll() {
        for (BrowserFragment fragment : mTabList) {
            if (fragment != null) {
                fragment.resumeTimers();
            }
        }
    }

    public void pauseAll() {
        for (BrowserFragment fragment : mTabList) {
            if (fragment != null) {
                fragment.pauseTimers();
            }
        }
    }

    public void setShot(Bitmap bitmap) {
        if (mCurrentFragment == null) {
            mHomeTitleInfo.setmShot(bitmap);
        } else {
            mCurrentFragment.getTitleInfo().setmShot(bitmap);
        }
    }

    public BrowserViewTitle getHomeTitleInfo() {
        return mHomeTitleInfo;
    }
}

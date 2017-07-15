package com.example.zy1584.mybase.manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.example.zy1584.mybase.ui.main.BrowserActivity;
import com.example.zy1584.mybase.ui.main.BrowserFragment;
import com.example.zy1584.mybase.utils.GlobalParams;

import java.util.HashMap;

/**
 * Created by zy1584 on 2017-7-15.
 */

public class TabsManager {
    private HashMap<Integer, Fragment> mTabsMap = new HashMap<>();
    private TabNumberChangedListener mTabNumberListener;
    private BrowserActivity mActivity;

    public TabsManager(BrowserActivity activity) {
        mTabsMap.put(1, null);
        mActivity = activity;
    }

    public BrowserFragment newTab(String url, boolean isIncognito, boolean show){
        if (TextUtils.isEmpty(url)){// 隐藏所有的BrowserFragment
            hideAllBrowserFrg();
        }else{
            final BrowserFragment fragment = new BrowserFragment();
            final Bundle bundle = new Bundle();
            bundle.putString(GlobalParams.URL, url);
            bundle.putBoolean(GlobalParams.IS_INCOGNITO, isIncognito);
            fragment.setArguments(bundle);

            mTabsMap.put(mTabsMap.size() + 1, fragment);
            mActivity.add(fragment, show);
            if (mTabNumberListener != null){
                mTabNumberListener.tabNumberChanged(mTabsMap.size());
            }
        }
        return null;
    }

    public boolean deleteTab(){

        return false;
    }

    public void switchTab(int position){

    }

    public void setTabNumberChangedListener(@Nullable TabNumberChangedListener listener) {
        mTabNumberListener = listener;
    }

    public interface TabNumberChangedListener {
        void tabNumberChanged(int newNumber);
    }

    public int size(){
        return mTabsMap.size();
    }

    public void hideAllBrowserFrg(){
        for (Fragment fragment : mTabsMap.values()) {
            mActivity.hide(fragment);
        }
    }

}

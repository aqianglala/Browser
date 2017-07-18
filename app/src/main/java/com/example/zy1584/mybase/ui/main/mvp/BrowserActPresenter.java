package com.example.zy1584.mybase.ui.main.mvp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.manager.TabsManager;
import com.example.zy1584.mybase.ui.main.BrowserActivity;
import com.example.zy1584.mybase.ui.main.BrowserFragment;

/**
 * Created by zy1584 on 2017-7-15.
 */

public class BrowserActPresenter extends BasePresenter<BrowserActivity> implements BrowserActContract.Presenter {
    @NonNull
    private TabsManager mTabsModel;
    private BrowserFragment mCurrentTab;

    public BrowserActPresenter() {

    }

    @Override
    public boolean newTab(@Nullable String url, boolean isIncognito, boolean show) {
        mTabsModel = getIView().getTabModel();
        if (mTabsModel.size() >= 20) {
            getIView().showSnackBar(R.string.max_tabs);
            return false;
        }
        mTabsModel.newTab(url, isIncognito, show);
        return true;
    }

    @Override
    public void loadUrlInCurrentView(@NonNull String url) {
        final BrowserFragment currentTab = mTabsModel.getCurrentTab();
        if (currentTab == null) {
            getIView().loadUrlInNewFragment(url);
            return;
        }
        currentTab.loadUrl(url);
    }

}

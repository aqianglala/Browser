package com.example.zy1584.mybase.ui.main.mvp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.manager.TabsManager;
import com.example.zy1584.mybase.ui.main.BrowserActivity;

/**
 * Created by zy1584 on 2017-7-15.
 */

public class BrowserActPresenter extends BasePresenter<BrowserActivity> implements BrowserActContract.Presenter {
    @NonNull
    private final TabsManager mTabsModel;

    public BrowserActPresenter() {
        mTabsModel = getIView().getTabModel();
    }

    @Override
    public boolean newTab(@Nullable String url, boolean isIncognito, boolean show) {
        if (mTabsModel.size() >= 20) {
            getIView().showSnackbar(R.string.max_tabs);
            return false;
        }
        mTabsModel.newTab(url, isIncognito, show);
        return true;
    }
}

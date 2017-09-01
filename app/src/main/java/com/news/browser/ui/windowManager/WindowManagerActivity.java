package com.news.browser.ui.windowManager;

import android.graphics.Color;
import android.os.Bundle;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BasePresenter;
import com.news.browser.utils.StatusBarUtils;

public class WindowManagerActivity extends BaseActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_window_manager;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        StatusBarUtils.setColor(this, Color.BLACK);
    }
}

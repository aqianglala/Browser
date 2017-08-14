package com.news.browser.ui.about;

import android.os.Bundle;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.ui.about.mvp.AboutContract;
import com.news.browser.ui.about.mvp.AboutPresenter;

public class AboutActivity extends BaseActivity<AboutPresenter> implements AboutContract.View {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected AboutPresenter loadPresenter() {
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {

    }
}

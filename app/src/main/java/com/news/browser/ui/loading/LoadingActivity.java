package com.news.browser.ui.loading;

import android.os.Bundle;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.ui.loading.mvp.LoadingActContract;
import com.news.browser.ui.loading.mvp.LoadingPresenter;
import com.news.browser.utils.SPUtils;

import static com.news.browser.utils.GlobalParams.IS_NET_ALLOW;

public class LoadingActivity extends BaseActivity<LoadingPresenter> implements LoadingActContract.View{

    @Override
    protected int getLayoutId() {
        return R.layout.activity_loading;
    }

    @Override
    protected LoadingPresenter loadPresenter() {
        return new LoadingPresenter();
    }


    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        showNetAllow();
    }

    private void showNetAllow(){
        final boolean isNetAllow = (boolean) SPUtils.get(IS_NET_ALLOW, false);
        if (!isNetAllow){

        }
    }

}

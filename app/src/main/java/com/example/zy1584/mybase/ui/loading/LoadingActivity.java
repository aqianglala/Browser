package com.example.zy1584.mybase.ui.loading;

import android.os.Bundle;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseActivity;
import com.example.zy1584.mybase.ui.loading.mvp.LoadingActContract;
import com.example.zy1584.mybase.ui.loading.mvp.LoadingPresenter;
import com.example.zy1584.mybase.utils.SPUtils;

import static com.example.zy1584.mybase.utils.GlobalParams.IS_NET_ALLOW;

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

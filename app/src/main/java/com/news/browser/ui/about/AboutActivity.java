package com.news.browser.ui.about;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.ui.about.mvp.AboutContract;
import com.news.browser.ui.about.mvp.AboutPresenter;

import butterknife.BindView;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity<AboutPresenter> implements AboutContract.View {

    @OnClick(R.id.iv_back)
    void back(){
        finish();
    }
    @BindView(R.id.tv_version)
    TextView tv_version;
    @BindView(R.id.tv_title)
    TextView tv_title;

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
        tv_title.setText(R.string.about_browser);
        PackageManager packageManager = mActivity.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mActivity.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            tv_version.setText("版本号：v" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}

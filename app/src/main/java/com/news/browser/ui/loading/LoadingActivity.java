package com.news.browser.ui.loading;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.dialog.BrowserDialog;
import com.news.browser.ui.loading.mvp.LoadingActContract;
import com.news.browser.ui.loading.mvp.LoadingPresenter;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.LocationUtils;
import com.news.browser.utils.SPUtils;

import static com.news.browser.utils.GlobalParams.IS_NET_ALLOW;

public class LoadingActivity extends BaseActivity<LoadingPresenter> implements LoadingActContract.View {
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            removeCallbacksAndMessages(null);
            gotoActivity(BrowserActivity.class, true, null);
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_loading;
    }

    @Override
    protected LoadingPresenter loadPresenter() {
        return new LoadingPresenter();
    }

    @Override
    protected boolean isFullScreen() {
        // 解决冷启动背景一片空白的情况
        setTheme(R.style.AppTheme);
        return true;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        // 每次启动时重新请求服务器地址
        SPUtils.remove(this, GlobalParams.IP);
        SPUtils.remove(this, GlobalParams.PORT);
        showNetAllow();
    }

    private void showNetAllow() {
        final boolean isNetAllow = (boolean) SPUtils.get(IS_NET_ALLOW, false);
        if (!isNetAllow) {
            BrowserDialog.showConfirm(this, R.string.show_net_allow_title, R.string.show_net_allow_message,
                    new BrowserDialog.Item(R.string.exit) {
                        @Override
                        public void onClick() {
                            finish();
                        }
                    }, new BrowserDialog.Item(R.string.enter) {

                        @Override
                        public void onClick() {
                            SPUtils.put(IS_NET_ALLOW, true);
                            new LocationUtils().getLocation();
                            gotoActivity(BrowserActivity.class, true, null);
                        }
                    });
        } else {
            new LocationUtils().getLocation();
            mHandler.sendMessageDelayed(Message.obtain(), 500);
        }
    }

}

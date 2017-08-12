package com.news.browser.ui.main.mvp;

import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.news.browser.bean.EngineBean;

/**
 * Created by zy1584 on 2017-7-13.
 */

public interface BrowserActContract {
    interface ActView {

        void onCreateWindow(Message resultMsg);

        void onCloseWindow();

        void onShowCustomView(View view, CustomViewCallback callback);

        void onShowCustomView(View view, CustomViewCallback callback, int requestedOrientation);

        void onHideCustomView();

        void showSnackBar(@StringRes int resource);

        void setForwardButtonEnabled(boolean enabled);

        void setBackButtonEnabled(boolean enabled);

        void receiveSearchEngine(EngineBean bean);

        void onReceiveEngineError(Throwable e);
    }

    interface Presenter{

        boolean newTab(@Nullable String url, boolean isIncognito, boolean show);

        void loadUrlInCurrentView(@NonNull final String url);

        void getSearchEngine();

    }
}

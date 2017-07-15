package com.example.zy1584.mybase.ui.main.mvp;

import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

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

        void showSnackbar(@StringRes int resource);
    }

    interface Presenter{

        boolean newTab(@Nullable String url, boolean isIncognito, boolean show);

    }
}

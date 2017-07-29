/*
 * Copyright 2014 A.C.R. Development
 */
package com.example.zy1584.mybase.ui.download;

import android.app.Activity;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;

import com.example.zy1584.mybase.preference.PreferenceManager;


public class LightningDownloadListener implements DownloadListener {

    private final Activity mActivity;

    PreferenceManager mPreferenceManager;

    public LightningDownloadListener(Activity context) {
        mActivity = context;
        mPreferenceManager = new PreferenceManager(context);
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent,
                                final String contentDisposition, final String mimetype, long contentLength) {
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
        DownloadHandler.promptDownload(mActivity, mPreferenceManager, fileName, url, userAgent,
                contentDisposition, mimetype, null, null);
    }
}

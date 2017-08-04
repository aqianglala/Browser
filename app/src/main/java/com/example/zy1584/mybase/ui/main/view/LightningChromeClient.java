package com.example.zy1584.mybase.ui.main.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseApplication;
import com.example.zy1584.mybase.ui.main.BrowserFragment;
import com.example.zy1584.mybase.utils.Preconditions;
import com.example.zy1584.mybase.ui.main.mvp.BrowserActContract;
import com.example.zy1584.mybase.ui.main.mvp.BrowserFrgContract;
import com.example.zy1584.mybase.utils.Utils;


public class LightningChromeClient extends WebChromeClient {

    private static final String TAG = LightningChromeClient.class.getSimpleName();

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    @NonNull
    private final Activity mActivity;
    @NonNull
    private BrowserFrgContract.FrgView mBrowserView;
    private BrowserActContract.ActView mActView;

    private BrowserFragment mFragment;


    public LightningChromeClient(@NonNull Activity activity, @NonNull BrowserFragment fragment) {
        Preconditions.checkNonNull(activity);
        Preconditions.checkNonNull(fragment);
        mActivity = activity;
        mFragment = fragment;
        mBrowserView = (BrowserFrgContract.FrgView)fragment;
        mActView = (BrowserActContract.ActView) activity;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (mBrowserView.isShown()) {
            mBrowserView.updateProgress(newProgress);
        }
    }

    @Override
    public void onReceivedIcon(@NonNull WebView view, Bitmap icon) {
        mFragment.getTitleInfo().setFavicon(icon);
//        mUIController.tabChanged(mLightningView);
        // TODO: 2017-7-13 通知图标发生改变
        cacheFavicon(view.getUrl(), icon, mActivity);
    }

    /**
     * Naive caching of the favicon according to the domain name of the URL
     *
     * @param icon the icon to cache
     */
    private static void cacheFavicon(@Nullable final String url, @Nullable final Bitmap icon, @NonNull final Context context) {
        if (icon == null || url == null) return;
        final Uri uri = Uri.parse(url);
        if (uri.getHost() == null) {
            return;
        }
        BaseApplication.getIOThread().execute(new IconCacheTask(uri, icon, BaseApplication.get(context)));
    }


    @Override
    public void onReceivedTitle(@Nullable WebView view, @Nullable String title) {
        if (title != null && !title.isEmpty()) {
            mFragment.getTitleInfo().setTitle(title);
        } else {
            mFragment.getTitleInfo().setTitle(mActivity.getString(R.string.untitled));
        }
//        mUIController.tabChanged(mLightningView);
        // TODO: 2017-7-13 通知图标发生改变
        if (view != null && view.getUrl() != null) {
            mBrowserView.updateHistory(title, view.getUrl());
        }
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(@NonNull final String origin,
                                                   @NonNull final GeolocationPermissions.Callback callback) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mActivity, PERMISSIONS, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                final boolean remember = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(mActivity.getString(R.string.location));
                String org;
                if (origin.length() > 50) {
                    org = origin.subSequence(0, 50) + "...";
                } else {
                    org = origin;
                }
                builder.setMessage(org + mActivity.getString(R.string.message_location))
                        .setCancelable(true)
                        .setPositiveButton(mActivity.getString(R.string.action_allow),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        callback.invoke(origin, true, remember);
                                    }
                                })
                        .setNegativeButton(mActivity.getString(R.string.action_dont_allow),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        callback.invoke(origin, false, remember);
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                Utils.setDialogSize(mActivity, alert);
            }

            @Override
            public void onDenied(String permission) {
                //TODO show message and/or turn off setting
            }
        });
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture,
                                  Message resultMsg) {
        mActView.onCreateWindow(resultMsg);
        return true;
    }

    @Override
    public void onCloseWindow(WebView window) {
        mActView.onCloseWindow();
    }

    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
//        mBrowserView.openFileChooser(uploadMsg);
        // TODO: 2017-7-13 由activity来处理
    }

    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
//        mBrowserView.openFileChooser(uploadMsg);
        // TODO: 2017-7-13 由activity来处理
    }

    @SuppressWarnings("unused")
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
//        mBrowserView.openFileChooser(uploadMsg);
        // TODO: 2017-7-13 由activity来处理
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     WebChromeClient.FileChooserParams fileChooserParams) {
//        mUIController.showFileChooser(filePathCallback);
        // TODO: 2017-7-13  由activity处理
        return true;
    }

    /**
     * Obtain an image that is displayed as a placeholder on a video until the video has initialized
     * and can begin loading.
     *
     * @return a Bitmap that can be used as a place holder for videos.
     */
    @Nullable
    @Override
    public Bitmap getDefaultVideoPoster() {
        final Resources resources = mActivity.getResources();
        return BitmapFactory.decodeResource(resources, android.R.drawable.spinner_background);
    }

    /**
     * Inflate a mContentView to send to a LightningView when it needs to display a video and has to
     * show a loading dialog. Inflates a progress mContentView and returns it.
     *
     * @return A mContentView that should be used to display the state
     * of a video's loading progress.
     */
    @Override
    public android.view.View getVideoLoadingProgressView() {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        return inflater.inflate(R.layout.video_loading_progress, null);
    }

    @Override
    public void onHideCustomView() {
        mActView.onHideCustomView();
    }

    @Override
    public void onShowCustomView(android.view.View view, CustomViewCallback callback) {
        mActView.onShowCustomView(view, callback);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onShowCustomView(android.view.View view, int requestedOrientation,
                                 CustomViewCallback callback) {
        mActView.onShowCustomView(view, callback, requestedOrientation);
    }
}

package com.news.browser.ui.main.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.news.browser.R;
import com.news.browser.ui.main.BrowserFragment;
import com.news.browser.ui.main.mvp.BrowserActContract;
import com.news.browser.ui.main.mvp.BrowserFrgContract;
import com.news.browser.utils.AdBlock;
import com.news.browser.utils.Constants;
import com.news.browser.utils.IntentUtils;
import com.news.browser.utils.Preconditions;
import com.news.browser.utils.Utils;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BrowserWebClient extends WebViewClient {

    @NonNull
    private final Activity mActivity;
    @NonNull
    private BrowserFrgContract.FrgView mBrowserView;
    @NonNull
    private BrowserActContract.ActView mActView;

    private BrowserFragment mFragment;
    @NonNull
    private final IntentUtils mIntentUtils;

    AdBlock mAdBlock;

    public BrowserWebClient(@NonNull Activity activity, @NonNull BrowserFragment fragment) {
        mAdBlock = AdBlock.getInstance();
        Preconditions.checkNonNull(activity);
        Preconditions.checkNonNull(fragment);
        mActivity = activity;
        mFragment = fragment;
        mBrowserView = (BrowserFrgContract.FrgView) fragment;
        mActView = (BrowserActContract.ActView) activity;
        mAdBlock.updatePreference();
        mIntentUtils = new IntentUtils(activity);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, @NonNull WebResourceRequest request) {
        if (mAdBlock.isAd(request.getUrl().toString())) {
            ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
            return new WebResourceResponse("text/plain", "utf-8", EMPTY);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (mAdBlock.isAd(url)) {
            ByteArrayInputStream EMPTY = new ByteArrayInputStream("".getBytes());
            return new WebResourceResponse("text/plain", "utf-8", EMPTY);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onPageFinished(@NonNull WebView view, String url) {
        if (view.isShown()) {
            mBrowserView.updateUrl(url, true);
            mActView.setBackButtonEnabled(view.canGoBack());
            mActView.setForwardButtonEnabled(view.canGoForward());
            view.postInvalidate();
        }

        if (view.getTitle() == null || view.getTitle().isEmpty()) {
            mFragment.getTitleInfo().setTitle(mActivity.getString(R.string.untitled));
        } else {
            mFragment.getTitleInfo().setTitle(view.getTitle());
        }
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT &&
                mFragment.getInvertePage()) {
            view.evaluateJavascript(Constants.JAVASCRIPT_INVERT_PAGE, null);
        }
        // TODO: 2017-7-13
//        mUIController.tabChanged(mLightningView);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mFragment.getTitleInfo().setFavicon(null);
        if (mFragment.isShown()) {
            mBrowserView.updateUrl(url, false);
            mBrowserView.showActionBar();
        }
        // TODO: 2017-7-13
//        mUIController.tabChanged(mLightningView);
    }

    @Override
    public void onReceivedHttpAuthRequest(final WebView view, @NonNull final HttpAuthHandler handler,
                                          final String host, final String realm) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final EditText name = new EditText(mActivity);
        final EditText password = new EditText(mActivity);
        LinearLayout passLayout = new LinearLayout(mActivity);
        passLayout.setOrientation(LinearLayout.VERTICAL);

        passLayout.addView(name);
        passLayout.addView(password);

        name.setHint(mActivity.getString(R.string.hint_username));
        name.setSingleLine();
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setSingleLine();
        password.setTransformationMethod(new PasswordTransformationMethod());
        password.setHint(mActivity.getString(R.string.hint_password));
        builder.setTitle(mActivity.getString(R.string.title_sign_in));
        builder.setView(passLayout);
        builder.setCancelable(true)
                .setPositiveButton(mActivity.getString(R.string.title_sign_in),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String user = name.getText().toString();
                                String pass = password.getText().toString();
                                handler.proceed(user.trim(), pass.trim());
                                Log.d(Constants.TAG, "Request Login");

                            }
                        })
                .setNegativeButton(mActivity.getString(R.string.action_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
        Utils.setDialogSize(mActivity, alert);
    }

    private volatile boolean mIsRunning = false;
    private float mZoomScale = 0.0f;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onScaleChanged(@NonNull final WebView view, final float oldScale, final float newScale) {
        if (view.isShown() && mFragment.mPreferences.getTextReflowEnabled() &&
                Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (mIsRunning)
                return;
            float changeInPercent = Math.abs(100 - 100 / mZoomScale * newScale);
            if (changeInPercent > 2.5f && !mIsRunning) {
                mIsRunning = view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mZoomScale = newScale;
                        view.evaluateJavascript(Constants.JAVASCRIPT_TEXT_REFLOW, new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                mIsRunning = false;
                            }
                        });
                    }
                }, 100);
            }

        }
    }

    @NonNull
    private static List<Integer> getAllSslErrorMessageCodes(@NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = new ArrayList<>(1);

        if (error.hasError(SslError.SSL_DATE_INVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_date_invalid);
        }
        if (error.hasError(SslError.SSL_EXPIRED)) {
            errorCodeMessageCodes.add(R.string.message_certificate_expired);
        }
        if (error.hasError(SslError.SSL_IDMISMATCH)) {
            errorCodeMessageCodes.add(R.string.message_certificate_domain_mismatch);
        }
        if (error.hasError(SslError.SSL_NOTYETVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_not_yet_valid);
        }
        if (error.hasError(SslError.SSL_UNTRUSTED)) {
            errorCodeMessageCodes.add(R.string.message_certificate_untrusted);
        }
        if (error.hasError(SslError.SSL_INVALID)) {
            errorCodeMessageCodes.add(R.string.message_certificate_invalid);
        }

        return errorCodeMessageCodes;
    }

    @Override
    public void onReceivedSslError(WebView view, @NonNull final SslErrorHandler handler, @NonNull SslError error) {
        List<Integer> errorCodeMessageCodes = getAllSslErrorMessageCodes(error);

        StringBuilder stringBuilder = new StringBuilder();
        for (Integer messageCode : errorCodeMessageCodes) {
            stringBuilder.append(" - ").append(mActivity.getString(messageCode)).append('\n');
        }
        String alertMessage =
                mActivity.getString(R.string.message_insecure_connection, stringBuilder.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.title_warning));
        builder.setMessage(alertMessage)
                .setCancelable(true)
                .setPositiveButton(mActivity.getString(R.string.action_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.proceed();
                            }
                        })
                .setNegativeButton(mActivity.getString(R.string.action_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                handler.cancel();
                            }
                        });
        Dialog dialog = builder.show();
        Utils.setDialogSize(mActivity, dialog);
    }

    @Override
    public void onFormResubmission(WebView view, @NonNull final Message dontResend, @NonNull final Message resend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.title_form_resubmission));
        builder.setMessage(mActivity.getString(R.string.message_form_resubmission))
                .setCancelable(true)
                .setPositiveButton(mActivity.getString(R.string.action_yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                resend.sendToTarget();
                            }
                        })
                .setNegativeButton(mActivity.getString(R.string.action_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dontResend.sendToTarget();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
        Utils.setDialogSize(mActivity, alert);
    }

    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull String url) {
        // Check if configured proxy is
        // TODO: 2017-7-13 代理设置
//        if (!mProxyUtils.isProxyReady()) {
//            // User has been notified
//            return true;
//        }

        Map<String, String> headers = mFragment.getRequestHeaders();

        // If the headers are empty, the user has not expressed the desire
        // to use them and therefore we can revert ic_back_white to the old way of loading
        if (headers.isEmpty()) {
            if (mFragment.isIncognito()) {
                // If we are in incognito, immediately load, we don't want the url to leave the app
                return super.shouldOverrideUrlLoading(view, url);
            }
            if (url.startsWith("about:")) {
                // If this is an about page, immediately load, we don't need to leave the app
                return super.shouldOverrideUrlLoading(view, url);
            }

            if (isMailOrIntent(url, view) || mIntentUtils.startActivityForUrl(view, url)) {
                // If it was a mailto: link, or an intent, or could be launched elsewhere, do that
                return true;
            }
        } else {
            if (mFragment.isIncognito() && Utils.doesSupportHeaders()) {
                // If we are in incognito, immediately load, we don't want the url to leave the app
                view.loadUrl(url, headers);
                return true;
            }
            if (url.startsWith("about:") && Utils.doesSupportHeaders()) {
                // If this is an about page, immediately load, we don't need to leave the app
                view.loadUrl(url, headers);
                return true;
            }

            if (isMailOrIntent(url, view) || mIntentUtils.startActivityForUrl(view, url)) {
                // If it was a mailto: link, or an intent, or could be launched elsewhere, do that
                return true;
            } else if (Utils.doesSupportHeaders()) {
                // Otherwise, load the headers.
                view.loadUrl(url, headers);
                return true;
            }
        }
        // If none of those instances was true, revert ic_back_white to the old way of loading
        return super.shouldOverrideUrlLoading(view, url);
    }

    private boolean isMailOrIntent(@NonNull String url, @NonNull WebView view) {
        if (url.startsWith("mailto:")) {
            MailTo mailTo = MailTo.parse(url);
            Intent i = Utils.newEmailIntent(mailTo.getTo(), mailTo.getSubject(),
                    mailTo.getBody(), mailTo.getCc());
            mActivity.startActivity(i);
            view.reload();
            return true;
        } else if (url.startsWith("intent://")) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            } catch (URISyntaxException ignored) {
                intent = null;
            }
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    intent.setSelector(null);
                }
                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(Constants.TAG, "ActivityNotFoundException");
                }
                return true;
            }
        }
        return false;
    }

}

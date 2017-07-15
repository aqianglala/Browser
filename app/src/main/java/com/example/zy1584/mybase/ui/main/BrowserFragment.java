package com.example.zy1584.mybase.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseApplication;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.ui.main.db.BookmarkManager;
import com.example.zy1584.mybase.ui.main.db.HistoryDatabase;
import com.example.zy1584.mybase.ui.main.db.HistoryItem;
import com.example.zy1584.mybase.ui.main.mvp.BrowserFrgContract;
import com.example.zy1584.mybase.ui.main.view.LightningChromeClient;
import com.example.zy1584.mybase.ui.main.view.LightningViewTitle;
import com.example.zy1584.mybase.ui.main.view.LightningWebClient;
import com.example.zy1584.mybase.utils.DensityUtils;
import com.example.zy1584.mybase.utils.GlobalParams;
import com.example.zy1584.mybase.utils.UIUtils;
import com.example.zy1584.mybase.utils.Utils;
import com.example.zy1584.mybase.widget.AnimatedProgressBar;

import java.io.File;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by zy1584 on 2017-7-13.
 */

public class BrowserFragment extends BaseFragment implements BrowserFrgContract.FrgView {
    @Nullable
    private WebView mWebView;
    public PreferenceManager mPreferences;
    private boolean mIsIncognitoTab;
    private String mUrl;
    private Bundle webViewState;

    public static final String HEADER_REQUESTED_WITH = "X-Requested-With";
    public static final String HEADER_WAP_PROFILE = "X-Wap-Profile";
    private static final String HEADER_DNT = "DNT";
    private static final int API = android.os.Build.VERSION.SDK_INT;
    private static final int SCROLL_UP_THRESHOLD = DensityUtils.dpToPx(10);

    private static String sHomepage;
    private static String sDefaultUserAgent;
    private static float sMaxFling;
    private boolean mInvertPage = false;
    private HistoryDatabase mHistoryDatabase;
    private BookmarkManager mBookmarkManager;

    private String mUntitledTitle;
    private Drawable mDeleteIcon, mRefreshIcon, mIcon;

    @NonNull private final Map<String, String> mRequestHeaders = new ArrayMap<>();
    @NonNull private LightningViewTitle mTitle;

    @NonNull
    private GestureDetector mGestureDetector;

    @BindView(R.id.content_frame)
    FrameLayout content_frame;
    @BindView(R.id.tv_search)
    TextView tv_search;
    @BindView(R.id.progress_view)
    AnimatedProgressBar progress_view;
    @BindView(R.id.iv_right)
    ImageView iv_right;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_browser;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (webViewState != null) {
            //Fragment实例并未被销毁, 重新create view
            mWebView.restoreState(webViewState);
        } else if (savedInstanceState != null) {
            //Fragment实例被销毁重建
            mWebView.restoreState(savedInstanceState);
        } else {
            //全新Fragment
            if (TextUtils.isEmpty(mUrl)) {
                mUrl = mPreferences.getHomepage();
            }
            mWebView.loadUrl(mUrl, mRequestHeaders);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        final Bundle arguments = getArguments();
        mIsIncognitoTab = arguments.getBoolean(GlobalParams.IS_INCOGNITO, false);
        mUrl = arguments.getString(GlobalParams.URL);

        mHistoryDatabase = new HistoryDatabase(mActivity);
        mBookmarkManager = new BookmarkManager(mActivity);
        mUntitledTitle = getString(R.string.untitled);
        mDeleteIcon = UIUtils.getDrawable(R.drawable.ic_action_delete);
        mRefreshIcon = UIUtils.getDrawable(R.drawable.ic_action_refresh);
        iv_right.setVisibility(View.VISIBLE);
        iv_right.setImageDrawable(mRefreshIcon);

        mWebView = new WebView(mActivity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            mWebView.setId(android.view.View.generateViewId());
        }
        mTitle = new LightningViewTitle(mActivity);
        sMaxFling = ViewConfiguration.get(mActivity).getScaledMaximumFlingVelocity();

        mWebView.setDrawingCacheBackgroundColor(Color.WHITE);
        mWebView.setFocusableInTouchMode(true);
        mWebView.setFocusable(true);
        mWebView.setDrawingCacheEnabled(false);
        mWebView.setWillNotCacheDrawing(true);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //noinspection deprecation
            mWebView.setAnimationCacheEnabled(false);
            //noinspection deprecation
            mWebView.setAlwaysDrawnWithCacheEnabled(false);
        }
        mWebView.setBackgroundColor(Color.WHITE);

        mWebView.setScrollbarFadingEnabled(true);
        mWebView.setSaveEnabled(true);
        mWebView.setNetworkAvailable(true);
        mWebView.setWebChromeClient(new LightningChromeClient(mActivity, this));
        mWebView.setWebViewClient(new LightningWebClient(mActivity, this));
        // TODO: 2017-7-13
//        mWebView.setDownloadListener(new LightningDownloadListener(mActivity));
        mGestureDetector = new GestureDetector(mActivity, new CustomGestureListener());
        mWebView.setOnTouchListener(new TouchListener());
        sDefaultUserAgent = mWebView.getSettings().getUserAgentString();
        initializeSettings();
        initializePreferences(mActivity);

        content_frame.addView(mWebView, MATCH_PARENT, MATCH_PARENT);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        
    }

    /**
     * Initialize the preference driven settings of the WebView. This method
     * must be called whenever the preferences are changed within SharedPreferences.
     *
     * @param context the context in which the WebView was created, it is used
     *                to get the default UserAgent for the WebView.
     */
    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    public synchronized void initializePreferences(@NonNull Context context) {
        mPreferences = new PreferenceManager(context);
        if (mWebView == null) {
            return;
        }

        WebSettings settings = mWebView.getSettings();

        if (mPreferences.getDoNotTrackEnabled()) {// 设置隐私信息是否能被追踪
            mRequestHeaders.put(HEADER_DNT, "1");
        } else {
            mRequestHeaders.remove(HEADER_DNT);
        }

        if (mPreferences.getRemoveIdentifyingHeadersEnabled()) {
            mRequestHeaders.put(HEADER_REQUESTED_WITH, "");
            mRequestHeaders.put(HEADER_WAP_PROFILE, "");
        } else {
            mRequestHeaders.remove(HEADER_REQUESTED_WITH);
            mRequestHeaders.remove(HEADER_WAP_PROFILE);
        }

        settings.setDefaultTextEncodingName(mPreferences.getTextEncoding());
        sHomepage = mPreferences.getHomepage();

        if (!mIsIncognitoTab) {
            settings.setGeolocationEnabled(mPreferences.getLocationEnabled());
        } else {
            settings.setGeolocationEnabled(false);
        }

        setUserAgent(context, mPreferences.getUserAgentChoice());

        if (mPreferences.getSavePasswordsEnabled() && !mIsIncognitoTab) {
            if (API < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //noinspection deprecation
                settings.setSavePassword(true);
            }
            settings.setSaveFormData(true);
        } else {
            if (API < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //noinspection deprecation
                settings.setSavePassword(false);
            }
            settings.setSaveFormData(false);
        }

        if (mPreferences.getJavaScriptEnabled()) {
            settings.setJavaScriptEnabled(true);
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
        } else {
            settings.setJavaScriptEnabled(false);
            settings.setJavaScriptCanOpenWindowsAutomatically(false);
        }

        if (mPreferences.getTextReflowEnabled()) {
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            if (API >= android.os.Build.VERSION_CODES.KITKAT) {
                try {
                    settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
                } catch (Exception e) {
                    // This shouldn't be necessary, but there are a number
                    // of KitKat devices that crash trying to set this
                    Log.e(TAG, "Problem setting LayoutAlgorithm to TEXT_AUTOSIZING");
                }
            }
        } else {
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }

        settings.setBlockNetworkImage(mPreferences.getBlockImagesEnabled());
        if (!mIsIncognitoTab) {
            settings.setSupportMultipleWindows(mPreferences.getPopupsEnabled());
        } else {
            settings.setSupportMultipleWindows(false);
        }
        settings.setUseWideViewPort(mPreferences.getUseWideViewportEnabled());
        settings.setLoadWithOverviewMode(mPreferences.getOverviewModeEnabled());
        switch (mPreferences.getTextSize()) {
            case 0:
                settings.setTextZoom(200);
                break;
            case 1:
                settings.setTextZoom(150);
                break;
            case 2:
                settings.setTextZoom(125);
                break;
            case 3:
                settings.setTextZoom(100);
                break;
            case 4:
                settings.setTextZoom(75);
                break;
            case 5:
                settings.setTextZoom(50);
                break;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView,
                    !mPreferences.getBlockThirdPartyCookiesEnabled());
        }
    }

    /**
     * Initialize the settings of the WebView that are intrinsic to Lightning and cannot
     * be altered by the user. Distinguish between Incognito and Regular tabs here.
     */
    @SuppressLint("NewApi")
    private void initializeSettings() {
        if (mWebView == null) {
            return;
        }
        final WebSettings settings = mWebView.getSettings();
        if (API < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            settings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        if (API < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //noinspection deprecation
            settings.setEnableSmoothTransition(true);
        }
        if (API > Build.VERSION_CODES.JELLY_BEAN) {
            settings.setMediaPlaybackRequiresUserGesture(true);
        }
        if (API >= Build.VERSION_CODES.LOLLIPOP && !mIsIncognitoTab) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        } else if (API >= Build.VERSION_CODES.LOLLIPOP) {
            // We're in Incognito mode, reject
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        if (!mIsIncognitoTab) {
            settings.setDomStorageEnabled(true);
            settings.setAppCacheEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setDatabaseEnabled(true);
        } else {
            settings.setDomStorageEnabled(false);
            settings.setAppCacheEnabled(false);
            settings.setDatabaseEnabled(false);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        if (API >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }

        getPathObservable("appcache")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File item) {
                        settings.setAppCachePath(item.getPath());
                    }
                });

        getPathObservable("geolocation")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File item) {
                        settings.setGeolocationDatabasePath(item.getPath());
                    }
                });

        getPathObservable("databases")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File item) {
                        if (API < Build.VERSION_CODES.KITKAT) {
                            //noinspection deprecation
                            settings.setDatabasePath(item.getPath());
                        }
                    }
                });

    }

    private Observable<File> getPathObservable(final String subFolder) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                File file = mActivity.getApplication().getDir(subFolder, 0);
                subscriber.onNext(file);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public void showActionBar() {

    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void updateProgress(int newProgress) {
        setIsLoading(newProgress < 100);
        progress_view.setProgress(newProgress);
    }

    /**
     * This method determines whether the current tab is visible or not.
     *
     * @return true if the WebView is non-null and visible, false otherwise.
     */
    @Override
    public boolean isShown() {
        return mWebView != null && mWebView.isShown();
    }

    public LightningViewTitle getTitleInfo() {
        return mTitle;
    }

    @Override
    public void updateUrl(@Nullable String url, boolean shortUrl) {
        if (url == null || tv_search == null) {
            return;
        }
        // TODO: 2017-7-14 通知更新书签url
//        mEventBus.post(new BrowserEvents.CurrentPageUrl(url));
        if (shortUrl) {
            switch (mPreferences.getUrlBoxContentChoice()) {
                case 0: // Default, show only the domain
                    url = url.replaceFirst(Constants.HTTP, "");
                    url = Utils.getDomainName(url);
                    tv_search.setText(url);
                    break;
                case 1: // URL, show the entire URL
                    tv_search.setText(url);
                    break;
                case 2: // Title, show the page's title
                    if (!getTitle().isEmpty()) {
                        tv_search.setText(getTitle());
                    } else {
                        tv_search.setText(mUntitledTitle);
                    }
                    break;
            }
        } else {
            tv_search.setText(url);
        }
    }

    @Override
    public void updateHistory(@Nullable String title, @NonNull String url) {
        if (mIsIncognitoTab) return;
        addItemToHistory(title, url);
    }

    public void setVisibility(int visible) {
        if (mWebView != null) {
            mWebView.setVisibility(visible);
        }
    }

    @NonNull
    public Bitmap getFavicon() {
        return mTitle.getFavicon(false);
    }

    @NonNull
    public String getTitle() {
        return mTitle.getTitle();
    }

    public boolean isIncognito() {
        return mIsIncognitoTab;
    }

    @NonNull
    public Map<String, String> getRequestHeaders() {
        return mRequestHeaders;
    }

    public boolean getInvertePage() {
        return mInvertPage;
    }

    /**
     * handle presses on the refresh icon in the search bar, if the page is
     * loading, stop the page, if it is done loading refresh the page.
     * See setIsFinishedLoading and setIsLoading for displaying the correct icon
     */
    @OnClick(R.id.iv_right)
    void refreshOrStop() {
        if (getProgress() < 100) {
            stopLoading();
        } else {
            reload();
        }
    }

    /**
     * Gets the current progress of the WebView.
     *
     * @return returns a number between 0 and 100 with
     * the current progress of the WebView. If the WebView
     * is null, then the progress returned will be 100.
     */
    public int getProgress() {
        if (mWebView != null) {
            return mWebView.getProgress();
        } else {
            return 100;
        }
    }

    /**
     * Notify the WebView to stop the current load.
     */
    public synchronized void stopLoading() {
        if (mWebView != null) {
            mWebView.stopLoading();
        }
    }

    /**
     * Tells the WebView to reload the current page.
     * If the proxy settings are not ready then the
     * this method will not have an affect as the
     * proxy must start before the load occurs.
     */
    public synchronized void reload() {
        if (mWebView != null) {
            mWebView.reload();
        }
    }

    /**
     * This method lets the search bar know that the page is currently loading
     * and that it should display the stop icon to indicate to the user that
     * pressing it stops the page from loading
     */
    private void setIsLoading(boolean isLoading) {
        mIcon = isLoading ? mDeleteIcon : mRefreshIcon;
        iv_right.setImageDrawable(mIcon);
    }

    void addItemToHistory(@Nullable final String title, @NonNull final String url) {
        BaseApplication.getIOThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mHistoryDatabase.visitHistoryItem(url, title);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "IllegalStateException in updateHistory", e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullPointerException in updateHistory", e);
                } catch (SQLiteException e) {
                    Log.e(TAG, "SQLiteException in updateHistory", e);
                }
            }
        });
    }

    /**
     * 将当前网页加入书签
     */
    public void bookmarkCurrentPage() {
        final String url = getUrl();
        final String title = getTitle();
        if (url == null) {
            return;
        }

        if (!mBookmarkManager.isBookmark(url)) {
            addBookmark(title, url);
        } else {
            deleteBookmark(title, url);
        }
    }

    private void addBookmark(final String title, final String url) {
        final HistoryItem item = !mBookmarkManager.isBookmark(url)
                ? new HistoryItem(url, title)
                : null;
        if (item != null && mBookmarkManager.addBookmark(item)) {
            // TODO: 2017-7-14 书签页的更新
//            mSuggestionsAdapter.refreshBookmarks();
//            mEventBus.post(new BrowserEvents.BookmarkAdded(title, url));
        }
    }

    private void deleteBookmark(final String title, final String url) {
        final HistoryItem item = mBookmarkManager.isBookmark(url)
                ? new HistoryItem(url, title)
                : null;
        if (item != null && mBookmarkManager.deleteBookmark(item)) {
            // TODO: 2017-7-14 书签页的更新
//            mSuggestionsAdapter.refreshBookmarks();
//            mEventBus.post(new BrowserEvents.CurrentPageUrl(url));
        }
    }

    /**
     * Get the current URL of the WebView, or an empty
     * string if the WebView is null or the URL is null.
     *
     * @return the current URL or an empty string.
     */
    @NonNull
    public String getUrl() {
        if (mWebView != null && mWebView.getUrl() != null) {
            return mWebView.getUrl();
        } else {
            return "";
        }
    }

    /**
     * This method sets the user agent of the current tab.
     * There are four options, 1, 2, 3, 4.
     * <p/>
     * 1. use the default user agent
     * <p/>
     * 2. use the desktop user agent
     * <p/>
     * 3. use the mobile user agent
     * <p/>
     * 4. use a custom user agent, or the default user agent
     * if none was set.
     *
     * @param context the context needed to get the default user agent.
     * @param choice  the choice of user agent to use, see above comments.
     */
    @SuppressLint("NewApi")
    private void setUserAgent(Context context, int choice) {
        if (mWebView == null) return;
        WebSettings settings = mWebView.getSettings();
        switch (choice) {
            case 1:
                if (API >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    settings.setUserAgentString(WebSettings.getDefaultUserAgent(context));
                } else {
                    settings.setUserAgentString(sDefaultUserAgent);
                }
                break;
            case 2:
                settings.setUserAgentString(Constants.DESKTOP_USER_AGENT);
                break;
            case 3:
                settings.setUserAgentString(Constants.MOBILE_USER_AGENT);
                break;
            case 4:
                String ua = mPreferences.getUserAgentString(sDefaultUserAgent);
                if (ua == null || ua.isEmpty()) {
                    ua = " ";
                }
                settings.setUserAgentString(ua);
                break;
        }
    }

    /**
     * The OnTouchListener used by the WebView so we can
     * get scroll events and show/hide the action bar when
     * the page is scrolled up/down.
     */
    private class TouchListener implements android.view.View.OnTouchListener {

        float mLocation;
        float mY;
        int mAction;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(@Nullable android.view.View view, @NonNull MotionEvent arg1) {
            if (view == null)
                return false;

            if (!view.hasFocus()) {
                view.requestFocus();
            }
            mAction = arg1.getAction();
            mY = arg1.getY();
            if (mAction == MotionEvent.ACTION_DOWN) {
                mLocation = mY;
            } else if (mAction == MotionEvent.ACTION_UP) {
                final float distance = (mY - mLocation);
                if (distance > SCROLL_UP_THRESHOLD && view.getScrollY() < SCROLL_UP_THRESHOLD) {
                    showActionBar();
                } else if (distance < -SCROLL_UP_THRESHOLD) {
                    hideActionBar();
                }
                mLocation = 0;
            }
            mGestureDetector.onTouchEvent(arg1);
            return false;
        }
    }


    /**
     * The SimpleOnGestureListener used by the {@link TouchListener}
     * in order to delegate show/hide events to the action bar when
     * the user flings the page. Also handles long press events so
     * that we can capture them accurately.
     */
    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int power = (int) (velocityY * 100 / sMaxFling);
            if (power < -10) {
                hideActionBar();// fragment
            } else if (power > 15) {
                showActionBar();// fragment
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /**
         * Without this, onLongPress is not called when user is zooming using
         * two fingers, but is when using only one.
         * <p/>
         * The required behaviour is to not trigger this when the user is
         * zooming, it shouldn't matter how much fingers the user's using.
         */
        private boolean mCanTriggerLongPress = true;

        @Override
        public void onLongPress(MotionEvent e) {
//            if (mCanTriggerLongPress) {
//                Message msg = mWebViewHandler.obtainMessage();
//                if (msg != null) {
//                    msg.setTarget(mWebViewHandler);
//                    if (mWebView == null) {
//                        return;
//                    }
//                    mWebView.requestFocusNodeHref(msg);
//                }
//            }
        }

        /**
         * Is called when the user is swiping after the doubletap, which in our
         * case means that he is zooming.
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            mCanTriggerLongPress = false;
            return false;
        }

        /**
         * Is called when something is starting being pressed, always before
         * onLongPress.
         */
        @Override
        public void onShowPress(MotionEvent e) {
            mCanTriggerLongPress = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
            //Fragment不被销毁(Fragment被加入back stack)的情况下, 依靠Fragment中的成员变量保存WebView状态
            webViewState = new Bundle();
            mWebView.saveState(webViewState);
            Log.d(TAG, "WebView onPause: " + mWebView.getId());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
            Log.d(TAG, "WebView onResume: " + mWebView.getId());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyWebView();
        if (mHistoryDatabase != null) {
            mHistoryDatabase.close();
            mHistoryDatabase = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Fragment被销毁的情况, 依靠outState保存WebView状态
        if (mWebView != null) {
            mWebView.saveState(outState);
        }
    }

    public synchronized void destroyWebView() {
        if (mWebView != null) {
            // Check to make sure the WebView has been removed
            // before calling destroy() so that a memory leak is not created
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                Log.e(TAG, "WebView was not detached from window before onDestroy");
                parent.removeView(mWebView);
            }
            mWebView.stopLoading();
            mWebView.onPause();
            mWebView.clearHistory();
            mWebView.setVisibility(android.view.View.GONE);
            mWebView.removeAllViews();
            mWebView.destroyDrawingCache();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //this is causing the segfault occasionally below 4.2
                mWebView.destroy();
            }
            mWebView = null;
        }
    }
}

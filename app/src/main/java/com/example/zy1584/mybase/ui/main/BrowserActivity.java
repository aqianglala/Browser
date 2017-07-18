package com.example.zy1584.mybase.ui.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.VideoView;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseActivity;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.manager.TabsManager;
import com.example.zy1584.mybase.ui.main.adapter.MainFragmentAdapter;
import com.example.zy1584.mybase.ui.main.mvp.BrowserActContract;
import com.example.zy1584.mybase.ui.main.mvp.BrowserActPresenter;
import com.example.zy1584.mybase.ui.navigation.NavigationFragment;
import com.example.zy1584.mybase.ui.search.SearchFragment;
import com.example.zy1584.mybase.utils.ActivityCollector;
import com.example.zy1584.mybase.utils.CompressImage;
import com.example.zy1584.mybase.utils.UrlUtils;
import com.example.zy1584.mybase.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.example.zy1584.mybase.R.id.viewPager;

public class BrowserActivity extends BaseActivity<BrowserActPresenter> implements BrowserActContract.ActView {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MainFragment mainFragment;
    private NavigationFragment navigationFragment;
    private PopupWindow mPopupWindow;
    private int keyCount;
    private TabsManager mTabsManager;
    private String mSearchText;

    private PreferenceManager mPreferences;

    private int mOriginalOrientation;
    private boolean mIsFullScreen = false;
    private boolean mIsImmersive = false;
    private boolean mFullScreen;
    private static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    // Full Screen Video Views
    private FrameLayout mFullscreenContainer;
    private VideoView mVideoView;
    private View mCustomView;

    // Callback
    private CustomViewCallback mCustomViewCallback;

    @BindView(viewPager)
    ViewPager mViewPager;

    @BindView(R.id.ll_toolbar_container)
    LinearLayout ll_toolbar_container;

    @OnClick(R.id.ib_back)
    void back() {
        toast("点击后退");
    }

    @OnClick(R.id.ib_forward)
    void forward() {
        toast("点击前进");
    }

    @OnClick(R.id.ib_menu)
    void showMenu() {
        toast("点击菜单");
        showPopupWindow();
    }

    @OnClick(R.id.ib_tab)
    void showTabs() {
        toast("点击标签");
        getShotInThread().subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                mTabsManager.setShot(bitmap);
                // TODO: 2017-7-17 这里可以优化成全局的fragment
                TabsFragment tabsFragment = new TabsFragment();
                addFragment(tabsFragment, R.id.fl_container_full, true);
            }
        });
    }

    @OnClick(R.id.ib_home)
    void home() {
        toast("点击主页");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test_main;
    }

    @Override
    protected BrowserActPresenter loadPresenter() {
        return new BrowserActPresenter();
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mPreferences = new PreferenceManager(this);
        mTabsManager = new TabsManager(this);
        initFragments();
        MainFragmentAdapter adapter = new MainFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);

        @SuppressWarnings("VariableNotUsedInsideIf")
        Intent intent = savedInstanceState == null ? getIntent() : null;

        boolean launchedFromHistory = intent != null && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        if (launchedFromHistory) {
            intent = null;
        }
        if (intent != null) {
            String url = intent.getDataString();
            if (!TextUtils.isEmpty(url)){
                searchTheWeb(url);
            }
        }
        setIntent(null);
    }

    private void initFragments() {
        mainFragment = new MainFragment();
        navigationFragment = new NavigationFragment();
        fragments.add(mainFragment);
        fragments.add(navigationFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BrowserFragment currentTab = mTabsManager.getCurrentTab();
            if(currentTab != null){
                if (currentTab.canGoBack()){
                    if (!currentTab.isShown()) {
                        onHideCustomView();
                        return true;
                    } else {
                        currentTab.goBack();
                        return true;
                    }
                }
            }else if (mCustomView != null || mCustomViewCallback != null) {
                onHideCustomView();
                return true;
            }else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (mainFragment.onBackPressed()) {
                    return true;
                } else if (keyCount < 1) {
                    keyCount++;
                    toast(R.string.exit_app_toast);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    private void showPopupWindow() {
        if (mPopupWindow == null) {
            View menuView = LayoutInflater.from(mActivity).inflate(R.layout.include_menu, null);
            menuView.findViewById(R.id.ib_add_bookmark).setOnClickListener(this);
            menuView.findViewById(R.id.ib_bookmark_history).setOnClickListener(this);
            menuView.findViewById(R.id.ib_refresh).setOnClickListener(this);
            menuView.findViewById(R.id.ib_setting).setOnClickListener(this);
            menuView.findViewById(R.id.ib_download).setOnClickListener(this);
            menuView.findViewById(R.id.ib_exit).setOnClickListener(this);
            mPopupWindow = new PopupWindow(menuView, CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);

            mPopupWindow.setFocusable(true);
            // 设置允许在外点击消失，必须和setBackgroundDrawable方法一起使用才有效
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.update();
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, ll_toolbar_container.getMeasuredHeight());
        } else {
            if (!mPopupWindow.isShowing()) {
                mPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, ll_toolbar_container.getMeasuredHeight());
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.ib_add_bookmark:
                dismissPopupWindow();
                toast("添加书签");
                break;
            case R.id.ib_bookmark_history:
                dismissPopupWindow();
                toast("书签/历史");
                break;
            case R.id.ib_refresh:
                dismissPopupWindow();
                toast("刷新");
                break;
            case R.id.ib_setting:
                dismissPopupWindow();
                toast("设置");
                break;
            case R.id.ib_download:
                dismissPopupWindow();
                toast("下载");
                break;
            case R.id.ib_exit:
                dismissPopupWindow();
                ActivityCollector.finishAll();
                break;
        }
    }

    private void dismissPopupWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    /**
     * 跳转到搜索页
     */
    public void jumpToSearch() {
        getShotInThread().subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                SearchFragment searchFragment = new SearchFragment();
                addFragment(searchFragment, R.id.fl_container_full, true);
            }
        });
    }

    /**
     * 跳转到搜索结果页
     */
    public void loadUrlInNewFragment(String query) {
        BrowserFragment fragment = BrowserFragment.newInstance(query, false);
        add(fragment);
        mTabsManager.updateCurrentTab(fragment);
    }

    /**
     * searches the web for the query fixing any and all problems with the input
     * checks if it is a search, url, etc.
     */
    public void searchTheWeb(@NonNull String query) {
        final BrowserFragment currentTab = mTabsManager.getCurrentTab();
        if (query.isEmpty()) {
            return;
        }
        String searchUrl = mSearchText + UrlUtils.QUERY_PLACE_HOLDER;
        query = query.trim();
        String urlFilter = UrlUtils.smartUrlFilter(query, true, searchUrl);
        if (currentTab != null) {
            currentTab.stopLoading();
            currentTab.loadUrl(urlFilter);
        } else {
            loadUrlInNewFragment(urlFilter);
        }
    }

    @Override
    public void onCreateWindow(Message resultMsg) {
        // TODO: 2017-7-14
    }

    @Override
    public void onCloseWindow() {
        // TODO: 2017-7-14
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        int requestedOrientation = mOriginalOrientation = getRequestedOrientation();
        onShowCustomView(view, callback, requestedOrientation);
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback, int requestedOrientation) {
        final BrowserFragment currentTab = mTabsManager.getCurrentTab();
        if (view == null || mCustomView != null) {
            if (callback != null) {
                try {
                    callback.onCustomViewHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding custom view", e);
                }
            }
            return;
        }
        try {
            view.setKeepScreenOn(true);
        } catch (SecurityException e) {
            Log.e(TAG, "WebView is not allowed to keep the screen on");
        }
        mOriginalOrientation = getRequestedOrientation();
        mCustomViewCallback = callback;
        mCustomView = view;

        setRequestedOrientation(requestedOrientation);
        final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();

        mFullscreenContainer = new FrameLayout(this);
        mFullscreenContainer.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black));
        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                mVideoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                mVideoView.setOnErrorListener(new VideoCompletionListener());
                mVideoView.setOnCompletionListener(new VideoCompletionListener());
            }
        } else if (view instanceof VideoView) {
            mVideoView = (VideoView) view;
            mVideoView.setOnErrorListener(new VideoCompletionListener());
            mVideoView.setOnCompletionListener(new VideoCompletionListener());
        }
        decorView.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        mFullscreenContainer.addView(mCustomView, COVER_SCREEN_PARAMS);
        decorView.requestLayout();
        setFullscreen(true, true);
        if (currentTab != null) {
            currentTab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onHideCustomView() {
        final BrowserFragment currentTab = mTabsManager.getCurrentTab();
        if (mCustomView == null || mCustomViewCallback == null) {
            if (mCustomViewCallback != null) {
                try {
                    mCustomViewCallback.onCustomViewHidden();
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding custom view", e);
                }
                mCustomViewCallback = null;
            }
            return;
        }
        Log.d(TAG, "onHideCustomView");
        if (currentTab != null) {
            currentTab.setVisibility(View.VISIBLE);
        }
        try {
            mCustomView.setKeepScreenOn(false);
        } catch (SecurityException e) {
            Log.e(TAG, "WebView is not allowed to keep the screen on");
        }
        setFullscreen(mPreferences.getHideStatusBarEnabled(), false);
        if (mFullscreenContainer != null) {
            ViewGroup parent = (ViewGroup) mFullscreenContainer.getParent();
            if (parent != null) {
                parent.removeView(mFullscreenContainer);
            }
            mFullscreenContainer.removeAllViews();
        }

        mFullscreenContainer = null;
        mCustomView = null;
        if (mVideoView != null) {
            Log.d(TAG, "VideoView is being stopped");
            mVideoView.stopPlayback();
            mVideoView.setOnErrorListener(null);
            mVideoView.setOnCompletionListener(null);
            mVideoView = null;
        }
        if (mCustomViewCallback != null) {
            try {
                mCustomViewCallback.onCustomViewHidden();
            } catch (Exception e) {
                Log.e(TAG, "Error hiding custom view", e);
            }
        }
        mCustomViewCallback = null;
        setRequestedOrientation(mOriginalOrientation);
    }

    @Override
    public void showSnackBar(@StringRes int resource) {
        Utils.showSnackbar(this, resource);
    }

    @Override
    public void setForwardButtonEnabled(boolean enabled) {

    }

    @Override
    public void setBackButtonEnabled(boolean enabled) {

    }

    /**
     * This method sets whether or not the activity will display
     * in full-screen mode (i.e. the ActionBar will be hidden) and
     * whether or not immersive mode should be set. This is used to
     * set both parameters correctly as during a full-screen video,
     * both need to be set, but other-wise we leave it up to user
     * preference.
     *
     * @param enabled   true to enable full-screen, false otherwise
     * @param immersive true to enable immersive mode, false otherwise
     */
    private void setFullscreen(boolean enabled, boolean immersive) {
        mIsFullScreen = enabled;
        mIsImmersive = immersive;
        Window window = getWindow();
        View decor = window.getDecorView();
        if (enabled) {
            if (immersive) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public Bitmap getShot() {
        // 获取windows中最顶层的view
        View view = getWindow().getDecorView();
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        int statusBarHeights = rect.top;
        Display display = getWindowManager().getDefaultDisplay();

        // 获取屏幕宽和高
        int widths = display.getWidth();
        int heights = display.getHeight();

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights / 2 - statusBarHeights);

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
    }

    private Observable<Bitmap> getShotInThread() {
        // 屏幕截图
        final Bitmap shot = getShot();
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap shotCompressed = CompressImage.compressImage(shot);
                subscriber.onNext(shotCompressed);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener,
            MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }

    }

    public TabsManager getTabModel() {
        return mTabsManager;
    }

    public void show(BaseFragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.show(fragment);
            transaction.commit();
        }
    }

    public void hide(BaseFragment fragment) {
        if (fragment != null && !fragment.isHidden()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(fragment);
            transaction.commit();
        }
    }

    public void add(BaseFragment fragment) {
        if (fragment != null) {
            addFragment(fragment, R.id.fl_container_except_bottom, true);
        }
    }

    public void remove(BaseFragment fragment) {
        if (fragment == null) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTabsManager != null) {
            mTabsManager.pauseAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializePreferences();
        if (mTabsManager != null) {
            mTabsManager.resumeAll();
        }
    }

    private void initializePreferences() {
        mFullScreen = mPreferences.getFullScreenEnabled();

        setFullscreen(mPreferences.getHideStatusBarEnabled(), false);

        switch (mPreferences.getSearchChoice()) {
            case 0:
                mSearchText = mPreferences.getSearchUrl();
                if (!mSearchText.startsWith(Constants.HTTP)
                        && !mSearchText.startsWith(Constants.HTTPS)) {
                    mSearchText = Constants.BAIDU_SEARCH;
                }
                break;
            case 1:
                mSearchText = Constants.BAIDU_SEARCH;
                break;
            case 2:
                mSearchText = Constants.GOOGLE_SEARCH;
                break;
            case 3:
                mSearchText = Constants.BING_SEARCH;
                break;
            case 4:
                mSearchText = Constants.YAHOO_SEARCH;
                break;
            case 5:
                mSearchText = Constants.SEARCH_360;
                break;
        }
    }
}

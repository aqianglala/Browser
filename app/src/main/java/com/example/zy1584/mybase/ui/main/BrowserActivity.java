package com.example.zy1584.mybase.ui.main;

import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
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
import com.example.zy1584.mybase.manager.TabsManager;
import com.example.zy1584.mybase.ui.main.adapter.MainFragmentAdapter;
import com.example.zy1584.mybase.ui.main.mvp.BrowserActContract;
import com.example.zy1584.mybase.ui.main.mvp.BrowserActPresenter;
import com.example.zy1584.mybase.ui.navigation.NavigationFragment;
import com.example.zy1584.mybase.ui.search.SearchFragment;
import com.example.zy1584.mybase.utils.ActivityCollector;
import com.example.zy1584.mybase.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static com.example.zy1584.mybase.R.id.viewPager;

public class BrowserActivity extends BaseActivity<BrowserActPresenter> implements BrowserActContract.ActView{
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MainFragment mainFragment;
    private NavigationFragment navigationFragment;
    private PopupWindow mPopupWindow;
    private int keyCount;
    private TabsManager mTabsManager;

    private PreferenceManager mPreferences;

    private int mOriginalOrientation;
    private boolean mIsFullScreen = false;
    private boolean mIsImmersive = false;
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
    private BrowserFragment mCurrentFrg;

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
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mPreferences = new PreferenceManager(this);
        mTabsManager = new TabsManager(this);
        initFragments();
        MainFragmentAdapter adapter = new MainFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
    }

    private void initFragments() {
        mainFragment = new MainFragment();
        navigationFragment = new NavigationFragment();
        fragments.add(mainFragment);
        fragments.add(navigationFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            // TODO: 2017-7-14 处理全屏播放时返回键问题
            if (mCustomView != null || mCustomViewCallback != null) {
                onHideCustomView();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 0){
                if (mainFragment.onBackPressed()){
                    return true;
                }else if (keyCount<1){
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
    public void jumpToSearch(){
//        SearchFragment fragment = new SearchFragment();
        mCurrentFrg = new BrowserFragment();
        addFragment(mCurrentFrg, R.id.fl_container_full, true);
    }

    /**
     * 跳转到搜索结果页
     */
    public void jumpToSearchResult(){
        SearchFragment searchFragment = new SearchFragment();
        addFragment(searchFragment, R.id.fl_container_except_bottom, true);
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
        mCurrentFrg.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onHideCustomView() {
        if (mCustomView == null || mCustomViewCallback == null || mCurrentFrg == null) {
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
        mCurrentFrg.setVisibility(View.VISIBLE);
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
    public void showSnackbar(@StringRes int resource) {
        Utils.showSnackbar(this, resource);
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

    public TabsManager getTabModel(){
        return mTabsManager;
    }

    public void show(Fragment fragment){
        if (fragment != null && fragment.isHidden()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.show(fragment);
            transaction.commit();
        }
    }

    public void hide(Fragment fragment){
        if (fragment != null && !fragment.isHidden()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(fragment);
            transaction.commit();
        }
    }

    public void add(Fragment fragment, boolean isShow){
        if (fragment != null && !fragment.isHidden()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fl_container_except_bottom, fragment);
            if (!isShow){
                transaction.hide(fragment);
            }
            transaction.commit();
        }
    }

}

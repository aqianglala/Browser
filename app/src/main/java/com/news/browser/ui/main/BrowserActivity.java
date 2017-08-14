package com.news.browser.ui.main;

import android.app.Activity;
import android.app.Dialog;
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
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BaseApplication;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.EngineBean;
import com.news.browser.bean.EngineBean.EngineItem;
import com.news.browser.bean.UpgradeBean;
import com.news.browser.bus.RXEvent;
import com.news.browser.db.EngineDatabase;
import com.news.browser.manager.TabsManager;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.service.UpdateService;
import com.news.browser.service.UpdateService.IAppStoreUpdateReturn;
import com.news.browser.service.UpdateService.IDetailAppListener;
import com.news.browser.service.UpdateService.IFileDownloadInfoReturn;
import com.news.browser.ui.bookmark.BookmarkEditActivity;
import com.news.browser.ui.bookmark.BookmarkHistoryFragment;
import com.news.browser.ui.download.DownloadManagerActivity;
import com.news.browser.ui.hotSite.HotSiteFragment;
import com.news.browser.ui.main.adapter.MainFragmentAdapter;
import com.news.browser.ui.main.mvp.BrowserActContract;
import com.news.browser.ui.main.mvp.BrowserActPresenter;
import com.news.browser.ui.navigation.HotTagFragment;
import com.news.browser.ui.qrcode.SimpleCaptureActivity;
import com.news.browser.ui.search.SearchFragment;
import com.news.browser.ui.setting.SettingsActivity;
import com.news.browser.ui.windowManager.WindowManagerFragmentNew;
import com.news.browser.utils.ActivityCollector;
import com.news.browser.utils.CompressImage;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.UIUtils;
import com.news.browser.utils.UrlUtils;
import com.news.browser.utils.Utils;
import com.news.browser.widget.OutsideViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class BrowserActivity extends BaseActivity<BrowserActPresenter> implements BrowserActContract.ActView,
        IDetailAppListener, IAppStoreUpdateReturn, IFileDownloadInfoReturn {
    private static final int REQUEST_QR_CODE = 1;
    private static final int API = android.os.Build.VERSION.SDK_INT;

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MainFragment mainFragment;
    private HotTagFragment hotTagFragment;
    private PopupWindow mMenuPopupWindow;
    private PopupWindow mBookmarkPopupWindow;
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

    private Subscription rxSubscription;
    private ProgressBar downloading_pb_task;
    private TextView downloading_tv_status;
    private TextView downloading_tv_speed;
    private Dialog downloading_dialog;

    private boolean isIgnore = false;

    private EngineDatabase mEngineDatabase;
    private List<EngineItem> mEngineItems = new ArrayList<>();
    private EngineItem mDefaultEngine;

    @BindView(R.id.viewPager)
    OutsideViewPager mViewPager;

    @BindView(R.id.ll_toolbar_container)
    LinearLayout ll_toolbar_container;

    @BindView(R.id.rl_update_bar)
    RelativeLayout rl_update_bar;

    @BindView(R.id.tv_status)
    TextView tv_status;

    @BindView(R.id.tv_ignore)
    TextView tv_ignore;

    @BindView(R.id.tv_install)
    TextView tv_install;

    @BindView(R.id.ll_update_button)
    LinearLayout ll_update_button;

    @BindView(R.id.tv_complete)
    TextView tv_complete;;

    @BindView(R.id.tv_tab_num)
    TextView tv_tab_num;

    @OnClick(R.id.tv_complete)
    void onEditComplete(){
        if (hotTagFragment != null){
            showCompleteButton(false);
            hotTagFragment.onEditComplete();
            setPagingEnabled(true);
        }
    }

    @OnClick(R.id.tv_ignore)
    void ignore(){
        isIgnore = true;
        setViewVisible(rl_update_bar, false);
    }

    @OnClick(R.id.tv_install)
    void install(){
        if (!UpdateService.normalInstall(this))
        {
            isIgnore = true;
            setViewVisible(rl_update_bar, false);
            toast("文件已被删除！");
        }
    }

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
        showMenuWindow();
    }

    @OnClick(R.id.ib_tab)
    void showTabs() {
        toast("点击标签");
        getShotInThread().subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                mTabsManager.setShot(bitmap);
                // TODO: 2017-7-17 这里可以优化成全局的fragment
                WindowManagerFragmentNew windowManagerFragment = new WindowManagerFragmentNew();
                addFragment(windowManagerFragment, R.id.fl_container_full, true);
            }
        });
    }

    @OnClick(R.id.ib_home)
    void home() {
        if (!mainFragment.home()){
            int currentItem = mViewPager.getCurrentItem();
            mViewPager.setCurrentItem(currentItem == 0 ? 1 : 0);
        }
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
        mPreferences = PreferenceManager.getInstance();
        mTabsManager = new TabsManager(this);
        mEngineDatabase = EngineDatabase.getInstance();
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
            if (!TextUtils.isEmpty(url)) {
                searchTheWeb(url);
            }
        }
        setIntent(null);
        rxSubscription = RxBus.getInstance().toObservable(RXEvent.class)
                .subscribe(new Action1<RXEvent>() {
                    @Override
                    public void call(RXEvent rxEvent) {
                        String tag = rxEvent.getTag();
                        if (tag == RXEvent.TAG_BROWSER_MSG) {
                            showSnackBar(rxEvent.getMsgId());
                        } else if (tag == RXEvent.TAG_SEARCH) {
                            searchTheWeb(rxEvent.getMsg());
                        }
                    }
                });
        // 检查更新
        BaseApplication.startCheckAppStoreUpdate(this, true);
    }

    private void initFragments() {
        mainFragment = new MainFragment();
        hotTagFragment = new HotTagFragment();
        fragments.add(mainFragment);
        fragments.add(hotTagFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BrowserFragment currentTab = mTabsManager.getCurrentTab();
            if (currentTab != null) {
                if (currentTab.canGoBack()) {
                    if (!currentTab.isShown()) {
                        onHideCustomView();
                        return true;
                    } else {
                        currentTab.goBack();
                        return true;
                    }
                }
            } else if (mCustomView != null || mCustomViewCallback != null) {
                onHideCustomView();
                return true;
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
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


    private void showMenuWindow() {
        if (mMenuPopupWindow == null) {
            View menuView = LayoutInflater.from(mActivity).inflate(R.layout.include_menu, (ViewGroup) mContentView, false);
            menuView.findViewById(R.id.ll_add_bookmark).setOnClickListener(this);
            menuView.findViewById(R.id.ll_bookmark_history).setOnClickListener(this);
            menuView.findViewById(R.id.ll_refresh).setOnClickListener(this);
            menuView.findViewById(R.id.ll_setting).setOnClickListener(this);
            menuView.findViewById(R.id.ll_download).setOnClickListener(this);
            menuView.findViewById(R.id.ll_exit).setOnClickListener(this);

            LinearLayout ll_refresh = (LinearLayout) menuView.findViewById(R.id.ll_refresh);
            LinearLayout ll_add_bookmark = (LinearLayout) menuView.findViewById(R.id.ll_add_bookmark);

            int height = UIUtils.getDimen(R.dimen.menu_height);
            mMenuPopupWindow = new PopupWindow(menuView, CoordinatorLayout.LayoutParams.MATCH_PARENT, height);

            mMenuPopupWindow.setFocusable(true);
            // 设置允许在外点击消失，必须和setBackgroundDrawable方法一起使用才有效
            mMenuPopupWindow.setOutsideTouchable(true);
            mMenuPopupWindow.update();
            mMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mMenuPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            mMenuPopupWindow.setOutsideTouchable(true);
            mMenuPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, ll_toolbar_container.getMeasuredHeight());

        } else {
            if (!mMenuPopupWindow.isShowing()) {
                mMenuPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, ll_toolbar_container.getMeasuredHeight());
            }
        }
    }

    private void showAddBookmarkWindow(String url) {
        if (mBookmarkPopupWindow == null) {
            View menuView = LayoutInflater.from(mActivity).inflate(R.layout.include_add_bookmark_window, (ViewGroup) mContentView, false);
            menuView.findViewById(R.id.ib_edit).setOnClickListener(this);
            menuView.findViewById(R.id.ll_bookmark).setOnClickListener(this);
            menuView.findViewById(R.id.ll_bookmark_home).setOnClickListener(this);
            TextView tv_title = (TextView) menuView.findViewById(R.id.tv_title);
            tv_title.setText(url);
            mBookmarkPopupWindow = new PopupWindow(menuView, CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);

            mBookmarkPopupWindow.setFocusable(true);
            // 设置允许在外点击消失，必须和setBackgroundDrawable方法一起使用才有效
            mBookmarkPopupWindow.setOutsideTouchable(true);
            mBookmarkPopupWindow.update();
            mBookmarkPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mBookmarkPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            mBookmarkPopupWindow.setOutsideTouchable(true);
            mBookmarkPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, ll_toolbar_container.getMeasuredHeight());

        } else {
            if (!mBookmarkPopupWindow.isShowing()) {
                mBookmarkPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, ll_toolbar_container.getMeasuredHeight());
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        BrowserFragment currentTab = mTabsManager.getCurrentTab();
        switch (v.getId()) {
//            菜单按钮
            case R.id.ll_add_bookmark:
                dismissPopupWindow();
                if (currentTab == null) {
                    toast("首页不可点击");
                } else {
                    String url = currentTab.getUrl();
                    showAddBookmarkWindow(url);
                    toast("添加书签");
                }
                break;
            case R.id.ll_bookmark_history:
                dismissPopupWindow();
                BookmarkHistoryFragment bookmarkHistoryFragment = new BookmarkHistoryFragment();
                addFragment(bookmarkHistoryFragment, R.id.fl_container_full, true);
                break;
            case R.id.ll_refresh:
                dismissPopupWindow();
                toast("刷新");
                break;
            case R.id.ll_setting:
                dismissPopupWindow();
                gotoActivity(SettingsActivity.class);
                break;
            case R.id.ll_download:
                dismissPopupWindow();
                toast("下载");
                gotoActivity(DownloadManagerActivity.class);
                break;
            case R.id.ll_exit:
                dismissPopupWindow();
                ActivityCollector.finishAll();
                break;
//                书签窗口按钮
            case R.id.ib_edit:
                if (currentTab != null) {
                    String url = currentTab.getUrl();
                    String title = currentTab.getTitle();
                    Bundle bundle = new Bundle();
                    bundle.putString(GlobalParams.URL, url);
                    bundle.putString(GlobalParams.TITLE, title);
                    gotoActivity(BookmarkEditActivity.class, false, bundle);
                }
                break;
            case R.id.ll_bookmark:
                currentTab.bookmarkCurrentPage();
                break;
            case R.id.ll_bookmark_home:
                break;
        }
    }

    private void dismissPopupWindow() {
        if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()) {
            mMenuPopupWindow.dismiss();
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
        String searchUrl = null;
        if (mPreferences.getSearchUrl().equals(mSearchText)){
            searchUrl = mSearchText + UrlUtils.QUERY_PLACE_HOLDER;
        }
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
                    Log.e(TAG, "Error hiding custom mContentView", e);
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
                    Log.e(TAG, "Error hiding custom mContentView", e);
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
                Log.e(TAG, "Error hiding custom mContentView", e);
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

    @Override
    public void receiveSearchEngine(EngineBean bean) {
        if (bean.getData() != null){
            // 验证数据是否正确
            boolean hasDefault = false;
            for (EngineItem item : bean.getData()){
                if (item.getIsDefault() == 1){
                    hasDefault = true;
                }
            }
            if (hasDefault){
                mEngineItems.clear();
                mEngineItems.addAll(bean.getData());
                for (EngineItem item : bean.getData()){
                    mEngineDatabase.addEngineItem(item);
                    if (item.getIsDefault() == 1){
                        mDefaultEngine = item;
                        mSearchText = mDefaultEngine.getAddrUrl();
                    }
                }
            }
        }
    }

    @Override
    public void onReceiveEngineError(Throwable e) {

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
                statusBarHeights, widths, heights - statusBarHeights);

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

    @Override
    public void receiveAppStoreUpdateInfo(int status) {
        if (UpdateService.APP_DOWNLOAD_STATUS_DOWNLOADING == status
                || UpdateService.APP_DOWNLOAD_STATUS_INSTALLING == status) {
            setViewVisible(rl_update_bar, true);
            setViewVisible(ll_update_button, false);
            tv_status.setText("浏览器正在零流量更新中...");
            isIgnore = false;
        } else if (UpdateService.APP_DOWNLOAD_STATUS_DOWNLOADED == status) {
            if (!isIgnore){
                UpdateService.resetFileDownloadInterface();
                setViewVisible(rl_update_bar, true);
                setViewVisible(ll_update_button, true);
                tv_status.setText("浏览器更新包准备好了");
            }
        } else if (UpdateService.APP_DOWNLOAD_STATUS_FAIL == status) {
            setViewVisible(rl_update_bar, false);
        } else {
            setViewVisible(rl_update_bar, false);
        }
    }

    private void setViewVisible(View view, boolean isShow)
    {
        if (null != view)
        {
            view.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void receiveDetailAppInfo(boolean isNewVersion, UpgradeBean bean) {
        if (!isNewVersion) return;
        showUpdateDialog(bean);
    }

    /**
     * 显示检查更新框
     * @param bean
     */
    private void showUpdateDialog(final UpgradeBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_dialog_update, null);

        TextView tv_version = (TextView) layout.findViewById(R.id.tv_version);
        TextView tv_size = (TextView) layout.findViewById(R.id.tv_size);
        TextView tv_description = (TextView) layout.findViewById(R.id.tv_description);

        TextView tv_left = (TextView) layout.findViewById(R.id.tv_left);
        TextView tv_right = (TextView) layout.findViewById(R.id.tv_right);

        UpgradeBean.DataBean info = bean.getData().get(0);

        tv_version.setText(info.getVersionName());
        tv_size.setText(Utils.formatFileSize(info.getFileSize()) + "M");
        tv_description.setText(info.getNoticeContent());

        tv_left.setText(info.getIsForce() == 0 ? "下次更新" : "退出");
        tv_right.setText("立即更新");

        builder.setView(layout);
        final Dialog dialog = builder.show();

        tv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                TextView view = (TextView) v;
                String str = view.getText().toString();
                if ("退出".equals(str)){
                    ActivityCollector.finishAll();
                }
            }
        });
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                UpdateService.setCheckVersionInsterface(BrowserActivity.this, BrowserActivity.this, BrowserActivity.this);
                UpdateService.startDownloadAppStore(bean);
            }
        });
    }

    @Override
    public void receiveFileDownloadInfo(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        if (downloading_dialog == null){
            showDownloadingDialog(task, soFarBytes, totalBytes);
        }else {
            if (downloading_dialog.isShowing()){
                if (task.getStatus() == FileDownloadStatus.completed){
                    downloading_dialog.dismiss();
                    UpdateService.installApp();
                }else{
                    updateDownloadingDialog(task, soFarBytes, totalBytes);
                }
            }
        }
    }

    private void updateDownloadingDialog(BaseDownloadTask task, int soFarBytes, float totalBytes) {
        final float percent = soFarBytes / totalBytes;
        downloading_pb_task.setMax(100);
        downloading_pb_task.setProgress((int) (percent * 100));

        downloading_tv_status.setText("已下载：" +  (int) (percent * 100) + "%");
        downloading_tv_speed.setText(Utils.formatSpeed(task.getSpeed()));
    }

    public void showDownloadingDialog(BaseDownloadTask task, int sofar, int total) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_dialog_progress, null);

        downloading_pb_task = (ProgressBar) layout.findViewById(R.id.pb_task);
        downloading_tv_status = (TextView) layout.findViewById(R.id.tv_status);
        downloading_tv_speed = (TextView) layout.findViewById(R.id.tv_speed);

        updateDownloadingDialog(task, sofar, total);

        builder.setCancelable(false);
        builder.setView(layout);
        downloading_dialog = builder.show();

        layout.findViewById(R.id.tv_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloading_dialog.dismiss();
                UpdateService.cancelDownloadAppStore();
            }
        });

        layout.findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloading_dialog.dismiss();
                UpdateService.resetFileDownloadInterface();
            }
        });

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
            transaction.commitAllowingStateLoss();
        }
    }

    public void hide(BaseFragment fragment) {
        if (fragment != null && !fragment.isHidden()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public void add(BaseFragment fragment) {
        if (fragment != null) {
            addFragment(fragment, R.id.fl_container_except_bottom, true);
        }
    }

    public void showHotSiteFragment() {
        HotSiteFragment fragment = new HotSiteFragment();
//        if (fragment != null) {
//            addFragment(fragment, R.id.fl_container_full, true);
//        }
        add(fragment);
    }

    public void remove(BaseFragment fragment) {
        if (fragment == null) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTabsManager != null) {
            mTabsManager.pauseAll();
        }
        UpdateService.resetListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializePreferences();
        if (mTabsManager != null) {
            mTabsManager.resumeAll();
        }
        UpdateService.setCheckVersionInsterface(this, this, this);
        receiveAppStoreUpdateInfo(UpdateService.getAppStoreStatus());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!rxSubscription.isUnsubscribed()) {
            rxSubscription.unsubscribe();
        }
        FileDownloader.getImpl().unBindServiceIfIdle();
//        FileDownloadMonitor.releaseGlobalMonitor();
    }

    private Observable<List<EngineItem>> getEngineList() {
        return Observable.create(new Observable.OnSubscribe<List<EngineItem>>() {
            @Override
            public void call(Subscriber<? super List<EngineItem>> subscriber) {
                List<EngineItem> engineList = mEngineDatabase.getAllEngineItems();
                subscriber.onNext(engineList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<EngineItem> getDefaultEngine() {
        return Observable.create(new Observable.OnSubscribe<EngineItem>() {
            @Override
            public void call(Subscriber<? super EngineItem> subscriber) {
                EngineItem item = mEngineDatabase.getDefaultEngine();
                subscriber.onNext(item);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void initializePreferences() {
        mFullScreen = mPreferences.getFullScreenEnabled();

        setFullscreen(mPreferences.getHideStatusBarEnabled(), false);

        // 从数据库中获取搜索引擎列表，设置默认搜索引擎
        getEngineList().subscribe(new Action1<List<EngineItem>>() {
            @Override
            public void call(List<EngineItem> engineItems) {
                if (engineItems != null && engineItems.size() > 0){
                    mEngineItems.clear();
                    mEngineItems.addAll(engineItems);
                }else{
                    mPresenter.getSearchEngine();
                }
            }
        });

        getDefaultEngine().subscribe(new Action1<EngineItem>() {
            @Override
            public void call(EngineItem engineItem) {
                mDefaultEngine = engineItem;
                if (mDefaultEngine == null){
                    mSearchText = mPreferences.getSearchUrl();
                }else {
                    mSearchText = mDefaultEngine.getAddrUrl();
                }
            }
        });
    }

    public List<EngineItem> getEngineItems() {
        return mEngineItems;
    }

    public EngineItem getDefaultEngineItem() {
        return mDefaultEngine;
    }

    public void setEngineItems(List<EngineItem> mEngineItems) {
        this.mEngineItems = mEngineItems;
    }

    public void setDefaultEngine(EngineItem mDefaultEngine) {
        this.mDefaultEngine = mDefaultEngine;
        mSearchText = mDefaultEngine.getAddrUrl();
    }

    public void updateEngineDB(EngineItem defaultEngine){
        mEngineDatabase.setDefaultEngine(defaultEngine.getAddrUrl());
    }

    public void openQrcode() {
        Intent i = new Intent(BrowserActivity.this, SimpleCaptureActivity.class);
        BrowserActivity.this.startActivityForResult(i, REQUEST_QR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    Bundle extras = intent.getExtras();
                    String result = extras.getString("result");
                    searchTheWeb(result);
                }
            }
        }

    }

    public void showCompleteButton(boolean isShow){
        tv_complete.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setTabNum(int position){
        tv_tab_num.setText(String.valueOf(position + 1));
    }

    public void setPagingEnabled(boolean isPagingEnable){
        mViewPager.setPagingEnabled(isPagingEnable);
    }
}

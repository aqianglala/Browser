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
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.news.browser.bean.HotTagBean;
import com.news.browser.bean.UpgradeBean;
import com.news.browser.bus.RXEvent;
import com.news.browser.db.EngineDatabase;
import com.news.browser.db.HotTagDatabase;
import com.news.browser.manager.TabsManager;
import com.news.browser.manager.TabsManager.TabNumberChangedListener;
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
import com.news.browser.ui.main.db.BookmarkManager;
import com.news.browser.ui.main.mvp.BrowserActContract;
import com.news.browser.ui.main.mvp.BrowserActPresenter;
import com.news.browser.ui.navigation.HotTagFragment;
import com.news.browser.ui.qrcode.SimpleCaptureActivity;
import com.news.browser.ui.search.SearchFragment;
import com.news.browser.ui.setting.SettingsActivity;
import com.news.browser.ui.windowManager.WindowManagerFragmentNew;
import com.news.browser.utils.ActivityCollector;
import com.news.browser.utils.CompressImage;
import com.news.browser.utils.Constants;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.SPUtils;
import com.news.browser.utils.ScreenUtils;
import com.news.browser.utils.UIUtils;
import com.news.browser.utils.UrlUtils;
import com.news.browser.utils.Utils;
import com.news.browser.widget.OutsideViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class BrowserActivity extends BaseActivity<BrowserActPresenter> implements BrowserActContract.ActView,
        TabNumberChangedListener, IDetailAppListener, IAppStoreUpdateReturn, IFileDownloadInfoReturn {
    private static final int REQUEST_QR_CODE = 1;
    private static final int API = android.os.Build.VERSION.SDK_INT;

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MainFragment mainFragment;
    private HotTagFragment hotTagFragment;
    private PopupWindow mMenuPopupWindow;
    private PopupWindow mBookmarkPopupWindow;
    private TabsManager mTabsManager;
    private String mSearchText;

    private PreferenceManager mPreferences;
    private BookmarkManager mBookmarkManager;
    private HotTagDatabase mHotTagDatabase;

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

    private ImageView iv_refresh;
    private ImageView iv_add_bookmark;
    private TextView tv_add_bookmark;
    private TextView tv_refresh;

    // 保存上一个页面的状态
    private Map<Integer, BrowserFragment> tabStatusMap = new HashMap<>();

    private final String STACK_INCLUDE_BOTTOM = "stack_include_bottom";
    private final String STACK_EXCEPT_BOTTOM = "stack_except_bottom";

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
    TextView tv_complete;

    @BindView(R.id.tv_tab_num)
    TextView tv_tab_num;

    @BindView(R.id.ib_forward)
    ImageButton ib_forward;

    @BindView(R.id.ib_back)
    ImageButton ib_back;
    private LinearLayout ll_add_bookmark;
    private LinearLayout ll_refresh;
    private ImageView iv_added_bookmark;
    private ImageView iv_added_home;
    private TextView tv_title_add_bookmark;
    private WindowManagerFragmentNew mWindowManagerFragment;
    private BookmarkHistoryFragment mBookmarkHistoryFragment;
    private SearchFragment mSearchFragment;
    private HotSiteFragment mHotSiteFragment;
    private Dialog upgradeDialog;

    @OnClick(R.id.tv_complete)
    void onEditComplete() {
        if (hotTagFragment != null) {
            showCompleteButton(false);
            hotTagFragment.onEditComplete();
            setPagingEnabled(true);
        }
    }

    @OnClick(R.id.tv_ignore)
    void ignore() {
        isIgnore = true;
        setViewVisible(rl_update_bar, false);
    }

    @OnClick(R.id.tv_install)
    void install() {
        if (!UpdateService.normalInstall(this)) {
            isIgnore = true;
            setViewVisible(rl_update_bar, false);
            toast("文件已被删除！");
        }
    }

    @OnClick(R.id.ib_back)
    void back() {
        if (removeOtherFragment()) return;
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (currentTab != null) {
            if (currentTab.canGoBack()) {
                if (!currentTab.isShown()) {
                    onHideCustomView();
                } else {
                    currentTab.goBack();
                }
            } else {
                saveCurrentTab(currentTab);
            }
        } else if (mCustomView != null || mCustomViewCallback != null) {
            onHideCustomView();
        }
    }

    /**
     * 如果当前页是搜索、书签历史、窗口页、热门网站页，则将其移除
     *
     * @return
     */
    private boolean removeOtherFragment() {
        if (mSearchFragment != null && mSearchFragment.isVisible()) {
            remove(mSearchFragment);
            setButtonEnable();
            return true;
        }
        if (mBookmarkHistoryFragment != null && mBookmarkHistoryFragment.isVisible()) {
            remove(mBookmarkHistoryFragment);
            setButtonEnable();
            return true;
        }
        if (mWindowManagerFragment != null && mWindowManagerFragment.isVisible()) {
            remove(mWindowManagerFragment);
            setButtonEnable();
            return true;
        }
        if (mTabsManager.getCurrentFragment() == null && mHotSiteFragment != null && mHotSiteFragment.isVisible()) {
            remove(mHotSiteFragment);
            setButtonEnable();
            return true;
        }
        return false;
    }

    /**
     * 按右下角的home键，将所有的fragment移除
     * @return
     */
    private void removeExceptBrowser() {
        if (mSearchFragment != null) {
            remove(mSearchFragment);
        }
        if (mBookmarkHistoryFragment != null) {
            remove(mBookmarkHistoryFragment);
        }
        if (mWindowManagerFragment != null) {
            remove(mWindowManagerFragment);
        }
        if (mHotSiteFragment != null) {
            remove(mHotSiteFragment);
        }
        ib_back.setEnabled(false);
        ib_forward.setEnabled(false);
    }

    /**
     * 设置前进后退按钮是否可操作
     */
    public void setButtonEnable() {
//        if (checkButton()) return;
        BrowserFragment currentFragment = mTabsManager.getCurrentFragment();
        if (currentFragment != null) {
            ib_back.setEnabled(true);
            ib_forward.setEnabled(currentFragment.canGoForward());
        } else {
            BrowserFragment browserFragment = tabStatusMap.get(mTabsManager.getCurrentIndex());
            ib_back.setEnabled(false);
            if (browserFragment != null) {
                ib_forward.setEnabled(true);
            } else {
                ib_forward.setEnabled(false);
            }
        }
    }

    /**
     * 如果栈顶是搜索页、书签历史页、窗口页或热门网站页，则返回键置为可见，前进键不可见
     *
     * @return true表示已处理
     */
    private boolean checkButton() {
        if ((mSearchFragment != null && mSearchFragment.isVisible())
                || (mBookmarkHistoryFragment != null && mBookmarkHistoryFragment.isVisible())
                || (mWindowManagerFragment != null && mWindowManagerFragment.isVisible())
                || (mHotSiteFragment != null && mHotSiteFragment.isVisible())) {
            ib_back.setEnabled(true);
            ib_forward.setEnabled(false);
            return true;
        }
        return false;
    }

    /**
     * 当从网页中返回到主页面时，保存网页并更新tab列表、后退按钮、前进按钮、添加书签按钮及更新按钮
     *
     * @param currentTab
     */
    private void saveCurrentTab(BrowserFragment currentTab) {
        int currentIndex = mTabsManager.getCurrentIndex();
        BrowserFragment fragment = tabStatusMap.get(currentIndex);
        if (fragment != null && fragment != currentTab) {
            remove(fragment);
        }
        pauseFragment(currentTab);
        hideBrowserFragment(currentTab);
        mTabsManager.backToHome(currentTab);
        tabStatusMap.put(currentIndex, currentTab);
        ib_back.setEnabled(mHotSiteFragment == null || !mHotSiteFragment.isVisible() ? false : true);
        ib_forward.setEnabled(true);
    }

    private void pauseFragment(BrowserFragment currentTab) {
        currentTab.pauseTimers();
        currentTab.onPause();
    }

    private void resumeFragment(BrowserFragment currentTab) {
        currentTab.resumeTimers();
        currentTab.onResume();

        currentTab.updateUrl(currentTab.getUrl(), true);
        currentTab.updateProgress(currentTab.getProgress());
    }

    private void setMenuButtonEnable(boolean isEnable) {
        ll_add_bookmark.setEnabled(isEnable);
        ll_refresh.setEnabled(isEnable);
        iv_add_bookmark.setEnabled(isEnable);
        tv_add_bookmark.setEnabled(isEnable);
        iv_refresh.setEnabled(isEnable);
        tv_refresh.setEnabled(isEnable);
    }

    @OnClick(R.id.ib_forward)
    void forward() {
        int currentIndex = mTabsManager.getCurrentIndex();
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (currentTab != null) {
            currentTab.goForward();
        } else {
            BrowserFragment browserFragment = tabStatusMap.get(currentIndex);
            if (browserFragment != null) {
                resumeFragment(browserFragment);

                showBrowserFragment(browserFragment);
                mTabsManager.updateCurrentTab(browserFragment);
                ib_back.setEnabled(true);
                ib_forward.setEnabled(browserFragment.canGoForward());
            }
        }
    }

    @OnClick(R.id.ib_menu)
    void showMenu() {
        showMenuWindow();
    }

    @OnClick(R.id.ib_tab)
    void showTabs() {
        getShotInThread().subscribe(new Action1<Bitmap>() {
            @Override
            public void call(Bitmap bitmap) {
                mTabsManager.setShot(bitmap);
                // TODO: 2017-7-17 这里可以优化成全局的fragment
                mWindowManagerFragment = new WindowManagerFragmentNew();
                addFragment(mWindowManagerFragment, R.id.fl_container_full, true, STACK_INCLUDE_BOTTOM);
            }
        });
    }

    @OnClick(R.id.ib_home)
    void home() {
        removeExceptBrowser();
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (currentTab != null) {
            saveCurrentTab(currentTab);
            mainFragment.home();
        } else {
            if (!mainFragment.home()) {
                int currentItem = mViewPager.getCurrentItem();
                mViewPager.setCurrentItem(currentItem == 0 ? 1 : 0);
            }
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
    protected void initView() {
        super.initView();
        ib_back.setEnabled(false);
        ib_forward.setEnabled(false);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mPreferences = PreferenceManager.getInstance();
        mBookmarkManager = BookmarkManager.getInstance();
        mHotTagDatabase = HotTagDatabase.getInstance();
        mTabsManager = new TabsManager(this);
        mTabsManager.setTabNumberChangedListener(this);
        mEngineDatabase = EngineDatabase.getInstance();
        initializePreferences();
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
                SPUtils.put(GlobalParams.LAUNCH_TYPE, "2");// 外部启动
                searchTheWeb(url);
            } else {
                SPUtils.put(GlobalParams.LAUNCH_TYPE, "1");// 普通启动
            }
        } else {
            SPUtils.put(GlobalParams.LAUNCH_TYPE, "1");// 普通启动
        }
        setIntent(null);
        rxSubscription = RxBus.getInstance().toObservable(RXEvent.class)
                .subscribe(new Action1<RXEvent>() {
                    @Override
                    public void call(RXEvent rxEvent) {
                        String tag = rxEvent.getTag();
                        if (RXEvent.TAG_BROWSER_MSG.equals(tag)) {
//                            showSnackBar(rxEvent.getMsgId());
                            toast(rxEvent.getMsgId());
                        } else if (RXEvent.TAG_SEARCH.equals(tag)) {
                            searchTheWeb(rxEvent.getMsg());
                        } else if (RXEvent.TAG_UPDATE_DEFAULT_ENGINE.equals(tag)) {
                            updateEngineList();
                            updateDefaultEngine();
                        } else if (RXEvent.TAG_UPDATE_TAB_SIZE.equals(tag)) {
                            tv_tab_num.setText(String.valueOf(mTabsManager.getTabList().size()));
                        }
                    }
                });
        // 检查更新
        BaseApplication.startCheckAppUpdate(this, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            String url = intent.getDataString();
            if (!TextUtils.isEmpty(url)) {
                SPUtils.put(GlobalParams.LAUNCH_TYPE, "2");// 外部启动
                searchTheWeb(url);
            }
        }
    }

    private void initFragments() {
        mainFragment = new MainFragment();
        hotTagFragment = new HotTagFragment();
        fragments.add(mainFragment);
        fragments.add(hotTagFragment);
    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSearchFragment != null && mSearchFragment.isVisible()) {
                return super.onKeyDown(keyCode, event);
            }
            if (mBookmarkHistoryFragment != null && mBookmarkHistoryFragment.isVisible()) {
                return super.onKeyDown(keyCode, event);
            }
            if (mWindowManagerFragment != null && mWindowManagerFragment.isVisible()) {
                return super.onKeyDown(keyCode, event);
            }
            if (mTabsManager.getCurrentFragment() == null && mHotSiteFragment != null && mHotSiteFragment.isVisible()) {
                remove(mHotSiteFragment);
                ib_back.setEnabled(false);
                return true;
            }
            BrowserFragment currentTab = mTabsManager.getCurrentFragment();
            if (currentTab != null) {
                if (currentTab.canGoBack()) {
                    if (!currentTab.isShown()) {
                        onHideCustomView();
                    } else {
                        currentTab.goBack();
                    }
                } else {
                    saveCurrentTab(currentTab);
                }
            } else if (mCustomView != null || mCustomViewCallback != null) {
                onHideCustomView();
            } else if (isHotTagEditable()) {
                onEditComplete();
            } else {
                if (mainFragment.onBackPressed()) {
                    return true;
                } else if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    toast(R.string.exit_app_toast);
                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
            }
            return true;
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
            menuView.findViewById(R.id.ib_pull_down).setOnClickListener(this);

            ll_add_bookmark = (LinearLayout) menuView.findViewById(R.id.ll_add_bookmark);
            ll_refresh = (LinearLayout) menuView.findViewById(R.id.ll_refresh);
            tv_add_bookmark = (TextView) menuView.findViewById(R.id.tv_add_bookmark);
            iv_add_bookmark = (ImageView) menuView.findViewById(R.id.iv_add_bookmark);
            tv_refresh = (TextView) menuView.findViewById(R.id.tv_refresh);
            iv_refresh = (ImageView) menuView.findViewById(R.id.iv_refresh);

            setMenuButtonEnable(mTabsManager.getCurrentFragment() != null);

            int height = UIUtils.getDimen(R.dimen.menu_height);
            mMenuPopupWindow = new PopupWindow(menuView, CoordinatorLayout.LayoutParams.MATCH_PARENT, height);

            mMenuPopupWindow.setFocusable(true);
            // 设置允许在外点击消失，必须和setBackgroundDrawable方法一起使用才有效
            mMenuPopupWindow.setOutsideTouchable(true);
            mMenuPopupWindow.update();
            mMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mMenuPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            mMenuPopupWindow.setOutsideTouchable(true);
            mMenuPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, 0);

            // 设置背景颜色变暗
            setBackgroundBlur();
            mMenuPopupWindow.setOnDismissListener(onDismissListener);

        } else {
            if (!mMenuPopupWindow.isShowing()) {
                mMenuPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, 0);
                setMenuButtonEnable(mTabsManager.getCurrentFragment() != null);
                setBackgroundBlur();
            }
        }
    }

    private void setBackgroundBlur() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.7f;
        getWindow().setAttributes(lp);
    }

    private PopupWindow.OnDismissListener onDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.alpha = 1f;
            getWindow().setAttributes(lp);
        }
    };

    private void showAddBookmarkWindow() {
        if (mBookmarkPopupWindow == null) {
            View menuView = LayoutInflater.from(mActivity).inflate(R.layout.include_add_bookmark_window, (ViewGroup) mContentView, false);
            menuView.findViewById(R.id.ib_edit).setOnClickListener(this);
            menuView.findViewById(R.id.ll_bookmark).setOnClickListener(this);
            menuView.findViewById(R.id.ll_bookmark_home).setOnClickListener(this);
            menuView.findViewById(R.id.tv_cancel).setOnClickListener(this);

            tv_title_add_bookmark = (TextView) menuView.findViewById(R.id.tv_title_add_bookmark);
            iv_added_bookmark = (ImageView) menuView.findViewById(R.id.iv_added_bookmark);
            iv_added_home = (ImageView) menuView.findViewById(R.id.iv_added_home);

            updateAddBookmarkView();
            mBookmarkPopupWindow = new PopupWindow(menuView, CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);

            mBookmarkPopupWindow.setFocusable(true);
            // 设置允许在外点击消失，必须和setBackgroundDrawable方法一起使用才有效
            mBookmarkPopupWindow.setOutsideTouchable(true);
            mBookmarkPopupWindow.update();
            mBookmarkPopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mBookmarkPopupWindow.setAnimationStyle(R.style.PopupAnimation);
            mBookmarkPopupWindow.setOutsideTouchable(true);
            mBookmarkPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, 0);

            setBackgroundBlur();
            mBookmarkPopupWindow.setOnDismissListener(onDismissListener);

        } else {
            if (!mBookmarkPopupWindow.isShowing()) {
                mBookmarkPopupWindow.showAtLocation(mContentView, Gravity.BOTTOM, 0, 0);
                updateAddBookmarkView();
                setBackgroundBlur();
            }
        }
    }

    private void updateAddBookmarkView() {
        BrowserFragment currentFragment = mTabsManager.getCurrentFragment();
        if (currentFragment != null) {
            tv_title_add_bookmark.setText(currentFragment.getTitle());
            boolean isAddedToBookmark = mBookmarkManager.isBookmark(currentFragment.getUrl());
            boolean isAddedToHotTag = mHotTagDatabase.isContain(currentFragment.getTitle(), currentFragment.getUrl());
            iv_added_bookmark.setVisibility(isAddedToBookmark ? View.VISIBLE : View.GONE);
            iv_added_home.setVisibility(isAddedToHotTag ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
//            菜单按钮
        switch (v.getId()) {
            case R.id.tv_cancel:
                dismissAddBookmarkWindow();
                break;
            case R.id.ib_pull_down:
                dismissMenuWindow();
                break;
            case R.id.ll_add_bookmark:
                dismissMenuWindow();
                if (currentTab == null) {
                    toast("首页不可点击");
                } else {
                    showAddBookmarkWindow();
                }
                break;
            case R.id.ll_bookmark_history:
                dismissMenuWindow();
                mBookmarkHistoryFragment = new BookmarkHistoryFragment();
                addFragment(mBookmarkHistoryFragment, R.id.fl_container_full, true, STACK_INCLUDE_BOTTOM);
                break;
            case R.id.ll_refresh:
                dismissMenuWindow();
                if (currentTab != null) {
                    currentTab.reload();
                }
                break;
            case R.id.ll_setting:
                dismissMenuWindow();
                gotoActivity(SettingsActivity.class);
                break;
            case R.id.ll_download:
                dismissMenuWindow();
                gotoActivity(DownloadManagerActivity.class);
                break;
            case R.id.ll_exit:
                dismissMenuWindow();
                ActivityCollector.finishAll();
                break;
//                书签窗口按钮
            case R.id.ib_edit:
                mBookmarkPopupWindow.dismiss();
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
                if (currentTab != null) {
                    if (mBookmarkManager.isBookmark(currentTab.getUrl())) {
                        toast("书签已存在");
                    } else {
                        currentTab.bookmarkCurrentPage();
                        toast("书签添加成功");
                    }
                }
                mBookmarkPopupWindow.dismiss();
                break;
            case R.id.ll_bookmark_home:
                // 判断是否有在热门标签中
                mBookmarkPopupWindow.dismiss();
                if (currentTab != null) {
                    String url = currentTab.getUrl();
                    String title = currentTab.getTitle();
                    if (mHotTagDatabase.isContain(title, url)) {//
                        toast("书签已存在");
                    } else {
                        HotTagBean.DataBean bean = new HotTagBean.DataBean();
                        bean.setName(title);
                        bean.setIsErase(1);
                        bean.setAddrUrl(url);
                        mHotTagDatabase.addHotTagItem(bean);
                        RxBus.getInstance().post(new RXEvent(RXEvent.TAG_NOTIFY_DATA, ""));
                        toast("已添加");
                    }
                }
                break;
        }
    }

    private void dismissMenuWindow() {
        if (mMenuPopupWindow != null && mMenuPopupWindow.isShowing()) {
            mMenuPopupWindow.dismiss();
        }
    }

    private void dismissAddBookmarkWindow() {
        if (mBookmarkPopupWindow != null && mBookmarkPopupWindow.isShowing()) {
            mBookmarkPopupWindow.dismiss();
        }
    }

    /**
     * 跳转到搜索页
     */
    public void jumpToSearch(final int lastPage) {
        mSearchFragment = SearchFragment.newInstance(lastPage);
        addFragment(mSearchFragment, R.id.fl_container_full, true, STACK_INCLUDE_BOTTOM, false);
    }

    /**
     * 跳转到搜索结果页
     */
    public void loadUrlInNewFragment(String query) {
        BrowserFragment fragment = BrowserFragment.newInstance(query, false);
        addBrowserFragment(fragment);
        ib_back.setEnabled(true);
        mTabsManager.updateCurrentTab(fragment);
    }

    /**
     * searches the web for the query fixing any and all problems with the input
     * checks if it is a search, url, etc.
     */
    public void searchTheWeb(@NonNull String query) {
        final BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (query.isEmpty()) {
            return;
        }
        String searchUrl;
        if (Constants.BAIDU_SEARCH.equals(mSearchText)) {
            searchUrl = mSearchText + UrlUtils.QUERY_PLACE_HOLDER;
        } else {
            searchUrl = mSearchText;
        }
        query = query.trim();
        String urlFilter = UrlUtils.smartUrlFilter(query, true, searchUrl);
        if (currentTab != null) {
            currentTab.stopLoading();
            resumeFragment(currentTab);
            currentTab.loadUrl(urlFilter);
        } else {
            int currentIndex = mTabsManager.getCurrentIndex();
            tabStatusMap.remove(currentIndex);
            ib_back.setEnabled(true);
            ib_forward.setEnabled(false);
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
        final BrowserFragment currentTab = mTabsManager.getCurrentFragment();
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
        final BrowserFragment currentTab = mTabsManager.getCurrentFragment();
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
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (currentTab != null) {
            ib_forward.setEnabled(enabled);
        }
    }

    @Override
    public void setBackButtonEnabled(boolean enabled) {
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (currentTab != null) {
            ib_back.setEnabled(true);
        }
    }

    @Override
    public void receiveSearchEngine(EngineBean bean) {
        if (bean.getData() != null) {
            // 验证数据是否正确
            boolean hasDefault = false;
            for (EngineItem item : bean.getData()) {
                if (item.getIsDefault() == 1) {
                    hasDefault = true;
                }
            }
            if (hasDefault) {
                mEngineItems.clear();
                mEngineItems.addAll(bean.getData());
                for (EngineItem item : bean.getData()) {
                    mEngineDatabase.addEngineItem(item);
                    if (item.getIsDefault() == 1) {
                        mDefaultEngine = item;
                        mPreferences.setSearchUrl(mDefaultEngine.getAddrUrl());
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

        // 获取屏幕宽和高
        int widths = ScreenUtils.getScreenWidth(mActivity);
        int heights = ScreenUtils.getScreenHeight(mActivity);

        // 允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);

        // 去掉状态栏
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                statusBarHeights, widths, heights - statusBarHeights - UIUtils.getDimen(R.dimen.bottom_menu_height));

        // 销毁缓存信息
        view.destroyDrawingCache();

        return bmp;
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
            if (!isIgnore) {
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

    private void setViewVisible(View view, boolean isShow) {
        if (null != view) {
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
     *
     * @param bean
     */
    private void showUpdateDialog(final UpgradeBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View layout = LayoutInflater.from(this).inflate(R.layout.layout_dialog_update, null);

        TextView tv_version = (TextView) layout.findViewById(R.id.tv_version);
        TextView tv_size = (TextView) layout.findViewById(R.id.tv_size);
        TextView tv_title = (TextView) layout.findViewById(R.id.tv_title_update);
        TextView tv_description = (TextView) layout.findViewById(R.id.tv_description);

        TextView tv_left = (TextView) layout.findViewById(R.id.tv_left);
        TextView tv_right = (TextView) layout.findViewById(R.id.tv_right);

        UpgradeBean.DataBean info = bean.getData().get(0);

        tv_version.setText(getString(R.string.version_name) + info.getVersionName());
        tv_size.setText(getString(R.string.size) + Utils.formatFileSize(info.getFileSize()) + "M");
        tv_title.setText(info.getUpdateTitle());
        tv_description.setText(info.getUpdateInfo());

        tv_left.setText(info.getIsForce() == 0 ? R.string.update_next_time : R.string.exit);
        tv_right.setText(R.string.update_right_now);

        builder.setView(layout);
        builder.setCancelable(false);
        upgradeDialog = builder.show();

        tv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgradeDialog.dismiss();
                TextView view = (TextView) v;
                String str = view.getText().toString();
                if (getString(R.string.exit).equals(str)) {
                    ActivityCollector.finishAll();
                }
            }
        });
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upgradeDialog.dismiss();
                UpdateService.setCheckVersionInsterface(BrowserActivity.this, BrowserActivity.this, BrowserActivity.this);
                UpdateService.startDownloadAppStore(bean);
            }
        });
    }

    @Override
    public void receiveFileDownloadInfo(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        if (downloading_dialog == null) {
            showDownloadingDialog(task, soFarBytes, totalBytes);
        } else {
            if (downloading_dialog.isShowing()) {
                if (task.getStatus() == FileDownloadStatus.completed) {
                    downloading_dialog.dismiss();
                    UpdateService.installApp();
                } else {
                    updateDownloadingDialog(task, soFarBytes, totalBytes);
                }
            }
        }
    }

    private void updateDownloadingDialog(BaseDownloadTask task, int soFarBytes, float totalBytes) {
        final float percent = soFarBytes / totalBytes;
        downloading_pb_task.setMax(100);
        downloading_pb_task.setProgress((int) (percent * 100));

        downloading_tv_status.setText("已下载：" + (int) (percent * 100) + "%");
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

    @Override
    public void tabNumberChanged(int newNumber) {
        BrowserFragment currentTab = mTabsManager.getCurrentFragment();
        if (currentTab != null) {
            ib_back.setEnabled(true);
            ib_forward.setEnabled(currentTab.canGoForward());
        } else {
            BrowserFragment browserFragment = tabStatusMap.get(newNumber);
            if (browserFragment != null) {
                ib_back.setEnabled(false);
                ib_forward.setEnabled(true);
            } else {
                ib_back.setEnabled(false);
                ib_forward.setEnabled(false);
            }
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

    public TabsManager getTabModel() {
        return mTabsManager;
    }

    /**
     * 显示浏览页
     *
     * @param fragment
     */
    public void showBrowserFragment(BaseFragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_enter, R.anim.fragment_exit);
            transaction.show(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏浏览页
     *
     * @param fragment
     */
    public void hideBrowserFragment(BaseFragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_enter, R.anim.fragment_exit);
            transaction.hide(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 添加浏览页
     */
    public void addBrowserFragment(BaseFragment fragment) {
        if (fragment != null) {
            addFragment(fragment, R.id.fl_container_except_bottom, true, STACK_EXCEPT_BOTTOM);
        }
    }

    /**
     * 跳转到热门网站页
     */
    public void showHotSiteFragment() {
        mHotSiteFragment = new HotSiteFragment();
        addFragment(mHotSiteFragment, R.id.fl_container_except_bottom, true, STACK_EXCEPT_BOTTOM);
        ib_back.setEnabled(true);
        ib_forward.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTabsManager != null) {
            mTabsManager.pauseAll();
        }
        UpdateService.resetListener();
        if (upgradeDialog != null){
            upgradeDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTabsManager != null) {
            mTabsManager.resumeAll();
        }
        UpdateService.setCheckVersionInsterface(this, this, this);
        receiveAppStoreUpdateInfo(UpdateService.getAppStoreStatus());
        if (downloading_dialog != null && downloading_dialog.isShowing()) {
            downloading_dialog.dismiss();
        }
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
        updateEngineList();

        updateDefaultEngine();
    }

    private void updateEngineList() {
        getEngineList().subscribe(new Action1<List<EngineItem>>() {
            @Override
            public void call(List<EngineItem> engineItems) {
                if (engineItems != null && engineItems.size() > 0) {
                    mEngineItems.clear();
                    mEngineItems.addAll(engineItems);
                } else {
                    mPresenter.getSearchEngine();
                }
            }
        });
    }

    private void updateDefaultEngine() {
        getDefaultEngine().subscribe(new Action1<EngineItem>() {
            @Override
            public void call(EngineItem engineItem) {
                mDefaultEngine = engineItem;
                if (mDefaultEngine == null) {
                    mSearchText = Constants.BAIDU_SEARCH;
                } else {
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

    public void updateEngineDB(EngineItem defaultEngine) {
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

    public void showCompleteButton(boolean isShow) {
        tv_complete.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public boolean isHotTagEditable() {
        return tv_complete.getVisibility() == View.VISIBLE;
    }

    public void setPagingEnabled(boolean isPagingEnable) {
        mViewPager.setPagingEnabled(isPagingEnable);
    }

}

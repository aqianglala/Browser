package com.example.zy1584.mybase.ui.main;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseActivity;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.ui.main.adapter.MainFragmentAdapter;
import com.example.zy1584.mybase.ui.navigation.NavigationFragment;
import com.example.zy1584.mybase.ui.search.SearchFragment;
import com.example.zy1584.mybase.utils.ActivityCollector;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import static com.example.zy1584.mybase.R.id.viewPager;

public class BrowserActivity extends BaseActivity {
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private MainFragment mainFragment;
    private NavigationFragment navigationFragment;
    private PopupWindow mPopupWindow;
    private int keyCount;

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
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
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
            if (getSupportFragmentManager().getBackStackEntryCount() == 0){
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
        SearchFragment searchFragment = new SearchFragment();
        addFragment(searchFragment, R.id.fl_container_full, true);
    }

    /**
     * 跳转到搜索结果页
     */
    public void jumpToSearchResult(){
        SearchFragment searchFragment = new SearchFragment();
        addFragment(searchFragment, R.id.fl_container_except_bottom, true);
    }

}

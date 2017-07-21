package com.example.zy1584.mybase.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.mvp.IView;
import com.example.zy1584.mybase.utils.ToastUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by zy1584 on 2017-3-27.
 */

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity implements
        IView, View.OnClickListener {
    protected View view;
    protected P mPresenter;

    protected String TAG;
    protected LayoutInflater mInflater;
    protected BaseActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFullScreen()) {// 隐藏android系统的状态栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(getView());
        initMembers();

        mPresenter = loadPresenter();
        attachView();

        ButterKnife.bind(this);
        initToolBar();
        initView();
        initListener();
        if (null != getIntent()) {
            handleIntent(getIntent());
        }
        doBusiness(savedInstanceState);
    }

    private void attachView() {
        if (mPresenter != null)
            mPresenter.attachView(this);
    }

    private void initMembers() {
        TAG = this.getClass().getSimpleName();
        mInflater = getLayoutInflater();
        mActivity = this;
    }

    /**
     * @return 显示的内容
     */
    private View getView() {
        view = View.inflate(this, getLayoutId(), null);
        return view;
    }

    @Override
    public void onClick(View v) {

    }

    /******************************************* protected方法 ***********************************************/

    protected boolean isFullScreen() {
        return false;
    }

    protected void handleIntent(Intent intent) {
    }

    protected void initListener() {
    }

    protected void initView() {
    }

    /******************************************* abstract方法 ***********************************************/

    protected abstract int getLayoutId();

    protected abstract P loadPresenter();

    protected abstract void doBusiness(Bundle savedInstanceState);

    /******************************************* activity生命周期封装 ***********************************************/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();// rx生命周期管理
            mPresenter.detachView();
        }
    }

    /******************************************* webView封装 ***********************************************/

    protected void initWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(false);
        settings.setAppCachePath(getCacheDir().getAbsolutePath() + "/webViewCache");
        settings.setAppCacheEnabled(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.setWebChromeClient(new WebChromeClient());
    }

    /******************************************* toolbar封装 ***********************************************/
    private TextView mToolbarTitle;
    private TextView mToolbarSubTitle;
    private Toolbar mToolbar;

    public TextView getToolbarTitle(){
        return mToolbarTitle;
    }

    public TextView getSubTitle(){
        return mToolbarSubTitle;
    }

    public Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    /**
     * 设置标题
     * @param title
     */
    public void setToolBarTitle(CharSequence title) {
        if(mToolbarTitle != null){
            mToolbarTitle.setText(title);
        }else{
            getToolbar().setTitle(title);
            setSupportActionBar(getToolbar());
        }
    }

    protected void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbarSubTitle = (TextView) findViewById(R.id.toolbar_subtitle);
        if (mToolbar != null) {
            //将Toolbar显示到界面
            setSupportActionBar(mToolbar);
            showBack();
            if (mToolbarTitle != null) {
                //getTitle()的值是activity的android:lable属性值
                mToolbarTitle.setText(getTitle());
                //设置默认的标题不显示
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }

    }

    /**
     * 设置返回按钮，默认显示
     */
    protected void showBack(){
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(isShowBacking());
        getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBackPressed();
            }
        });
    }

    /**
     * 是否显示后退按钮,默认显示,可在子类重写该方法.
     * @return
     */
    protected boolean isShowBacking(){
        return true;
    }

    /******************************************* 高频操作封装 ***********************************************/
    /**
     * 打开一个Activity 默认 不关闭当前activity
     */
    public void gotoActivity(Class<?> clz) {
        gotoActivity(clz, false, null);
    }

    public void gotoActivity(Class<?> clz, boolean isCloseCurrentActivity) {
        gotoActivity(clz, isCloseCurrentActivity, null);
    }

    public void gotoActivity(Class<?> clz, boolean isCloseCurrentActivity, Bundle ex) {
        Intent intent = new Intent(this, clz);
        if (ex != null) intent.putExtras(ex);
        startActivity(intent);
        if (isCloseCurrentActivity) {
            finish();
        }
    }

    /**
     * @param str 显示一个内容为str的toast
     */
    public void toast(String str) {
        ToastUtils.showShort(this, str);
    }

    /**
     * @param contentId 显示一个内容为contentId指定的toast
     */
    public void toast(int contentId) {
        ToastUtils.showShort(this, contentId);
    }

    /**
     * @param str 日志的处理
     */
    public void logI(String str) {
        Logger.t(TAG).i(str);
    }

    protected void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /******************************************* fragment操作封装 ***********************************************/

    /**
     * 添加fragment
     *
     * @param fragment
     * @param contentId
     * @param addToBackStack
     */
    public void addFragment(BaseFragment fragment, int contentId, boolean addToBackStack) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(contentId, fragment, fragment.getClass().getSimpleName());
            if (addToBackStack) {
                transaction.addToBackStack("");
            }
            transaction.commit();
        }
    }

    /**
     * 替换fragment
     *
     * @param fragment
     * @param contentId
     * @param addToBackStack
     */
    public void replaceFragment(BaseFragment fragment, int contentId, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(contentId, fragment, fragment.getClass().getSimpleName());
        if (addToBackStack) {
            transaction.addToBackStack("");
        }
        transaction.commit();

    }

    //移除fragment
    protected void doBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

//    //返回键返回事件
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (KeyEvent.KEYCODE_BACK == keyCode) {
//            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//                finish();
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    /******************************************* Android 6.0权限封装 ***********************************************/

    public static int REQUEST_CODE = 0;

    public void requestPermission(String[] permissions, int requestCode) {

        this.REQUEST_CODE = requestCode;

        //检查权限是否授权
        if (checkPermissions(permissions)) {
            permissionSucceed(REQUEST_CODE);
        } else {
            List<String> needPermissions = getPermissions(permissions);
            ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQUEST_CODE);
        }
    }

    /**
     * 检测所有的权限是否都已授权
     *
     * @param permissions
     * @return
     */
    private boolean checkPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private List<String> getPermissions(String[] permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                permissionList.add(permission);
            }
        }
        return permissionList;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == REQUEST_CODE) {
            if (verificationPermissions(grantResults)) {
                permissionSucceed(REQUEST_CODE);
            } else {
                permissionFailing(REQUEST_CODE);
                showFailingDialog();
            }
        }
    }

    private boolean verificationPermissions(int[] results) {

        for (int result : results) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }

    private void showFailingDialog() {

        new AlertDialog.Builder(this)
                .setTitle("消息")
                .setMessage("当前应用无此权限，该功能暂时无法使用。如若需要，请单击确定按钮进行权限授权！")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        return;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSettings();
                    }
                }).show();

    }

    private void startSettings() {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    public void permissionFailing(int code) {

        Log.d(TAG, "获取权限失败=" + code);
    }

    public void permissionSucceed(int code) {

        Log.d(TAG, "获取权限成功=" + code);
    }

}

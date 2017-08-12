package com.news.browser.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.news.browser.mvp.IView;
import com.news.browser.utils.ToastUtils;
import com.orhanobut.logger.Logger;

import butterknife.ButterKnife;


public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements IView,
        View.OnClickListener {
    public String TAG;
    protected View mContentView;
    protected BaseActivity mActivity;
    protected P mPresenter;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        TAG = this.getClass().getSimpleName();
        mActivity = (BaseActivity) activity;
        mPresenter = loadPresenter();
        initMembers();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onUserVisible();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != getArguments()) {
            handleArguments(getArguments());
        }
        // 避免多次从xml中加载布局文件
        if (mContentView == null) {
            mContentView = inflater.inflate(getLayoutId(), null);
            ButterKnife.bind(this, mContentView);
            initView(savedInstanceState);
            setListener();
            doBusiness(savedInstanceState);
        } else {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            if (parent != null) {
                parent.removeView(mContentView);
            }
        }
        return mContentView;
    }

    private void initMembers() {
        if (mPresenter != null)
            mPresenter.attachView(this);
    }

    @Override
    public void onClick(View v) {

    }

    /******************************************* abstract方法 ***********************************************/

    protected abstract int getLayoutId();

    protected abstract P loadPresenter();

    protected abstract void doBusiness(Bundle savedInstanceState);

    /******************************************* protected方法 ***********************************************/

    protected void handleArguments(Bundle arguments) {
    }

    protected void initView(Bundle savedInstanceState) {
    }

    ;

    protected void setListener() {
    }

    ;

    protected void onUserVisible() {
    }

    ;

    /******************************************* 高频操作封装 ***********************************************/

    public void toast(String str) {
        ToastUtils.showShort(mActivity, str);
    }

    public void toast(int contentId) {
        ToastUtils.showShort(mActivity, contentId);
    }

    public void logI(String str) {
        Logger.t(TAG).i(str);
    }

    protected void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /******************************************* activity生命周期封装 ***********************************************/

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();// rx 生命周期管理
            mPresenter.detachView();
        }
    }
}
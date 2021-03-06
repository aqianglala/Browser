package com.news.browser.ui.windowManager;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.jaeger.library.StatusBarUtil;
import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.base.BasePresenter;
import com.news.browser.manager.TabsManager;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.main.BrowserFragment;
import com.news.browser.ui.main.view.BrowserViewTitle;
import com.news.browser.utils.UIUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by zy1584 on 2017-7-17.
 */

public class WindowManagerFragmentNew extends BaseFragment implements WindowSwipeAdapterNew.onSwipeDismissListener,
        WindowSwipeAdapterNew.OnItemClickLitener {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private BrowserActivity mBrowserAct;
    private LinearLayoutManager mLayoutManager;

    @OnClick(R.id.ll_new_create)
    void addTab() {
        if (mTabsManager.size() >= 12) {
            toast("窗口数量已达上限");
        } else {
            mBrowserAct.getSupportFragmentManager().popBackStack();
            mTabsManager.newTab("", false, true);
        }
    }

    @OnClick(R.id.ll_clear)
    void clear() {
        mTabsManager.clearAllTab();
        mBrowserAct.getSupportFragmentManager().popBackStack();
    }

    @OnClick(R.id.ll_complete)
    void complete() {
        mBrowserAct.getSupportFragmentManager().popBackStack();
    }

    private ArrayList<BrowserViewTitle> mData = new ArrayList<>();
    private WindowSwipeAdapterNew mAdapter;

    private TabsManager mTabsManager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tabs_new;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        mBrowserAct = (BrowserActivity) mActivity;
        mTabsManager = mBrowserAct.getTabModel();
        initData();
        mAdapter = new WindowSwipeAdapterNew(mActivity, mData);
        mAdapter.setOnSwipeDismissListener(this);
        mAdapter.setOnItemClickLitener(this);
        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.scrollToPosition(mTabsManager.getCurrentIndex());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new MyItemTouchHelperCallBack(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {

    }

    private void initData() {
        ArrayList<BrowserFragment> tabsList = mTabsManager.getTabList();
        for (BrowserFragment tab : tabsList) {
            if (tab == null) {
                mData.add(mTabsManager.getHomeTitleInfo());
            } else {
                mData.add(tab.getTitleInfo());
            }
        }
    }

    @Override
    public void onDismiss(int position) {
        mTabsManager.deleteTab(position);
        if (mData.size() == 0) {
            mBrowserAct.getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        mBrowserAct.getSupportFragmentManager().popBackStack();
        mTabsManager.switchToTab(position);
    }

    @Override
    public void onItemRemove(View view, int position) {
        mData.remove(position);
        mAdapter.notifyItemRemoved(position);
        mTabsManager.deleteTab(position);
        if (mData.size() == 0) {
            mBrowserAct.getSupportFragmentManager().popBackStack();
        }
    }

    class MyItemTouchHelperCallBack extends ItemTouchHelper.SimpleCallback {

        public MyItemTouchHelperCallBack(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            mAdapter.removeData(position);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        StatusBarUtil.setColor(mActivity, UIUtils.getColor(R.color.color_window_manager));
    }

    @Override
    public void onStop() {
        super.onStop();
        StatusBarUtil.setColor(mActivity, UIUtils.getColor(R.color.colorPrimary));
    }
}

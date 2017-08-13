package com.news.browser.ui.windowManager;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.base.BasePresenter;
import com.news.browser.manager.TabsManager;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.main.BrowserFragment;
import com.news.browser.ui.main.view.LightningViewTitle;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by zy1584 on 2017-7-17.
 */

public class WindowManagerFragment extends BaseFragment implements WindowSwipeAdapter.onSwipeDismissListener,
        WindowSwipeAdapter.OnItemClickLitener{
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private BrowserActivity mBrowserAct;

    @OnClick(R.id.btn_add_tab)
    void addTab(){
        mBrowserAct.getSupportFragmentManager().popBackStack();
        mTabsManager.newTab("", false, true);
    }
    private ArrayList<LightningViewTitle> mData = new ArrayList<>();
    private WindowSwipeAdapter mAdapter;

    private TabsManager mTabsManager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tabs;
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
        mAdapter = new WindowSwipeAdapter(mActivity, mData);
        mAdapter.setOnSwipeDismissListener(this);
        mAdapter.setOnItemClickLitener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new MyItemTouchHelperCallBack(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {

    }

    private void initData() {
        ArrayList<BrowserFragment> tabsList = mTabsManager.getmTabList();
        if (tabsList.size() == 0){
            tabsList.add(null);
        }
        for (BrowserFragment tab : tabsList){
            if (tab == null){
                mData.add(mTabsManager.getmHomeTitleInfo());
            }else{
                mData.add(tab.getTitleInfo());
            }
        }
    }

    @Override
    public void onDismiss(int position) {
        mTabsManager.deleteTab(position);
        if (mTabsManager.getmTabList().size() == 0){
            mBrowserAct.getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        mBrowserAct.getSupportFragmentManager().popBackStack();
        mTabsManager.switchToTab(position);
    }

    @Override
    public void onItemLongClick(View view, int position) {

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
}

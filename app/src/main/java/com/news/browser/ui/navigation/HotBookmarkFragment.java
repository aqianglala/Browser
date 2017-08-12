package com.news.browser.ui.navigation;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.HotTagBean;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.navigation.Adapter.HotTagAdapter;
import com.news.browser.ui.navigation.mvp.HotBookmarkContract;
import com.news.browser.ui.navigation.mvp.HotBookmarkPresenter;
import com.news.browser.utils.DensityUtils;
import com.news.browser.widget.GridSpacingItemDecoration;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class HotBookmarkFragment extends BaseFragment<HotBookmarkPresenter> implements HotBookmarkContract.View,
        MultiItemTypeAdapter.OnItemClickListener{
    @BindView(R.id.rv_tag)
    RecyclerView rv_tag;

    private List<HotTagBean.DataBean> mData = new ArrayList<>();
    private HotTagAdapter mHotTagAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_navigation;
    }

    @Override
    protected HotBookmarkPresenter loadPresenter() {
        return new HotBookmarkPresenter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rv_tag.setLayoutManager(new GridLayoutManager(mActivity, 4));
        rv_tag.setHasFixedSize(true);
        rv_tag.addItemDecoration(new GridSpacingItemDecoration(4, DensityUtils.dpToPx(25), true));
//        rv_tag.addItemDecoration(new SpacesItemDecoration(DensityUtils.dpToPx(25), 4));
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mPresenter.getHotBookmarkList();
    }


    @Override
    public void onReceiveHotBookmarkList(HotTagBean bean) {
        mData.clear();
        mData.addAll(bean.getData());
        mData.add(null);
        mHotTagAdapter = new HotTagAdapter(mActivity, mData);
        mHotTagAdapter.setOnItemClickListener(this);
        rv_tag.setAdapter(mHotTagAdapter);
    }

    @Override
    public void onGetHotBookmarkListError(Throwable e) {

    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        if (position == mData.size() - 1){// 添加

        }else{
            BrowserActivity browserAct = (BrowserActivity) mActivity;
            browserAct.searchTheWeb(mData.get(position).getAddrUrl());
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }
}

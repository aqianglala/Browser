package com.news.browser.ui.hotSite;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.BaseItem;
import com.news.browser.bean.HotSiteBean;
import com.news.browser.bean.HotSiteTitleBean;
import com.news.browser.bean.HotTagBean;
import com.news.browser.bus.RXEvent;
import com.news.browser.data.AccessRecordTool;
import com.news.browser.db.HotTagDatabase;
import com.news.browser.ui.hotSite.adapter.HotSiteAdapter;
import com.news.browser.ui.hotSite.mvp.HotSiteContract;
import com.news.browser.ui.hotSite.mvp.HotSitePresenter;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.utils.RxBus;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by zy1584 on 2017-8-13.
 */

public class HotSiteFragment extends BaseFragment<HotSitePresenter> implements HotSiteContract.View,
        MultiItemTypeAdapter.OnItemClickListener, HotSiteAdapter.OnButtonClickListener {

    private List<BaseItem> mData = new ArrayList<>();
    private List<String> mTitleList = new ArrayList<>();
    private HotTagDatabase mHotTagDatabase;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    private HotSiteAdapter mHotSiteAdapter;

    @OnClick(R.id.iv_back)
    void back() {
        mActivity.getSupportFragmentManager().popBackStack();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_hot_site;
    }

    @Override
    protected HotSitePresenter loadPresenter() {
        return new HotSitePresenter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mHotSiteAdapter = new HotSiteAdapter(mActivity, mData, this);
        mHotSiteAdapter.setOnItemClickListener(this);
        recycler_view.setLayoutManager(new LinearLayoutManager(mActivity));
        recycler_view.setHasFixedSize(true);
        recycler_view.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        recycler_view.setAdapter(mHotSiteAdapter);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        tv_title.setText(R.string.hot_site);
        mHotTagDatabase = HotTagDatabase.getInstance();
        mPresenter.getHotSite();
        // 自营数据统计
        AccessRecordTool.getInstance().accessHotSite();
    }

    @Override
    public void receiveHotSite(HotSiteBean bean) {
        List<HotSiteBean.DataBean> data = bean.getData();
        if (data != null && data.size() > 0) {
            mData.clear();
            for (HotSiteBean.DataBean b : data) {
                String classifyName = b.getClassifyName();
                if (!TextUtils.isEmpty(classifyName)) {
                    if (!isContainTitle(classifyName)) {
                        mTitleList.add(classifyName);
                        mData.add(new HotSiteTitleBean(classifyName));
                        mData.add(b);
                    } else {
                        mData.add(b);
                    }
                }
            }
            mHotSiteAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetHotSiteError(Throwable e) {

    }

    private boolean isContainTitle(String title) {
        boolean isContain = false;
        for (String str : mTitleList) {
            if (title.equals(str)) {
                isContain = true;
                break;
            }
        }
        return isContain;
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        BaseItem baseItem = mData.get(position);
        if (baseItem instanceof HotSiteBean.DataBean) {
            BrowserActivity browserActivity = (BrowserActivity) mActivity;
            browserActivity.searchTheWeb(((HotSiteBean.DataBean) baseItem).getAddrUrl());
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onAddClick(ViewHolder holder, HotSiteBean.DataBean bean, int position) {
        HotTagBean.DataBean dataBean = new HotTagBean.DataBean();
        dataBean.setAddrUrl(bean.getAddrUrl());
        dataBean.setIsErase(1);
        dataBean.setIconUrl(bean.getIconUrl());
        dataBean.setName(bean.getName());
        mHotTagDatabase.addHotTagItem(dataBean);
        RxBus.getInstance().post(new RXEvent(RXEvent.TAG_NOTIFY_DATA, ""));
    }

    @Override
    public void onOpenClick(ViewHolder holder, HotSiteBean.DataBean bean, int position) {
        BrowserActivity browserActivity = (BrowserActivity) mActivity;
        browserActivity.searchTheWeb(bean.getAddrUrl());
    }
}

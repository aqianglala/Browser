package com.news.browser.ui.navigation;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.HotTagBean;
import com.news.browser.bus.RXEvent;
import com.news.browser.data.AccessRecordTool;
import com.news.browser.db.HotTagDatabase;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.navigation.Adapter.HotTagAdapter;
import com.news.browser.ui.navigation.mvp.HotBookmarkContract;
import com.news.browser.ui.navigation.mvp.HotBookmarkPresenter;
import com.news.browser.utils.DensityUtils;
import com.news.browser.utils.FileUtils;
import com.news.browser.utils.RxBus;
import com.news.browser.widget.SpacesItemDecoration;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscription;
import rx.functions.Action1;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class HotTagFragment extends BaseFragment<HotBookmarkPresenter> implements HotBookmarkContract.View,
        MultiItemTypeAdapter.OnItemClickListener, HotTagAdapter.OnSiteRemoveClickListener{
    @BindView(R.id.rv_tag)
    RecyclerView rv_tag;

    private List<HotTagBean.DataBean> mData = new ArrayList<>();
    private HotTagAdapter mHotTagAdapter;
    private HotTagDatabase mHotTagDatabase;
    private Subscription rxSubscription;

    @OnClick(R.id.iv_qr_code)
    void openQrcode(){
        if (!mHotTagAdapter.isEditable()){
            BrowserActivity browserActivity = (BrowserActivity) mActivity;
            browserActivity.openQrcode();
        }
    }

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
        mHotTagAdapter = new HotTagAdapter(mActivity, mData, this);
        mHotTagAdapter.setOnItemClickListener(this);

        rv_tag.setLayoutManager(new GridLayoutManager(mActivity, 4));
        rv_tag.setHasFixedSize(true);
        rv_tag.addItemDecoration(new SpacesItemDecoration(DensityUtils.dpToPx(12.5f)));
        rv_tag.setAdapter(mHotTagAdapter);
    }

    @Override
    protected void setListener() {
        super.setListener();
        mContentView.findViewById(R.id.tv_hint).setOnClickListener(this);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        mHotTagDatabase = HotTagDatabase.getInstance();
        // 从数据库中读取，没有则从文件中读取，再判断是否需要从服务器中读取
        List<HotTagBean.DataBean> allHotSite = mHotTagDatabase.getAllHotTag();
        if (allHotSite != null && allHotSite.size() > 0){
            setHotSite(allHotSite, true);
        }else{
            FileUtils.loadFile(mActivity, FileUtils.HOT_SITE_FILE_NAME)
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            HotTagBean hotTagBean = new Gson().fromJson(s, HotTagBean.class);
                            setHotSite(hotTagBean.getData(), false);
                        }
                    });
        }
       if (allHotSite != null && allHotSite.size() == 0){
           mPresenter.getHotBookmarkList();
       }

        rxSubscription = RxBus.getInstance().toObservable(RXEvent.class)
                .subscribe(new Action1<RXEvent>() {
                    @Override
                    public void call(RXEvent rxEvent) {
                        List<HotTagBean.DataBean> allHotSite = mHotTagDatabase.getAllHotTag();
                        if (allHotSite != null && allHotSite.size() > 0) {
                            setHotSite(allHotSite, true);
                        }
                    }
                });
    }


    @Override
    public void onReceiveHotBookmarkList(HotTagBean bean) {
        List<HotTagBean.DataBean> data = bean.getData();
        if (data != null && data.size() > 0){
            // 保存到数据库中
            for (HotTagBean.DataBean b : data){
                mHotTagDatabase.addHotTagItem(b);
            }
            setHotSite(data, true);
        }
    }

    private void setHotSite(List<HotTagBean.DataBean> data, boolean showAddButton) {
        mData.clear();
        mData.addAll(data);
        if (showAddButton){
            mData.add(null);
        }
        mHotTagAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetHotBookmarkListError(Throwable e) {

    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        if (mHotTagAdapter.isEditable()) return;
        BrowserActivity browserAct = (BrowserActivity) mActivity;
        if (position == mData.size() - 1){// 添加
            browserAct.showHotSiteFragment();
        }else{
            browserAct.searchTheWeb(mData.get(position).getAddrUrl());
            // 自营数据统计：热门标签
            AccessRecordTool.getInstance().reportClick(0, AccessRecordTool.PG_RIGHT_SCREEN,
                    AccessRecordTool.TYPE_HOT_TAG, position, mData.get(position).getName(), mData.get(position).getAddrUrl());
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        mHotTagAdapter.setEditable(true);
        mHotTagAdapter.notifyDataSetChanged();
        BrowserActivity browserActivity = (BrowserActivity) mActivity;
        browserActivity.setPagingEnabled(false);
        browserActivity.showCompleteButton(true);
        return false;
    }

    @Override
    public void onRemoveClick(ViewHolder holder, HotTagBean.DataBean dataBean, int position) {
        mData.remove(position);
        mHotTagAdapter.notifyItemRemoved(position);
        mHotTagDatabase.deleteSiteItem(dataBean.getId());
    }

    public void onEditComplete(){
        mHotTagAdapter.setEditable(false);
        mHotTagAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!rxSubscription.isUnsubscribed()) {
            rxSubscription.unsubscribe();
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.tv_hint:
                if (!mHotTagAdapter.isEditable()){
                    ((BrowserActivity)mActivity).jumpToSearch(AccessRecordTool.PG_RIGHT_SCREEN);
                }
                break;
        }
    }
}

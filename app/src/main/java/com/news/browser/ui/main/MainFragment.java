package com.news.browser.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.news.browser.BuildConfig;
import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.bean.HomeNavigationBean;
import com.news.browser.data.AccessRecordTool;
import com.news.browser.ui.main.adapter.TestFragmentAdapter;
import com.news.browser.ui.main.bean.ChannelBean;
import com.news.browser.ui.main.mvp.MainFrgContract;
import com.news.browser.ui.main.mvp.MainFrgPresenter;
import com.news.browser.ui.news.NewsChannelFragment;
import com.news.browser.ui.news.NewsRecommendFragment;
import com.news.browser.utils.DensityUtils;
import com.news.browser.utils.FileUtils;
import com.news.browser.utils.GlideUtils;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.LocationUtils;
import com.news.browser.utils.SPUtils;
import com.news.browser.widget.NewsViewPager;
import com.news.browser.widget.SpacesItemDecoration;
import com.news.browser.widget.behavior.uc.UcNewsHeaderPagerBehavior;
import com.orhanobut.logger.Logger;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.functions.Action1;

import static com.news.browser.utils.SPUtils.get;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class MainFragment extends BaseFragment<MainFrgPresenter> implements UcNewsHeaderPagerBehavior.OnPagerStateListener,
        MainFrgContract.View {
    private static final int REQUEST_QR_CODE = 1;

    private NewsViewPager mNewsPager;
    private TabLayout mTableLayout;
    private List<BaseFragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();
    private UcNewsHeaderPagerBehavior mPagerBehavior;
    private TestFragmentAdapter mNewsPagerAdapter;

    private List<HomeNavigationBean.DataBean> mNavigationList = new ArrayList<>();

    private NavigationAdapter mNavigationAdapter;

    @BindView(R.id.rv_navigation)
    RecyclerView rv_navigation;
    private NewsRecommendFragment mRecommendFragment;

    @OnClick(R.id.iv_qr_code)
    void openQrcode() {
        BrowserActivity browserActivity = (BrowserActivity) mActivity;
        browserActivity.openQrcode();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected MainFrgPresenter loadPresenter() {
        return new MainFrgPresenter();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Logger.i("initview:" + this.hashCode());
        mPagerBehavior = (UcNewsHeaderPagerBehavior) ((CoordinatorLayout.LayoutParams) mContentView.findViewById(R.id.id_uc_news_header_pager).getLayoutParams()).getBehavior();
        mPagerBehavior.setPagerStateListener(this);
        mNewsPager = (NewsViewPager) mContentView.findViewById(R.id.id_uc_news_content);
        mTableLayout = (TabLayout) mContentView.findViewById(R.id.id_uc_news_tab);

        mRecommendFragment = new NewsRecommendFragment();
        mTableLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTableLayout.addOnTabSelectedListener(new TabSelectedListener());
        mNewsPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTableLayout));
        mTableLayout.setupWithViewPager(mNewsPager);
        mNewsPagerAdapter = new TestFragmentAdapter(mFragments, mTitles, getChildFragmentManager());
        mNewsPager.setAdapter(mNewsPagerAdapter);

        initNavigation();
    }

    private void initNavigation() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 6);
        rv_navigation.setLayoutManager(gridLayoutManager);
        rv_navigation.setHasFixedSize(true);
        rv_navigation.addItemDecoration(new SpacesItemDecoration(DensityUtils.dpToPx(8f), DensityUtils.dpToPx(20)));
    }

    @Override
    protected void setListener() {
        super.setListener();
        mContentView.findViewById(R.id.iv_search).setOnClickListener(this);
        mContentView.findViewById(R.id.tv_hint).setOnClickListener(this);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        getChannelList();
        getNavigation();
    }

    /**
     * 网络--本地
     */
    private void getNavigation() {
        String json_Navigation = (String) SPUtils.get(GlobalParams.DATA_NAVIGATION, "");
        if (!TextUtils.isEmpty(json_Navigation)) {
            HomeNavigationBean homeNavigationBean = new Gson().fromJson(json_Navigation, HomeNavigationBean.class);
            setNavigationData(homeNavigationBean);
        } else {
            FileUtils.loadFile(mActivity, FileUtils.HOME_NAVIGATION_FILE_NAME)
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            HomeNavigationBean bean = new Gson().fromJson(s, HomeNavigationBean.class);
                            setNavigationData(bean);
                        }
                    });
        }
        mPresenter.getHomeNavigationList();
    }

    /**
     * 缓存--网络--本地文件
     */
    private void getChannelList() {
        String jsonStr = (String) get(GlobalParams.DATA_CHANNEL, "");
        if (TextUtils.isEmpty(jsonStr)) {
            FileUtils.loadFile(mActivity, FileUtils.CHANNEL_LIST_FILE_NAME)
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            Map<String, ChannelBean> beanMap = parseChannelListJson(s);
                            setChannelList(beanMap);
                        }
                    });
            mPresenter.getChannelList();
        } else {
            Map<String, ChannelBean> beanMap = parseChannelListJson(jsonStr);
            setChannelList(beanMap);
        }
    }

    private Map<String, ChannelBean> parseChannelListJson(String jsonStr) {
        Map<String, ChannelBean> result = new HashMap();
        try {
            JSONObject object = new JSONObject(jsonStr);

            JSONObject data = object.getJSONObject("data");
            Iterator<String> iterator = data.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                JSONObject obj = data.getJSONObject(key);
                String chanCode = obj.optString("chanCode");
                String chanId = obj.optString("chanId");
                String chanName = obj.optString("chanName");
                String chanName_m = obj.optString("chanName_m");
                String isKB = obj.optString("isKB");
                ChannelBean bean = new ChannelBean(chanCode, chanId, chanName, chanName_m, isKB);
                result.put(key, bean);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_search:
                ((BrowserActivity) mActivity).jumpToSearch(AccessRecordTool.PG_HOT_NEWS);
                break;
            case R.id.tv_hint:
                ((BrowserActivity) mActivity).jumpToSearch(AccessRecordTool.PG_HOME);
                break;
        }
    }

    @Override
    public void onPagerClosed() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPagerClosed: ");
        }
        mNewsPager.setPagingEnabled(true);
        ((BrowserActivity)mActivity).setPagingEnabled(false);
        // 自营统计
        if (mNewsPager.getCurrentItem() == 0) {
            AccessRecordTool.getInstance().accessPage(0, AccessRecordTool.PG_HOT_NEWS, "推荐");
        }
    }

    @Override
    public void onPagerOpened() {
        mNewsPager.setPagingEnabled(false);
        ((BrowserActivity)mActivity).setPagingEnabled(true);
        if (mRecommendFragment != null) {
            mRecommendFragment.scrollToTop();
        }
        mNewsPager.setCurrentItem(0);
    }

    public boolean onBackPressed() {
        Logger.i("initview:" + this.hashCode());
        if (mPagerBehavior != null && mPagerBehavior.isClosed()) {
            mPagerBehavior.openPager();
            return true;
        }
        return false;
    }


    @Override
    public void onReceiveChannelList(Map<String, ChannelBean> channelMap) {
        setChannelList(channelMap);
    }

    private void setChannelList(Map<String, ChannelBean> channelMap) {
        if (channelMap.size() == 0) return;
        String[] defaultChannels = getResources().getStringArray(R.array.default_channel);
        List<String> channels = new LinkedList(Arrays.asList(defaultChannels));
        String province = (String) get(LocationUtils.PROVINCE, "");
        String city = (String) get(LocationUtils.CITY, "");
        if (!TextUtils.isEmpty(province)) {
            for (ChannelBean bean : channelMap.values()) {
                if (bean == null) continue;
                if (city.contains(bean.getChanName()) || province.contains(bean.getChanName())) {
                    channels.add(4, bean.getChanId());
                    break;
                }
            }
        }
        mFragments.clear();
        mFragments.add(mRecommendFragment);
        mTitles.clear();
        mTitles.add("推荐");
        for (String key : channels) {
            ChannelBean bean = channelMap.get(key);
            if (bean != null) {
                NewsChannelFragment fragment = NewsChannelFragment.newInstance(bean.getChanCode(), bean.getChanName());
                mFragments.add(fragment);
                mTitles.add(bean.getChanName());
            }
        }
        mNewsPagerAdapter.notifyDataSetChanged();
    }

    /**
     * 加载失败则从本地文件中读取
     *
     * @param e
     */
    @Override
    public void onGetChannelListError(Throwable e) {

    }

    @Override
    public void onReceiveNavigationList(HomeNavigationBean bean) {
        setNavigationData(bean);
    }

    private void setNavigationData(HomeNavigationBean bean) {
        mNavigationList.clear();
        List<HomeNavigationBean.DataBean> data = bean.getData();
        if (data != null) {
            if (data.size() > 12) {
                List<HomeNavigationBean.DataBean> dataBeen = data.subList(0, 12);
                mNavigationList.addAll(dataBeen);
            } else {
                mNavigationList.addAll(data);
            }
        }
        if (mNavigationAdapter == null) {
            mNavigationAdapter = new NavigationAdapter(mActivity, R.layout.item_list_navigatioin, mNavigationList);
            rv_navigation.setAdapter(mNavigationAdapter);
            mNavigationAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    HomeNavigationBean.DataBean dataBean = mNavigationList.get(position);
                    BrowserActivity browserAct = (BrowserActivity) mActivity;
                    browserAct.searchTheWeb(dataBean.getAddrUrl());
                    // 自营数据统计：首页导航
                    AccessRecordTool.getInstance().reportClick(0, AccessRecordTool.PG_HOME,
                            AccessRecordTool.TYPE_NAVIGATION, position, dataBean.getName(), dataBean.getAddrUrl());
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
        } else {
            mNavigationAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载失败则从本地文件中读取
     *
     * @param e
     */
    @Override
    public void onGetHomeNavigationListError(Throwable e) {

    }

    class TabSelectedListener implements TabLayout.OnTabSelectedListener {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mNewsPager.setCurrentItem(tab.getPosition());
            for (int i = 0; i < mFragments.size(); i++) {
                BaseFragment fragment = mFragments.get(i);
                if (i == tab.getPosition()) {
                    fragment.setIsCurrentFragment(true);
                } else {
                    fragment.setIsCurrentFragment(false);
                }
            }
            // 自营统计
            if (mNewsPager.isPagingEnabled()) {
                AccessRecordTool.getInstance().accessPage(0, AccessRecordTool.PG_HOT_NEWS, tab.getText().toString());
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    class NavigationAdapter extends CommonAdapter<HomeNavigationBean.DataBean> {

        public NavigationAdapter(Context context, int layoutId, List<HomeNavigationBean.DataBean> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(ViewHolder holder, HomeNavigationBean.DataBean dataBean, int position) {
            holder.setText(R.id.tv_name, dataBean.getName());
            ImageView iv_icon = holder.getView(R.id.iv_icon);
            GlideUtils.loadIconImage(mActivity, dataBean.getIconUrl(), iv_icon);
        }
    }

    public boolean home() {
        if (mPagerBehavior != null) {
            if (mPagerBehavior.isClosed()) {
                mPagerBehavior.openPager();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
    }
}

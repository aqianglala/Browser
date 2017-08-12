package com.news.browser.ui.search;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bumptech.glide.Glide;
import com.news.browser.R;
import com.news.browser.base.BaseFragment;
import com.news.browser.base.BasePresenter;
import com.news.browser.bean.EngineBean.EngineItem;
import com.news.browser.bean.SearchHistoryItem;
import com.news.browser.db.SearchHistoryDatabase;
import com.news.browser.manager.TabsManager;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.main.BrowserFragment;
import com.news.browser.ui.search.adapter.SearchHistoryAdapter;
import com.news.browser.utils.ScreenUtils;
import com.news.browser.utils.UIUtils;
import com.news.browser.utils.UrlUtils;
import com.news.browser.widget.DividerItemDecoration;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by zy1584 on 2017-7-3.
 */

public class SearchFragment extends BaseFragment {
    @BindView(R.id.iv_left)
    ImageView iv_left;
    @BindView(R.id.et_search)
    EditText et_search;
    @BindView(R.id.iv_right)
    ImageView iv_right;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;

    @BindView(R.id.rv_history)
    RecyclerView rv_history;
    @BindView(R.id.rv_engines)
    RecyclerView rv_engines;
    private EnginesAdapter mEnginesAdapter;
    private List<EngineItem> mEngineItems;
    private EngineItem mDefaultEngine;

    private SearchHistoryDatabase mSearchHistoryDatabase;
    private List<SearchHistoryItem> mSearchHistoryItems = new ArrayList<>();
    private SearchHistoryAdapter mSearchHistoryAdapter;
    private int anchorX;
    private int offsetY;

    @OnClick(R.id.tv_cancel)
    void searchOrCancel(){
        String query = et_search.getText().toString().trim();
        if (!TextUtils.isEmpty(query)){
            searchTheWeb(query);
        }else{
            hideSoftInputFromWindow();
            mBrowserAct.getSupportFragmentManager().popBackStack();
        }
    }

    @OnClick(R.id.iv_left)
    void showEngines(){
        mEngineItems = mBrowserAct.getEngineItems();
        if (mEngineItems != null && mEngineItems.size() > 0){
            rv_engines.setVisibility(rv_engines.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            rv_history.setVisibility(rv_history.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (mEnginesAdapter == null){
                mEnginesAdapter = new EnginesAdapter(mActivity, R.layout.item_list_engine, mEngineItems);
                rv_engines.setLayoutManager(new LinearLayoutManager(mActivity));
                rv_engines.setHasFixedSize(true);
                rv_engines.setItemAnimator(new DefaultItemAnimator());
                rv_engines.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
                rv_engines.setAdapter(mEnginesAdapter);
            }
        }
    }

    private void searchTheWeb(final String query) {
        updateSearchHistory(query)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        hideSoftInputFromWindow();
                        mBrowserAct.getSupportFragmentManager().popBackStack();
                        mBrowserAct.searchTheWeb(query);
                    }
                });
    }

    private Observable<String> updateSearchHistory(final String query) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String s = UrlUtils.smartUrlFilter(query, false, null);
                if (!TextUtils.isEmpty(s)){// 是有效的url
                    mSearchHistoryDatabase.visitSearchHistoryItem(s, query);
                }else{// 无效的url
                    mSearchHistoryDatabase.visitSearchHistoryItem("", query);
                }
                subscriber.onNext(query);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private BrowserActivity mBrowserAct;
    private TabsManager mTabsManager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
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
        mDefaultEngine = mBrowserAct.getDefaultEngineItem();
        updateDefaultEngineIcon();
    }

    private void updateDefaultEngineIcon() {
        if (mDefaultEngine != null){
            Glide.with(mActivity).load(mDefaultEngine.getIconUrl()).into(iv_left);
        }
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        BrowserFragment currentTab = mTabsManager.getCurrentTab();
        if (currentTab != null){
            String url = currentTab.getUrl();
            et_search.setText(url);
            showDeleteButton(url);
        }
        et_search.addTextChangedListener(mTextWatcher);
        SearchListenerClass search = new SearchListenerClass();
        et_search.setOnKeyListener(search);
        et_search.setOnFocusChangeListener(search);
        et_search.setOnEditorActionListener(search);

        mSearchHistoryDatabase = SearchHistoryDatabase.getInstance();
        getSearchHistoryList()
                .subscribe(new Action1<List<SearchHistoryItem>>() {
                    @Override
                    public void call(List<SearchHistoryItem> searchHistoryItems) {
                        if (searchHistoryItems != null){
                            mSearchHistoryItems.clear();
                            mSearchHistoryItems.addAll(searchHistoryItems);

                            if (mSearchHistoryAdapter == null){
                                mSearchHistoryAdapter = new SearchHistoryAdapter(mActivity, mSearchHistoryItems);
                                rv_history.setLayoutManager(new LinearLayoutManager(mActivity));
                                rv_history.setHasFixedSize(true);
                                rv_history.setItemAnimator(new DefaultItemAnimator());
                                rv_history.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL_LIST));
                                rv_history.setAdapter(mSearchHistoryAdapter);
                                mSearchHistoryAdapter.setOnItemClickListener(new OnSearchItemClickListener());
                            }
                        }
                    }
                });
    }

    private void showDeleteButton(CharSequence s) {
        if (s == null || s.length() == 0){
            iv_right.setVisibility(View.GONE);
            tv_cancel.setText("取消");
        }else{
            iv_right.setVisibility(View.VISIBLE);
            iv_right.setImageResource(R.drawable.ic_action_delete);
            tv_cancel.setText("搜索");
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            showDeleteButton(s);
            // 获取搜索联想
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private class SearchListenerClass implements View.OnKeyListener, OnEditorActionListener,
            OnFocusChangeListener {

        @Override
        public boolean onKey(View searchView, int keyCode, KeyEvent keyEvent) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                    searchTheWeb(et_search.getText().toString());
                    final BrowserFragment currentView = mTabsManager.getCurrentTab();
                    if (currentView != null) {
                        currentView.requestFocus();
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }

        @Override
        public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || actionId == EditorInfo.IME_ACTION_SEND
                    || actionId == EditorInfo.IME_ACTION_SEARCH
                    || (arg2.getAction() == KeyEvent.KEYCODE_ENTER)) {
                searchTheWeb(et_search.getText().toString());
                final BrowserFragment currentView = mTabsManager.getCurrentTab();
                if (currentView != null) {
                    currentView.requestFocus();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onFocusChange(final View v, final boolean hasFocus) {
            final BrowserFragment currentView = mTabsManager.getCurrentTab();
            if (hasFocus && currentView != null) {
                et_search.selectAll();
            }
            if (!hasFocus) {
                hideSoftInputFromWindow();
            }
        }
    }

    private void hideSoftInputFromWindow() {
        InputMethodManager imm = (InputMethodManager) mBrowserAct.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        et_search.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) et_search.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et_search, 0);
    }

    class EnginesAdapter extends CommonAdapter<EngineItem> {


        public EnginesAdapter(Context context, int layoutId, List<EngineItem> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(final ViewHolder holder, EngineItem item, int position) {
            Context context = holder.getConvertView().getContext();
            ImageView iv_icon = holder.getView(R.id.iv_icon);
            ImageView iv_choose = holder.getView(R.id.iv_choose);
            Glide.with(context).load(item.getIconUrl()).into(iv_icon);
            holder.setText(R.id.tv_name, item.getName());
            if (item.getIsDefault() == 1){
                iv_choose.setVisibility(View.VISIBLE);
            }else{
                iv_choose.setVisibility(View.GONE);
            }
            holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    updateDefaultEngine(layoutPosition)
                            .subscribe(new Action1<EngineItem>() {
                                @Override
                                public void call(EngineItem item) {
                                    updateDefaultEngineIcon();
                                    notifyDataSetChanged();
                                    rv_engines.setVisibility(View.GONE);
                                    rv_history.setVisibility(View.VISIBLE);
                                }
                            });
                }
            });
        }
    }

    private Observable updateDefaultEngine(final int layoutPosition) {
        return Observable.create(new Observable.OnSubscribe<EngineItem>() {
            @Override
            public void call(Subscriber<? super EngineItem> subscriber) {
                for (int i = 0; i< mEngineItems.size(); i++){
                    EngineItem engineItem = mEngineItems.get(i);
                    if (i == layoutPosition){
                        engineItem.setIsDefault(1);
                        mDefaultEngine = engineItem;
                        mBrowserAct.setDefaultEngine(engineItem);
                    }else{
                        engineItem.setIsDefault(0);
                    }
                }
                mBrowserAct.setEngineItems(mEngineItems);
                mBrowserAct.updateEngineDB(mDefaultEngine);
                subscriber.onNext(mDefaultEngine);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<SearchHistoryItem>> getSearchHistoryList() {
        return Observable.create(new Observable.OnSubscribe<List<SearchHistoryItem>>() {
            @Override
            public void call(Subscriber<? super List<SearchHistoryItem>> subscriber) {
                List<SearchHistoryItem> historyList = mSearchHistoryDatabase.getLastItems();
                subscriber.onNext(historyList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    class OnSearchItemClickListener implements MultiItemTypeAdapter.OnItemClickListener{

        @Override
        public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
            SearchHistoryItem searchHistoryItem = mSearchHistoryItems.get(position);
            searchTheWeb(searchHistoryItem.getTitle());
        }

        @Override
        public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
            int height = view.getHeight();
            mCurrentLongClickPosition = position;
            showRemoveWindow(view, height);
            return false;
        }
    }

    private int mCurrentLongClickPosition;
    private PopupWindow mRemovePopupWindow;
    private void showRemoveWindow(View anchorView, int height) {
        if (mRemovePopupWindow == null) {
            int screenWidth = ScreenUtils.getScreenWidth(mActivity);
            int windowWidth = UIUtils.getDimen(R.dimen.remove_window_width);
            int windowHeight = UIUtils.getDimen(R.dimen.remove_window_height);
            anchorX = (screenWidth / 2) - (windowWidth / 2);
            offsetY = height / 2;

            View contentView = LayoutInflater.from(mActivity).inflate(R.layout.layout_remove_search_history_toast, (ViewGroup) mContentView, false);
            mRemovePopupWindow = new PopupWindow(contentView, windowWidth, windowHeight);

            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRemovePopupWindow.dismiss();
                    SearchHistoryItem searchHistoryItem = mSearchHistoryItems.get(mCurrentLongClickPosition);
                    mSearchHistoryDatabase.deleteSearchHistoryItem(searchHistoryItem);
                    mSearchHistoryItems.remove(mCurrentLongClickPosition);
                    mSearchHistoryAdapter.notifyItemRemoved(mCurrentLongClickPosition);
                }
            });

            mRemovePopupWindow.setFocusable(true);
            // 设置允许在外点击消失，必须和setBackgroundDrawable方法一起使用才有效
            mRemovePopupWindow.setOutsideTouchable(true);
            mRemovePopupWindow.update();
            mRemovePopupWindow.setBackgroundDrawable(new BitmapDrawable());

//            mRemovePopupWindow.setAnimationStyle(R.style.PopupAnimation);
            mRemovePopupWindow.setOutsideTouchable(true);
            mRemovePopupWindow.showAsDropDown(anchorView, anchorX, -offsetY);

        } else {
            if (!mRemovePopupWindow.isShowing()) {
                mRemovePopupWindow.showAsDropDown(anchorView,  anchorX, -offsetY);
            }
        }
    }

}

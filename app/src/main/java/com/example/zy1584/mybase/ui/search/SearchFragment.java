package com.example.zy1584.mybase.ui.search;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.base.BaseFragment;
import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.manager.TabsManager;
import com.example.zy1584.mybase.ui.main.BrowserActivity;
import com.example.zy1584.mybase.ui.main.BrowserFragment;

import butterknife.BindView;
import butterknife.OnClick;

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

    private void searchTheWeb(String query) {
        hideSoftInputFromWindow();
        mBrowserAct.getSupportFragmentManager().popBackStack();
        mBrowserAct.searchTheWeb(query);
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
}

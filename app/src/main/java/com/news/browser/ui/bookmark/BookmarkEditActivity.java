package com.news.browser.ui.bookmark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BasePresenter;
import com.news.browser.ui.main.db.BookmarkManager;
import com.news.browser.ui.main.db.HistoryItem;
import com.news.browser.utils.GlobalParams;

import butterknife.BindView;
import butterknife.OnClick;


public class BookmarkEditActivity extends BaseActivity {

    private BookmarkManager mBookmarkManager;
    private HistoryItem item;
    @BindView(R.id.ll_bookmark)
    LinearLayout ll_bookmark;

    @BindView(R.id.ll_bookmark_home)
    LinearLayout ll_bookmark_home;

    @BindView(R.id.et_name)
    EditText et_name;

    @BindView(R.id.et_url)
    EditText et_url;

    @OnClick(R.id.tv_cancel)
    void cancel(){
        finish();
    }

    @OnClick(R.id.tv_save)
    void save(){
        String title = et_name.getText().toString();
        String url = et_url.getText().toString();
        if (item == null){
            final HistoryItem historyItem = !mBookmarkManager.isBookmark(url)
                    ? new HistoryItem(url, title)
                    : null;
            if (historyItem != null && mBookmarkManager.addBookmark(historyItem)){
                toast("保存成功");
            }
        }else{
            HistoryItem editedItem = new HistoryItem();
            editedItem.setTitle(title);
            editedItem.setUrl(url);
            mBookmarkManager.editBookmark(item, editedItem);
            toast("更新成功");
        }
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bookmark_edit;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void handleIntent(Intent intent) {
        super.handleIntent(intent);
        Bundle extras = intent.getExtras();
        String url = extras.getString(GlobalParams.URL);
        String title = extras.getString(GlobalParams.TITLE);
        et_name.setText(title == null ? "" : title);
        et_url.setText(url == null ? "" : url);
        mBookmarkManager = BookmarkManager.getInstance();
        item = mBookmarkManager.findBookmarkForUrl(url);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {

    }
}

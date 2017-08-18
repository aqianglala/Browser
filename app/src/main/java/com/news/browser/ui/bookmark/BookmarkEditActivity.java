package com.news.browser.ui.bookmark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BasePresenter;
import com.news.browser.bean.HotTagBean;
import com.news.browser.bus.RXEvent;
import com.news.browser.db.HotTagDatabase;
import com.news.browser.ui.main.db.BookmarkManager;
import com.news.browser.ui.main.db.HistoryItem;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.UrlUtils;

import butterknife.BindView;
import butterknife.OnClick;


public class BookmarkEditActivity extends BaseActivity {

    private BookmarkManager mBookmarkManager;
    private HotTagDatabase mHotTagDatabase;
    private HistoryItem mBookmark;
    @BindView(R.id.ll_bookmark)
    LinearLayout ll_bookmark;

    @BindView(R.id.ll_bookmark_home)
    LinearLayout ll_bookmark_home;

    @BindView(R.id.et_name)
    EditText et_name;

    @BindView(R.id.et_url)
    EditText et_url;

    @BindView(R.id.iv_added_bookmark)
    ImageView iv_added_bookmark;

    @BindView(R.id.iv_choose_bookmark)
    ImageView iv_choose_bookmark;

    @BindView(R.id.iv_choose_home)
    ImageView iv_choose_home;

    @BindView(R.id.iv_added_home)
    ImageView iv_added_home;
    private String mUrl;
    private String mTitle;
    private HotTagBean.DataBean mHotTag;

    @OnClick(R.id.ll_bookmark)
    void clickAddBookmark() {
        iv_choose_bookmark.setVisibility(iv_choose_bookmark.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.ll_bookmark_home)
    void clickAddHotTag() {
        iv_choose_home.setVisibility(iv_choose_home.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.tv_cancel)
    void cancel() {
        finish();
    }

    @OnClick(R.id.tv_save)
    void save() {
        String title = et_name.getText().toString();
        String url = et_url.getText().toString();
        if (TextUtils.isEmpty(title)) {
            toast("网址名称不能为空！");
            return;
        }
        if (TextUtils.isEmpty(url)) {
            toast("当前网址不符合规范！");
            return;
        } else {
            String s = UrlUtils.smartUrlFilter(url, false, null);
            if (TextUtils.isEmpty(s)) {
                toast("当前网址不符合规范");
                return;
            }
        }
        if (iv_choose_bookmark.getVisibility() == View.VISIBLE) {
            if (mBookmark == null) {
                final HistoryItem historyItem = !mBookmarkManager.isBookmark(url)
                        ? new HistoryItem(url, title)
                        : null;
                if (historyItem != null) {
                    mBookmarkManager.addBookmark(historyItem);
                }
            } else {
                HistoryItem editedItem = new HistoryItem();
                editedItem.setTitle(title);
                editedItem.setUrl(url);
                mBookmarkManager.editBookmark(mBookmark, editedItem);
            }
        } else {
            if (mBookmark != null) {
                mBookmarkManager.deleteBookmark(mBookmark);
            }
        }
        if (iv_choose_home.getVisibility() == View.VISIBLE) {
            if (mHotTag == null) {
                HotTagBean.DataBean newTag = new HotTagBean.DataBean();
                newTag.setAddrUrl(mUrl);
                newTag.setIsErase(1);
                newTag.setName(mTitle);
                mHotTagDatabase.addHotTagItem(newTag);
                RxBus.getInstance().post(new RXEvent(RXEvent.TAG_NOTIFY_DATA, ""));
            } else {
                HotTagBean.DataBean newTag = new HotTagBean.DataBean();
                newTag.setAddrUrl(mUrl);
                newTag.setIsErase(1);
                newTag.setName(mTitle);
                mHotTagDatabase.editHotTag(mHotTag, newTag);
                RxBus.getInstance().post(new RXEvent(RXEvent.TAG_NOTIFY_DATA, ""));
            }
        } else {
            if (mHotTag != null) {
                mHotTagDatabase.deleteSiteItem(mHotTag.getId());
                RxBus.getInstance().post(new RXEvent(RXEvent.TAG_NOTIFY_DATA, ""));
            }
        }
        toast("保存成功");
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
        mUrl = extras.getString(GlobalParams.URL);
        mTitle = extras.getString(GlobalParams.TITLE);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        et_name.setText(mTitle == null ? "" : mTitle);
        et_url.setText(mUrl == null ? "" : mUrl);
        et_name.setSelection(et_name.length());
        mBookmarkManager = BookmarkManager.getInstance();
        mBookmark = mBookmarkManager.findBookmarkForUrl(mUrl);

        mHotTagDatabase = HotTagDatabase.getInstance();
        mHotTag = mHotTagDatabase.findHotTag(mTitle, mUrl);

        iv_added_bookmark.setVisibility(mBookmark != null ? View.VISIBLE : View.GONE);
        iv_added_home.setVisibility(mHotTag != null ? View.VISIBLE : View.GONE);
    }

}

package com.news.browser.ui.download.db;

import android.content.ContentValues;

import static com.news.browser.dialog.FileDatabase.KEY_CLICK_ID;
import static com.news.browser.dialog.FileDatabase.KEY_CONVERSION_LINK;
import static com.news.browser.dialog.FileDatabase.KEY_ID;
import static com.news.browser.dialog.FileDatabase.KEY_NAME;
import static com.news.browser.dialog.FileDatabase.KEY_PATH;
import static com.news.browser.dialog.FileDatabase.KEY_STATUS;
import static com.news.browser.dialog.FileDatabase.KEY_TYPE;
import static com.news.browser.dialog.FileDatabase.KEY_URL;

/**
 * Created by zy1584 on 2017-7-19.
 */

public class FileItem {

    private int mId;
    private String mUrl;
    private String mName;
    private int mType;
    private String mPath;
    private int mStatus;
    private String mTimestamp;
    private String mClickId;// 腾讯广告联盟统计
    private String mConversionLink;// 腾讯广告联盟统计

    public FileItem() {
    }

    public FileItem(int id, String mUrl, String mName, int mType, String mPath) {
        this.mId = id;
        this.mUrl = mUrl;
        this.mName = mName;
        this.mType = mType;
        this.mPath = mPath;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(String timestamp) {
        this.mTimestamp = timestamp;
    }

    public String getClickId() {
        return mClickId;
    }

    public void setClickId(String mClickId) {
        this.mClickId = mClickId;
    }

    public String getConversionLink() {
        return mConversionLink;
    }

    public void setConversionLink(String mConversionLink) {
        this.mConversionLink = mConversionLink;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID, mId);
        cv.put(KEY_URL, mUrl);
        cv.put(KEY_NAME, mName);
        cv.put(KEY_TYPE, mType);
        cv.put(KEY_PATH, mPath);
        cv.put(KEY_STATUS, mStatus);

        cv.put(KEY_CLICK_ID, mClickId);
        cv.put(KEY_CONVERSION_LINK, mConversionLink);
        return cv;
    }
}

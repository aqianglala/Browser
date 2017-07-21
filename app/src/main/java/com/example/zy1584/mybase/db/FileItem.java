package com.example.zy1584.mybase.db;

import android.content.ContentValues;

import static com.example.zy1584.mybase.dialog.FileDatabase.KEY_ID;
import static com.example.zy1584.mybase.dialog.FileDatabase.KEY_NAME;
import static com.example.zy1584.mybase.dialog.FileDatabase.KEY_PATH;
import static com.example.zy1584.mybase.dialog.FileDatabase.KEY_TYPE;
import static com.example.zy1584.mybase.dialog.FileDatabase.KEY_URL;

/**
 * Created by zy1584 on 2017-7-19.
 */

public class FileItem {

    private int mId;
    private String mUrl;
    private String mName;
    private long mSize;
    private int mType;
    private String mPath;

    public FileItem() {
    }

    public FileItem(int id, String mUrl, String mName, long mSize, int mType, String mPath) {
        this.mId = id;
        this.mUrl = mUrl;
        this.mName = mName;
        this.mSize = mSize;
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

    public long getSize() {
        return mSize;
    }

    public void setSize(long mSize) {
        this.mSize = mSize;
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

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID, mId);
        cv.put(KEY_URL, mUrl);
        cv.put(KEY_NAME, mName);
        cv.put(KEY_TYPE, mType);
        cv.put(KEY_PATH, mPath);
        return cv;
    }
}

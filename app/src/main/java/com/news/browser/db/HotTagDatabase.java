/*
 * Copyright 2014 A.C.R. Development
 */
package com.news.browser.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.news.browser.base.BaseApplication;
import com.news.browser.bean.HotTagBean.DataBean;

import java.util.ArrayList;
import java.util.List;


public class HotTagDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "hotTagManager";

    // HotTagItems table name
    private static final String TABLE_HOT_TAG = "hotTag";

    // HotTagItems Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_IS_ERASE = "isErase";
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS_URL = "addrUrl";
    public static final String KEY_ICON_URL = "iconUrl";

    @Nullable
    private SQLiteDatabase mDatabase;

    private static final HotTagDatabase instance = new HotTagDatabase();

    private HotTagDatabase() {
        super(BaseApplication.getContext().getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        initialize();
    }

    public static HotTagDatabase getInstance() {
        return instance;
    }

    private void initialize() {
        BaseApplication.getTaskThread().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (HotTagDatabase.this) {
                    mDatabase = HotTagDatabase.this.getWritableDatabase();
                }
            }
        });
    }

    // Creating Tables
    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String CREATE_HOT_SITE_TABLE = "CREATE TABLE " + TABLE_HOT_TAG + '('
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_IS_ERASE + " INTEGER,"
                + KEY_NAME + " TEXT,"
                + KEY_ADDRESS_URL + " TEXT,"
                + KEY_ICON_URL + " TEXT" + ')';
        db.execSQL(CREATE_HOT_SITE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOT_TAG);
        // Create tables again
        onCreate(db);
    }

    public synchronized void deleteHotTag() {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_HOT_TAG, null, null);
        mDatabase.close();
        mDatabase = this.getWritableDatabase();
    }

    @Override
    public synchronized void close() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
        super.close();
    }

    @NonNull
    private SQLiteDatabase openIfNecessary() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = this.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void addHotTagItem(@NonNull DataBean item) {
        mDatabase = openIfNecessary();
        mDatabase.insert(TABLE_HOT_TAG, null, item.toContentValues());
    }

    public synchronized void deleteSiteItem(int id) {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_HOT_TAG, KEY_ID + " = ?", new String[]{Integer.toString(id)});
    }

    public synchronized boolean isContain(String name, String url) {
        mDatabase = openIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_HOT_TAG, null,
                KEY_ADDRESS_URL + " = ? and " + KEY_NAME + " = ?", new String[]{url, name}, null, null, null, null);
        boolean isContain = false;
        if (cursor != null && cursor.getCount() > 0) {
            isContain = true;
            cursor.close();
        }
        return isContain;
    }

    public synchronized DataBean findHotTag(String name, String url) {
        mDatabase = openIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_HOT_TAG, null,
                KEY_ADDRESS_URL + " = ? and " + KEY_NAME + " = ?", new String[]{url, name}, null, null, null, null);
        DataBean item = null;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            item = new DataBean();
            item.setId(cursor.getInt(0));
            item.setIsErase(cursor.getInt(1));
            item.setName(cursor.getString(2));
            item.setAddrUrl(cursor.getString(3));
            item.setIconUrl(cursor.getString(4));
            cursor.close();
        }
        return item;
    }

    public synchronized DataBean editHotTag(DataBean oldTag, DataBean newTag) {
        mDatabase = openIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_HOT_TAG, null,
                KEY_ADDRESS_URL + " = ? and " + KEY_NAME + " = ?", new String[]{oldTag.getAddrUrl(),
                        oldTag.getName()}, null, null, null, null);
        DataBean item = null;
        if (cursor.getCount() > 0) {
            mDatabase.update(TABLE_HOT_TAG, newTag.toContentValues(), KEY_ADDRESS_URL + " = ? and " + KEY_NAME + " = ?",
                    new String[]{oldTag.getAddrUrl(), oldTag.getName()});
        } else {
            addHotTagItem(oldTag);
        }
        cursor.close();
        return item;
    }

    @NonNull
    public synchronized List<DataBean> getAllHotTag() {
        mDatabase = openIfNecessary();
        List<DataBean> itemList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_HOT_TAG + " ORDER BY " + KEY_ID
                + " ASC";

        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                DataBean item = new DataBean();
                item.setId(cursor.getInt(0));
                item.setIsErase(cursor.getInt(1));
                item.setName(cursor.getString(2));
                item.setAddrUrl(cursor.getString(3));
                item.setIconUrl(cursor.getString(4));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

}

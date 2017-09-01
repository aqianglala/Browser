/*
 * Copyright 2014 A.C.R. Development
 */
package com.news.browser.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.news.browser.base.BaseApplication;
import com.news.browser.bean.SearchHistoryItem;

import java.util.ArrayList;
import java.util.List;


public class SearchHistoryDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "searchHistoryManager";

    // SearchHistoryItems table name
    private static final String TABLE_SEARCH_HISTORY = "searchHistory";

    // SearchHistoryItems Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TIME_VISITED = "time";

    @Nullable
    private SQLiteDatabase mDatabase;

    private static final SearchHistoryDatabase instance = new SearchHistoryDatabase();

    private SearchHistoryDatabase() {
        super(BaseApplication.getContext().getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        initialize();
    }

    public static SearchHistoryDatabase getInstance(){
        return instance;
    }

    private void initialize() {
        BaseApplication.getTaskThread().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (SearchHistoryDatabase.this) {
                    mDatabase = SearchHistoryDatabase.this.getWritableDatabase();
                }
            }
        });
    }

    // Creating Tables
    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String CREATE_SEARCH_HISTORY_TABLE = "CREATE TABLE " + TABLE_SEARCH_HISTORY + '('
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_URL + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_TIME_VISITED + " INTEGER" + ')';
        db.execSQL(CREATE_SEARCH_HISTORY_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_HISTORY);
        // Create tables again
        onCreate(db);
    }

    public synchronized void deleteSearchHistory() {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_SEARCH_HISTORY, null, null);
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

    public synchronized void deleteSearchHistoryItem(@NonNull SearchHistoryItem item) {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_SEARCH_HISTORY, KEY_URL + " = ? and " + KEY_TITLE + " = ?" , new String[]{item.getUrl(), item.getTitle()});
    }

    /**
     * 更新标题
     * @param url
     * @param title
     */
    public synchronized void updateTitle(@NonNull String url, @Nullable String title) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(title)) return;
        mDatabase = openIfNecessary();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);
        values.put(KEY_TIME_VISITED, System.currentTimeMillis());
        Cursor q = mDatabase.query(false, TABLE_SEARCH_HISTORY, new String[]{KEY_URL},
            KEY_URL + " = ?", new String[]{url}, null, null, null, "1");
        if (q.getCount() > 0) {
            mDatabase.update(TABLE_SEARCH_HISTORY, values, KEY_URL + " = ?", new String[]{url});
        }
        q.close();
    }

    /**
     * 更新访问时间
     * @param url
     * @param title
     */
    public synchronized void visitSearchHistoryItem(@NonNull String url, @Nullable String title) {
        if (TextUtils.isEmpty(title)) return;
        mDatabase = openIfNecessary();
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title == null ? "" : title);
        values.put(KEY_TIME_VISITED, System.currentTimeMillis());
        Cursor q = mDatabase.query(false, TABLE_SEARCH_HISTORY, new String[]{KEY_TITLE},
                KEY_TITLE + " = ?", new String[]{title}, null, null, null, "1");
        if (q.getCount() > 0) {
            mDatabase.update(TABLE_SEARCH_HISTORY, values, KEY_TITLE + " = ?", new String[]{title});
        } else {
            addSearchHistoryItem(new SearchHistoryItem(url, title == null ? "" : title));
        }
        q.close();
    }

    private synchronized void addSearchHistoryItem(@NonNull SearchHistoryItem item) {
        mDatabase = openIfNecessary();
        ContentValues values = new ContentValues();
        values.put(KEY_URL, item.getUrl());
        values.put(KEY_TITLE, item.getTitle());
        values.put(KEY_TIME_VISITED, System.currentTimeMillis());
        mDatabase.insert(TABLE_SEARCH_HISTORY, null, values);
    }

    @NonNull
    public synchronized List<SearchHistoryItem> getLastItems() {
        mDatabase = openIfNecessary();
        List<SearchHistoryItem> itemList = new ArrayList<>(100);
        String selectQuery = "SELECT * FROM " + TABLE_SEARCH_HISTORY + " ORDER BY " + KEY_TIME_VISITED
            + " DESC";

        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        int counter = 0;
        if (cursor.moveToFirst()) {
            do {
                SearchHistoryItem item = new SearchHistoryItem();
                item.setUrl(cursor.getString(1));
                item.setTitle(cursor.getString(2));
                itemList.add(item);
                counter++;
            } while (cursor.moveToNext() && counter < 25);
        }
        cursor.close();
        return itemList;
    }

    @NonNull
    public synchronized List<SearchHistoryItem> getAllSearchHistoryItems() {
        mDatabase = openIfNecessary();
        List<SearchHistoryItem> itemList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SEARCH_HISTORY + " ORDER BY " + KEY_TIME_VISITED
            + " DESC";

        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SearchHistoryItem item = new SearchHistoryItem();
                item.setUrl(cursor.getString(1));
                item.setTitle(cursor.getString(2));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

    public synchronized int getSearchHistoryItemsCount() {
        mDatabase = openIfNecessary();
        String countQuery = "SELECT * FROM " + TABLE_SEARCH_HISTORY;
        Cursor cursor = mDatabase.rawQuery(countQuery, null);
        int n = cursor.getCount();
        cursor.close();
        return n;
    }
}

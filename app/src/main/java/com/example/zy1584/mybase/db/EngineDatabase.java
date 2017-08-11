/*
 * Copyright 2014 A.C.R. Development
 */
package com.example.zy1584.mybase.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.zy1584.mybase.base.BaseApplication;
import com.example.zy1584.mybase.bean.EngineBean.EngineItem;

import java.util.ArrayList;
import java.util.List;


public class EngineDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "engineManager";

    // EngineItems table name
    private static final String TABLE_ENGINE = "engine";

    // EngineItems Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_IS_DEFAULT = "isDefault";
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS_URL = "addrUrl";
    public static final String KEY_ICON_URL = "iconUrl";

    @Nullable
    private SQLiteDatabase mDatabase;

    private static final EngineDatabase instance = new EngineDatabase();

    private EngineDatabase() {
        super(BaseApplication.getContext().getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        initialize();
    }

    public static EngineDatabase getInstance() {
        return instance;
    }

    private void initialize() {
        BaseApplication.getTaskThread().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (EngineDatabase.this) {
                    mDatabase = EngineDatabase.this.getWritableDatabase();
                }
            }
        });
    }

    // Creating Tables
    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String CREATE_ENGINE_TABLE = "CREATE TABLE " + TABLE_ENGINE + '('
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_IS_DEFAULT + " INTEGER,"
                + KEY_NAME + " TEXT,"
                + KEY_ADDRESS_URL + " TEXT,"
                + KEY_ICON_URL + " TEXT" + ')';
        db.execSQL(CREATE_ENGINE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENGINE);
        // Create tables again
        onCreate(db);
    }

    public synchronized void deleteEngine() {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_ENGINE, null, null);
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

    public synchronized void setDefaultEngine(@NonNull String addressUrl) {
        mDatabase = openIfNecessary();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_DEFAULT, 0);
        mDatabase.update(TABLE_ENGINE, values, KEY_ADDRESS_URL + " != ?", new String[]{addressUrl});

        values.clear();
        values.put(KEY_IS_DEFAULT, 1);
        mDatabase.update(TABLE_ENGINE, values, KEY_ADDRESS_URL + " = ?", new String[]{addressUrl});
    }

    public synchronized void addEngineItem(@NonNull EngineItem item) {
        mDatabase = openIfNecessary();
        mDatabase.insert(TABLE_ENGINE, null, item.toContentValues());
    }

    @NonNull
    public synchronized List<EngineItem> getAllEngineItems() {
        mDatabase = openIfNecessary();
        List<EngineItem> itemList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_ENGINE + " ORDER BY " + KEY_ID
                + " DESC";

        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                EngineItem item = new EngineItem();
                item.setIsDefault(cursor.getInt(1));
                item.setName(cursor.getString(2));
                item.setAddrUrl(cursor.getString(3));
                item.setIconUrl(cursor.getString(4));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

    @NonNull
    public synchronized EngineItem getDefaultEngine() {
        mDatabase = openIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_ENGINE, null,
                KEY_IS_DEFAULT + " = ?", new String[]{Integer.toString(1)}, null, null, null, null);
        EngineItem item = null;
        if (cursor.moveToFirst()) {
            item = new EngineItem();
            item.setIsDefault(cursor.getInt(1));
            item.setName(cursor.getString(2));
            item.setAddrUrl(cursor.getString(3));
            item.setIconUrl(cursor.getString(4));
        }
        cursor.close();
        return item;
    }

}

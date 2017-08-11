package com.example.zy1584.mybase.dialog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.zy1584.mybase.base.BaseApplication;
import com.example.zy1584.mybase.ui.download.db.FileItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy1584 on 2017-7-19.
 */

public class FileDatabase extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "fileManager";

    // HistoryItems table name
    private static final String TABLE_FILE = "file";

    // HistoryItems Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_URL = "url";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_PATH = "path";
    public static final String KEY_STATUS = "status";
    public static final String KEY_TIME = "timestamp";
    public static final String KEY_CLICK_ID = "click_id"; // 腾讯广告联盟统计使用，丧心病狂
    public static final String KEY_CONVERSION_LINK = "conversion_link"; // 腾讯广告联盟统计使用，丧心病狂

    @Nullable
    private SQLiteDatabase mDatabase;

    private static final FileDatabase instance = new FileDatabase();

    public static FileDatabase getInstance(){
        return instance;
    }

    private FileDatabase(){
        super(BaseApplication.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        initialize();
    }

    private void initialize() {
        BaseApplication.getTaskThread().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (FileDatabase.this) {
                    mDatabase = FileDatabase.this.getWritableDatabase();
                }
            }
        });
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FILE_TABLE = "CREATE TABLE " + TABLE_FILE + '('
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_URL + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_TYPE + " INTEGER,"
                + KEY_PATH + " TEXT,"
                + KEY_STATUS + " INTEGER,"
                + KEY_TIME + " NOT NULL DEFAULT (datetime('now','localtime')),"
                + KEY_CLICK_ID + " TEXT,"
                + KEY_CONVERSION_LINK + " TEXT"+ ')';
        db.execSQL(CREATE_FILE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE);
        // Create tables again
        onCreate(db);
    }

    public synchronized void deleteFile() {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_FILE, null, null);
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

    private synchronized void addFileItem(@NonNull FileItem item) {
        mDatabase = openIfNecessary();
        mDatabase.insert(TABLE_FILE, null, item.toContentValues());
    }

    public synchronized void deleteFileItem(@NonNull int id) {
        mDatabase = openIfNecessary();
        mDatabase.delete(TABLE_FILE, KEY_ID + " = ?", new String[]{Integer.toString(id)});
    }

    public synchronized void updateFileItem(@NonNull FileItem item) {
        mDatabase = openIfNecessary();
        Cursor q = mDatabase.query(false, TABLE_FILE, new String[]{KEY_ID},
                KEY_ID + " = ?", new String[]{Integer.toString(item.getId())}, null, null, null, "1");
        if (q.getCount() > 0) {
            mDatabase.update(TABLE_FILE, item.toContentValues(), KEY_ID + " = ?", new String[]{Integer.toString(item.getId())});
        } else {
            addFileItem(item);
        }
        q.close();
    }

    @NonNull
    public synchronized List<FileItem> getAllFileItems() {
        mDatabase = openIfNecessary();
        List<FileItem> itemList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FILE + " ORDER BY " + KEY_ID
                + " DESC";

        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                FileItem item = new FileItem();
                item.setId(cursor.getInt(0));
                item.setUrl(cursor.getString(1));
                item.setName(cursor.getString(2));
                item.setType(cursor.getInt(3));
                item.setPath(cursor.getString(4));
                item.setStatus(cursor.getInt(5));
                item.setTimestamp(cursor.getString(6));
                item.setClickId(cursor.getString(7));
                item.setConversionLink(cursor.getString(8));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

    @Nullable
    public synchronized String getFileItem(@NonNull int id) {
        mDatabase = openIfNecessary();
        Cursor cursor = mDatabase.query(TABLE_FILE, new String[]{KEY_ID, KEY_URL, KEY_NAME},
                KEY_ID + " = ?", new String[]{Integer.toString(id)}, null, null, null, null);
        String m = null;
        if (cursor != null) {
            cursor.moveToFirst();
            m = cursor.getString(0);

            cursor.close();
        }
        return m;
    }
}

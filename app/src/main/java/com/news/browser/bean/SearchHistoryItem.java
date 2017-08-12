/*
 * Copyright 2014 A.C.R. Development
 */
package com.news.browser.bean;

import android.support.annotation.NonNull;


public class SearchHistoryItem {

    // private variables
    @NonNull
    private String mUrl = "";

    @NonNull
    private String mTitle = "";

    private int time;

    public SearchHistoryItem() {
    }

    public SearchHistoryItem(@NonNull String mUrl, @NonNull String mTitle) {
        this.mUrl = mUrl;
        this.mTitle = mTitle;
    }

    @NonNull
    public String getUrl() {
        return mUrl;
    }

    public void setUrl(@NonNull String mUrl) {
        this.mUrl = mUrl;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(@NonNull String mTitle) {
        this.mTitle = mTitle;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}

package com.news.browser.bean;

/**
 * Created by zy1584 on 2017-8-13.
 */

public class HotSiteTitleBean implements BaseItem {

    private String title;

    public HotSiteTitleBean(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

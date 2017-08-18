package com.news.browser.bus;

public class RXEvent {
    public static final String TAG_BROWSER_MSG = "tag_browser_msg";
    public static final String TAG_SEARCH = "tag_search";
    public static final String TAG_NOTIFY_DATA = "tag_notify_data";
    public static final String TAG_UPDATE_DEFAULT_ENGINE = "tag_update_default_engine";

    private String tag;
    private String msg;
    private int msgId;

    public RXEvent(String tag, int msgId) {
        this.tag = tag;
        this.msgId = msgId;
    }

    public RXEvent(String tag, String msg) {
        this.tag = tag;
        this.msg = msg;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
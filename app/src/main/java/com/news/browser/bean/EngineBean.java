package com.news.browser.bean;

import android.content.ContentValues;

import com.news.browser.db.EngineDatabase;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-10.
 */

public class EngineBean {

    /**
     * ret : 0
     * msg :
     * data : [{"isDefault":1,"name":"google+","addrUrl":"www.google.com","iconUrl":""}]
     */

    private int ret;
    private String msg;
    private List<EngineItem> data;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<EngineItem> getData() {
        return data;
    }

    public void setData(List<EngineItem> data) {
        this.data = data;
    }

    public static class EngineItem {
        /**
         * isDefault : 1
         * name : google+
         * addrUrl : www.google.com
         * iconUrl :
         */

        private int isDefault;
        private String name;
        private String addrUrl;
        private String iconUrl;

        public int getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(int isDefault) {
            this.isDefault = isDefault;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddrUrl() {
            return addrUrl;
        }

        public void setAddrUrl(String addrUrl) {
            this.addrUrl = addrUrl;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public ContentValues toContentValues() {
            ContentValues cv = new ContentValues();
            cv.put(EngineDatabase.KEY_IS_DEFAULT, isDefault);
            cv.put(EngineDatabase.KEY_NAME, name);
            cv.put(EngineDatabase.KEY_ADDRESS_URL, addrUrl);
            cv.put(EngineDatabase.KEY_ICON_URL, iconUrl);
            return cv;
        }
    }
}

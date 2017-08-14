package com.news.browser.bean;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-14.
 */

public class FeedbackTypeBean {

    private List<TypelistBean> typelist;

    public List<TypelistBean> getTypelist() {
        return typelist;
    }

    public void setTypelist(List<TypelistBean> typelist) {
        this.typelist = typelist;
    }

    public static class TypelistBean {
        /**
         * typeName : 网页打不开
         * id : 10
         */

        private String typeName;
        private int id;

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}

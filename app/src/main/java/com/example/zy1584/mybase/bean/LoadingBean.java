package com.example.zy1584.mybase.bean;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-8.
 */

public class LoadingBean {

    /**
     * ret : 0
     * msg :
     * data : [{"pictureUrl":"http://119.29.185.237:9629/tmp.jpg","name":"鍝熷摕鍝�","showSecond":3}]
     */

    private int ret;
    private String msg;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * pictureUrl : http://119.29.185.237:9629/tmp.jpg
         * name : 鍝熷摕鍝�
         * showSecond : 3
         */

        private String pictureUrl;
        private String name;
        private int showSecond;

        public String getPictureUrl() {
            return pictureUrl;
        }

        public void setPictureUrl(String pictureUrl) {
            this.pictureUrl = pictureUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getShowSecond() {
            return showSecond;
        }

        public void setShowSecond(int showSecond) {
            this.showSecond = showSecond;
        }
    }
}

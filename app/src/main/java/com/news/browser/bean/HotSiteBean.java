package com.news.browser.bean;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-13.
 */

public class HotSiteBean {

    /**
     * ret : 0
     * msg :
     * data : [{"iconUrl":"","classifyName":"啊啊啊","name":"ghhgh","classifyId":2,"addrUrl":"hhh"},{"iconUrl":"http://119.29.185.237:9629/tmp.jpg","classifyName":"啊啊啊","name":"唯品会","classifyId":2,"addrUrl":"http://www.vip.com/"},{"iconUrl":"http://119.29.185.237:9629/tmp.jpg","classifyName":"啊啊啊","name":"聚美优品","classifyId":2,"addrUrl":"http://www.jumei.com/"},{"iconUrl":"http://119.29.185.237:9629/tmp.jpg","classifyName":"啊啊啊","name":"花椒直播","classifyId":2,"addrUrl":"http://www.huajiao.com/"},{"iconUrl":"http://ds-browser.oss-cn-shenzhen.aliyuncs.com/picture/icon_1502370429.jpg","classifyName":"哦哦哦","name":"hhh","classifyId":3,"addrUrl":"hh"},{"iconUrl":"http://119.29.185.237:9629/tmp.jpg","classifyName":"哦哦哦","name":"人民网","classifyId":3,"addrUrl":"http://www.people.com/"},{"iconUrl":"http://119.29.185.237:9629/tmp.jpg","classifyName":"哦哦哦","name":"中国网","classifyId":3,"addrUrl":"http://www.china.com/"},{"iconUrl":"http://119.29.185.237:9629/tmp.jpg","classifyName":"哦哦哦","name":"央视网","classifyId":3,"addrUrl":"http://www.cctv.com/"}]
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

    public static class DataBean implements BaseItem{
        /**
         * iconUrl :
         * classifyName : 啊啊啊
         * name : ghhgh
         * classifyId : 2
         * addrUrl : hhh
         */

        private String iconUrl;
        private String classifyName;
        private String name;
        private int classifyId;
        private String addrUrl;

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public String getClassifyName() {
            return classifyName;
        }

        public void setClassifyName(String classifyName) {
            this.classifyName = classifyName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getClassifyId() {
            return classifyId;
        }

        public void setClassifyId(int classifyId) {
            this.classifyId = classifyId;
        }

        public String getAddrUrl() {
            return addrUrl;
        }

        public void setAddrUrl(String addrUrl) {
            this.addrUrl = addrUrl;
        }
    }
}

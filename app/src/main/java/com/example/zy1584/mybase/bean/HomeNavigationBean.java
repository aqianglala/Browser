package com.example.zy1584.mybase.bean;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-7.
 */

public class HomeNavigationBean {

    /**
     * ret : 0
     * msg :
     * data : [{"name":"鐧惧害","addrUrl":"https://www.baidu.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鏂版氮","addrUrl":"http://www.sina.com.cn/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"澶╃尗","addrUrl":"https://www.tmall.com","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鎼滅嫄","addrUrl":"http://www.sohu.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"缃戞槗","addrUrl":"http://www.163.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鍑ゅ嚢","addrUrl":"http://www.ifeng.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鑻忓畞","addrUrl":"https://www.suning.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鎼虹▼","addrUrl":"http://www.ctrip.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鑹洪緳","addrUrl":"http://www.elong.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鍞搧浼�","addrUrl":"http://www.vip.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鑱氱編浼樺搧","addrUrl":"http://www.jumei.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"},{"name":"鑺辨鐩存挱","addrUrl":"http://www.huajiao.com/","iconUrl":"http://119.29.185.237:9629/tmp.jpg"}]
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
         * name : 鐧惧害
         * addrUrl : https://www.baidu.com/
         * iconUrl : http://119.29.185.237:9629/tmp.jpg
         */

        private String name;
        private String addrUrl;
        private String iconUrl;

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
    }
}

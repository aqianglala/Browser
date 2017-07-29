package com.example.zy1584.mybase.bean;

/**
 * Created by zy1584 on 2017-7-28.
 */

public class ClickLinkResponseBean {

    /**
     * data : {"clickid":"bpshuwidaaajrbfcnbkq","dstlink":"http://dd.myapp.com/16891/DB7F759C534A54811C65FA3674573632.apk?fsname=com.naver.linewebtoon.cn_1.1.3_110300.apk&_gdt_ma_cdn_cb=1&qz_gdt=bpshuwidaaajrbfcnbkq&appid=1105694519"}
     * ret : 0
     */

    private DataBean data;
    private int ret;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public static class DataBean {
        /**
         * clickid : bpshuwidaaajrbfcnbkq
         * dstlink : http://dd.myapp.com/16891/DB7F759C534A54811C65FA3674573632.apk?fsname=com.naver.linewebtoon.cn_1.1.3_110300.apk&_gdt_ma_cdn_cb=1&qz_gdt=bpshuwidaaajrbfcnbkq&appid=1105694519
         */

        private String clickid;
        private String dstlink;

        public String getClickid() {
            return clickid;
        }

        public void setClickid(String clickid) {
            this.clickid = clickid;
        }

        public String getDstlink() {
            return dstlink;
        }

        public void setDstlink(String dstlink) {
            this.dstlink = dstlink;
        }
    }
}

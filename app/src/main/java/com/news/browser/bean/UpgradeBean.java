package com.news.browser.bean;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-8.
 */

public class UpgradeBean {

    /**
     * ret : 0
     * msg :
     * data : [{"updateInfo":"升级标题标题标题升级标题标题标题\n升级标题标题标题升级标题标题标题\n升级标题标题标题升级标题标题标题\n升级标题标题标题升级标题标题标题","packageName":"com.news.browser","limitNet":1,"fileSize":4454073,"versionCode":2,"appMd5":"18686a490290380d5cec23bb138021c2","updateTitle":"升级标题标题标题","signatureMd5":"5bbdf510d8f7d36f9221dce51ace4c5b","downloadUrl":"http://ds-browser.oss-cn-shenzhen.aliyuncs.com/apk/apk_1502778240.apk","isForce":0,"minversionCode":15,"versionName":"1.0.1"}]
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
         * updateInfo : 升级标题标题标题升级标题标题标题
         升级标题标题标题升级标题标题标题
         升级标题标题标题升级标题标题标题
         升级标题标题标题升级标题标题标题
         * packageName : com.news.browser
         * limitNet : 1
         * fileSize : 4454073
         * versionCode : 2
         * appMd5 : 18686a490290380d5cec23bb138021c2
         * updateTitle : 升级标题标题标题
         * signatureMd5 : 5bbdf510d8f7d36f9221dce51ace4c5b
         * downloadUrl : http://ds-browser.oss-cn-shenzhen.aliyuncs.com/apk/apk_1502778240.apk
         * isForce : 0
         * minversionCode : 15
         * versionName : 1.0.1
         */

        private String updateInfo;
        private String packageName;
        private int limitNet;
        private int fileSize;
        private int versionCode;
        private String appMd5;
        private String updateTitle;
        private String signatureMd5;
        private String downloadUrl;
        private int isForce;
        private int minversionCode;
        private String versionName;

        public String getUpdateInfo() {
            return updateInfo;
        }

        public void setUpdateInfo(String updateInfo) {
            this.updateInfo = updateInfo;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public int getLimitNet() {
            return limitNet;
        }

        public void setLimitNet(int limitNet) {
            this.limitNet = limitNet;
        }

        public int getFileSize() {
            return fileSize;
        }

        public void setFileSize(int fileSize) {
            this.fileSize = fileSize;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getAppMd5() {
            return appMd5;
        }

        public void setAppMd5(String appMd5) {
            this.appMd5 = appMd5;
        }

        public String getUpdateTitle() {
            return updateTitle;
        }

        public void setUpdateTitle(String updateTitle) {
            this.updateTitle = updateTitle;
        }

        public String getSignatureMd5() {
            return signatureMd5;
        }

        public void setSignatureMd5(String signatureMd5) {
            this.signatureMd5 = signatureMd5;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public int getIsForce() {
            return isForce;
        }

        public void setIsForce(int isForce) {
            this.isForce = isForce;
        }

        public int getMinversionCode() {
            return minversionCode;
        }

        public void setMinversionCode(int minversionCode) {
            this.minversionCode = minversionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }
    }
}

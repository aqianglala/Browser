package com.news.browser.bean;


/**
 * Created by zy1584 on 2017-7-27.
 */

public class ADRequestBean {

    public static class Pos {
        private long id; // 广告位id
        private int width;
        private int height;// 原声广告不填
        private int ad_count;// 原生广告位取值不超过 10； 其它广告位只能填 1。

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getAd_count() {
            return ad_count;
        }

        public void setAd_count(int ad_count) {
            this.ad_count = ad_count;
        }
    }

    public static class Media{

        private String app_id;// 平台创建的应用id

        private String app_bundle_id; // 包名

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public String getApp_bundle_id() {
            return app_bundle_id;
        }

        public void setApp_bundle_id(String app_bundle_id) {
            this.app_bundle_id = app_bundle_id;
        }
    }

    public static class Device{

        private String os;
        private String os_version;
        private String model;
        private String manufacturer;
        private int device_type;
        private String imei;
        private String android_id;

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getOs_version() {
            return os_version;
        }

        public void setOs_version(String os_version) {
            this.os_version = os_version;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public int getDevice_type() {
            return device_type;
        }

        public void setDevice_type(int device_type) {
            this.device_type = device_type;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getAndroid_id() {
            return android_id;
        }

        public void setAndroid_id(String android_id) {
            this.android_id = android_id;
        }

    }

    public static class Network{
        private int connect_type;
        private int carrier;

        public int getConnect_type() {
            return connect_type;
        }

        public void setConnect_type(int connect_type) {
            this.connect_type = connect_type;
        }

        public int getCarrier() {
            return carrier;
        }

        public void setCarrier(int carrier) {
            this.carrier = carrier;
        }
    }

}

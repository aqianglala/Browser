package com.example.zy1584.mybase.ui.main.bean;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class ChannelBean {

    /**
     * chanCode : news
     * chanId : 0
     * chanName : 要闻
     * chanName_m : 要闻
     * isKB : 0
     */

    private String chanCode;
    private String chanId;
    private String chanName;
    private String chanName_m;
    private String isKB;

    public ChannelBean(String chanCode, String chanId, String chanName, String chanName_m, String isKB) {
        this.chanCode = chanCode;
        this.chanId = chanId;
        this.chanName = chanName;
        this.chanName_m = chanName_m;
        this.isKB = isKB;
    }

    public String getChanCode() {
        return chanCode;
    }

    public void setChanCode(String chanCode) {
        this.chanCode = chanCode;
    }

    public String getChanId() {
        return chanId;
    }

    public void setChanId(String chanId) {
        this.chanId = chanId;
    }

    public String getChanName() {
        return chanName;
    }

    public void setChanName(String chanName) {
        this.chanName = chanName;
    }

    public String getChanName_m() {
        return chanName_m;
    }

    public void setChanName_m(String chanName_m) {
        this.chanName_m = chanName_m;
    }

    public String getIsKB() {
        return isKB;
    }

    public void setIsKB(String isKB) {
        this.isKB = isKB;
    }
}

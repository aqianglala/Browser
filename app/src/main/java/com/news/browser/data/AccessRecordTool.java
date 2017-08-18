package com.news.browser.data;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.news.browser.base.BaseApplication;
import com.news.browser.bean.EngineBean;
import com.news.browser.utils.ForegroundCallbacks;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zy1584 on 2017-8-18.
 */

public class AccessRecordTool implements ForegroundCallbacks.Listener {

    public static final int TIMER_MSG = 1;
    public static final int TIMER_DURATION = 30 * 1000; //30s
    private long mAccStartTime;// 一次访问开始时间
    private long mAccStopTime;// 一次访问结束时间

    private boolean isClose;
    private static Context mContext = null;

    private static final AccessRecordTool instance = new AccessRecordTool();

    public static AccessRecordTool getInstance() {
        return instance;
    }

    private AccessRecordTool() {
        mContext = BaseApplication.getContext();
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_MSG: {
                    mHandler.removeCallbacksAndMessages(null);
                    addAccessDuration(false);// 记录
                    reportData();
                    break;
                }
            }
        }
    };

    private void reportData() {

    }

    /**
     * 记录访问时长
     */
    private void addAccessDuration(boolean isActive) {
        if (isActive) {// 主动关闭
            mAccStopTime = System.currentTimeMillis();
        }
        if (mAccStartTime == 0 || mAccStopTime == 0) return;
        if (mAccStopTime < mAccStartTime) return;
        try {
            JSONObject jo = new JSONObject();
            jo.put("ot", getCurrentTimePoint(mAccStartTime));
            jo.put("ct", getCurrentTimePoint(mAccStopTime));
            jo.put("dur", Utils.halfUp((mAccStopTime - mAccStartTime) / 1000f));
            jo.put("net", NetUtils.getNetworkClass(mContext));
//            addRecord(TIMEKEY, jo);
            clearDuration();// 清零
        } catch (JSONException e) {
            e.printStackTrace();
            clearDuration();// 清零
        }
    }

    private void clearDuration() {
        mAccStartTime = 0;
        mAccStopTime = 0;
    }

    public static String getCurrentTimePoint(long milliseconds) {
        Date currData = new Date(milliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(currData);
    }

    @Override
    public void onBecameForeground() {
        setClose(false);// 设置为默认非主动关闭
        mHandler.removeCallbacksAndMessages(null);
        if (mAccStartTime == 0) {// 已经记录
            mAccStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onBecameBackground() {
        mAccStopTime = System.currentTimeMillis();
        Message msg = mHandler.obtainMessage(TIMER_MSG);
        mHandler.sendMessageDelayed(msg, TIMER_DURATION);
    }

    /**
     * 搜索
     * @param keyWord
     * @param defaultEngine
     */
    public void recordSearch(String keyWord, EngineBean.EngineItem defaultEngine){


    }

    /**
     * 广告和新闻曝光
     */
    public void reportExpose(){

    }

    /**
     * 首页导航点击
     */
    public void clickNavigation(){

    }

    /**
     * 热门标签点击
     */
    public void clickHotTag(){

    }

    /**
     * 热门网址访问，访问可以抽取出来，点击也可以抽取出来
     */
    public void accessHotSite(){

    }
}

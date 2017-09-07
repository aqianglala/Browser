package com.news.browser.data;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.news.browser.base.BaseApplication;
import com.news.browser.bean.ADBean;
import com.news.browser.http.Http;
import com.news.browser.http.HttpService;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.utils.ForegroundCallbacks;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.SPUtils;
import com.news.browser.utils.Utils;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Subscriber;

import static com.news.browser.utils.SPUtils.get;

/**
 * Created by zy1584 on 2017-8-18.
 */

public class AccessRecordTool implements ForegroundCallbacks.Listener, ForegroundCallbacks.AppCloseListener {

    // 页面
    public static final int PG_START_UP = 1001;
    public static final int PG_HOME = 1002;
    public static final int PG_RIGHT_SCREEN = 1003;
    public static final int PG_SEARCH = 1004;
    public static final int PG_SET = 1005;
    public static final int PG_HOT_NEWS = 1006;
    public static final int PG_WEATHER = 1007;
    public static final int PG_FAVORITE = 1008;
    public static final int PG_HISTORY = 1009;
    public static final int PG_DOWNLOAD = 1010;
    public static final int PG_BROWSER = 1011;
    public static final int PG_HOT_SITE = 1012;

    // 事件
    public static final int EV_START = 101;
    public static final int EV_LOAD = 102;
    public static final int EV_OPEN = 103;
    public static final int EV_REMAIN = 104;
    public static final int EV_SEARCH = 105;
    public static final int EV_CLICK = 106;
    public static final int EV_SHOW = 107;
    public static final int EV_INSTALL = 109;
    public static final int EV_UPGRADE = 110;
    public static final int EV_FAVORITE = 111;
    public static final int EV_SCAN = 112;
    public static final int EV_REFRESH = 113;
    public static final int EV_DELETE = 114;
    public static final int EV_EXIT = 115;

    public static final int TYPE_AD = 101;
    public static final int TYPE_NEWS = 102;
    public static final int TYPE_NAVIGATION = 103;
    public static final int TYPE_HOT_TAG = 104;
    public static final int TYPE_DUR_ALL = 105;
    public static final int TYPE_DUR_PAGE = 106;
    public static final int TYPE_HOT_SITE = 107;

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
        ForegroundCallbacks.get(BaseApplication.getContext()).addListener(this);
        ForegroundCallbacks.get(BaseApplication.getContext()).addAppCloseListener(this);
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
                    break;
                }
            }
        }
    };

    /**
     * 记录访问时长
     */
    private synchronized void addAccessDuration(boolean isActive) {
        if (isActive) {// 主动关闭
            mAccStopTime = System.currentTimeMillis();
        }
        if (mAccStartTime == 0 || mAccStopTime == 0) return;
        if (mAccStopTime < mAccStartTime) return;
        String s = Utils.halfUp((mAccStopTime - mAccStartTime) / 1000f);
        String launch_type = (String) SPUtils.get(GlobalParams.LAUNCH_TYPE, "0");

        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(0, 0, EV_REMAIN);
        params.put("Type", TYPE_DUR_ALL + "");
        params.put("Duration", TextUtils.isEmpty(s) ? "" : s);
        params.put("Flag", launch_type);
        params.put("Content", mAccStartTime + "");

        String url = getUrl(GlobalParams.UPLOAD);
        uploadRecord(url, params);

        clearDuration();// 清零
    }

    private void clearDuration() {
        mAccStartTime = 0;
        mAccStopTime = 0;
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

    @Override
    public void onClose() {
        mHandler.removeCallbacksAndMessages(null);
        addAccessDuration(true);
    }

    /**
     * 搜索
     *
     * @param lastPage
     * @param keyWord
     * @param addressUrl
     */
    public synchronized void recordSearch(int lastPage, String keyWord, String addressUrl) {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(lastPage, PG_SEARCH, EV_SEARCH);
        params.put("Content", keyWord);
        params.put("Extras", encode(addressUrl));
        String url = getUrl(GlobalParams.UPLOAD);
        uploadRecord(url, params);
    }

    /**
     * 广告和新闻曝光
     */
    public synchronized void reportADExpose(final ADBean bean, String channelName, String title, int event, int type) {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(0, PG_HOT_NEWS, event);
        params.put("Content", channelName);
        params.put("Extras", title);
        params.put("Type", type + "");
        params.put("Source", "1");
        String url = getUrl(GlobalParams.UPLOAD);

        Logger.t("upload").d(params);
        HttpService httpService = Http.getHttpService();
        RequestBody body = getRequestBody(params);
        httpService.uploadRecord(url, body).compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "自营统计上传失败");
                        bean.setSelfTiming(false);
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        bean.setSelfTiming(false);
                        boolean isSuccess = checkUploadRespond(body);
                        if (isSuccess) {
                            bean.setHasExpose2Self(true);
                        }
                    }
                });
    }

    /**
     * 广告和新闻点击
     */
    public synchronized void reportADOrNewsClick(String channelName, String title, int event, int type) {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(0, PG_HOT_NEWS, event);
        params.put("Content", channelName);
        params.put("Extras", title);
        params.put("Type", type + "");
        params.put("Source", "1");
        String url = getUrl(GlobalParams.UPLOAD);
        uploadRecord(url, params);
    }

    /**
     * 首页导航点击
     */
    public synchronized void reportClick(int lastPage, int currentPage, int type, int position, String content, String addressUrl) {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(lastPage, currentPage, EV_CLICK);
        params.put("Type", type + "");
        params.put("Position", (position + 1) + "");
        params.put("Content", content);
        params.put("Extras", encode(addressUrl));
        String url = getUrl(GlobalParams.UPLOAD);
        uploadRecord(url, params);
    }

    /**
     * 页面访问
     */
    public synchronized void accessPage(int lastPage, int currentPage, String pageName) {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(lastPage, currentPage, EV_OPEN);
        if (!TextUtils.isEmpty(pageName)) {
            params.put("Content", pageName);
        }
        if (currentPage == PG_HOT_NEWS) {
            params.put("Source", "1");
        }
        String url = getUrl(GlobalParams.UPLOAD);
        uploadRecord(url, params);
    }

    /**
     * 搜索页面访问
     */
    public synchronized void accessSearchPage(int lastPage, int currentPage, String defaultEngine) {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseRecordParams(lastPage, currentPage, EV_OPEN);
        if (!TextUtils.isEmpty(defaultEngine)) {
            params.put("Extras", encode(defaultEngine));
        }
        String url = getUrl(GlobalParams.UPLOAD);
        uploadRecord(url, params);
    }

    private synchronized void uploadRecord(String url, Map<String, String> map) {
        Logger.t("upload").d(map);
        HttpService httpService = Http.getHttpService();
        RequestBody body = getRequestBody(map);
        httpService.uploadRecord(url, body).compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "自营统计上传失败");
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        checkUploadRespond(body);
                    }
                });
    }

    private String encode(String str){
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    private RequestBody getRequestBody(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return RequestBody.create(MediaType.parse("text/plain"), URLEncoder.encode(sb.toString()));
    }

    private boolean checkUploadRespond(ResponseBody body) {
        if (body != null) {
            try {
                JSONObject jsonObject = new JSONObject(body.string());
                int ret = jsonObject.optInt("ret");
                if (ret == 0) {
                    Logger.e("自营统计上传成功");
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    protected String getUrl(String sub) {
        String newIp = (String) get(GlobalParams.RECORD_IP, GlobalParams.HOLDER_HOST_RECORD);
        String newPort = (String) get(GlobalParams.RECORD_PORT, GlobalParams.HOLDER_PORT_RECORD + "");
        if (TextUtils.isEmpty(newIp)) {
            newIp = GlobalParams.HOLDER_HOST_RECORD;
        }
        if (TextUtils.isEmpty(newPort)) {
            newPort = GlobalParams.HOLDER_PORT_RECORD + "";
        }
        return "http://" + newIp + ":" + newPort + "/" + sub;
    }

}

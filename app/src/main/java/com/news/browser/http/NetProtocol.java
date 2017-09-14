package com.news.browser.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.news.browser.base.BaseApplication;
import com.news.browser.utils.ChannelUtil;
import com.news.browser.utils.LocationUtils;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.SPUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

import static com.news.browser.utils.SPUtils.get;
import static com.news.browser.utils.UIUtils.getResources;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NetProtocol {
    private static final String TAG = "NetProtocol";
    private static Context mContext = null;

    //基本参数定义成全局变量, 只初始化一次
    private static String mQueryIp = null;
    private static String mQueryMobile = null;
    private static String mQueryModel = null;
    private static String mQueryMobileType = null;
    private static String mQueryIMEI = null;
    private static String mAppID = null;
    private static String mAppEnv = null;
    private static String mVersionSDK = null;
    private static String mClientVersion = null;
    private static String mClientVersionCode = null;
    private static String mAccounts = null;
    private static String mSignType = null;
    private static String mSign = null;
    private static String mPayProvicer = null;

    private static String mBuildVersion = null;
    private static String mCpuAbi = null;
    private static String mHardware = null;

    private static String mPackageName = null;
    private static String mImsiCode = null;
    private static String mImsi = null;
    private static String mAndroid_id = null;
    private static String mManufacturer = null;

    private static final String app_bundle_id = "com.news.browser";
    private static final String app_id = "1104241296";
    private static final String ad_id = "8050018672826551";
    private static final String os = "android";
    private static final String unknown = "unknown";
    private static final String refer = "openapi_for_rcmduowei";
    private static final String appkey = "156847d34334b1cb5acd8ec8b742e40e";

    private static final String DEFAULTACCOUNT = "34543534";
    private static final String QUERYIP = "QueryIP";
    private static final String QUERYIMEI = "QueryIMEI";
    private static final String QUERYMOBILE = "QueryMobile";
    private static final String APPENV = "AppEnv";
    private static final String APPID = "AppID";
    private static final String CLIENTVERSION = "ClientVersion";// 版本名
    private static final String VERSIONSDK = "VersionSDK";// sdk版本
    private static final String ACCOUNTS = "Accounts";
    private static final String SIGNTYPE = "SignType";
    private static final String SIGN = "Sign";
    private static final String PAYPROVIDER = "PayProvider";
    private static final String MOBILETYPE = "MobileType";

    private static final String BUILDVERSION = "BuildVersion";
    private static final String VERSIONCODE = "VersionCode";// app版本号
    private static final String USERID = "UserId";
    private static final String CPUABI = "CpuAbi";
    private static final String HARDWARE = "HardWare";

    private static final String LONGITUDE = "Longitude";
    private static final String LATITUDE = "Latitude";


    private static final class HolderClass {
        private static final NetProtocol INSTANCE = new NetProtocol();
    }

    public static NetProtocol getImpl() {
        mContext = BaseApplication.getContext();
        return HolderClass.INSTANCE;
    }

    public NetProtocol() {
        initGlobalVar();
    }

    /**
     * 初始化静态变量
     */
    private void initGlobalVar() {

        if (mQueryMobileType == null) {
            mQueryMobileType = getMobileType();
        }

        if (mPayProvicer == null) {
            mPayProvicer = "" + getPayprovider();
        }

        if (mSign == null) {
            mSign = "1glihU9DPWee+UJ82u3+mw3Bdnr9u01at0M/xJnPsGuHh+JA5bk3zbWaoWhU6GmLab3dIM4JNdktTcEUI9/FBGhgfLO39BKX/eBCFQ3bXAmIZn4l26fiwoO613BptT44GTEtnPiQ6+tnLsGlVSrFZaLB9FVhrGfipH2SWJcnwYs=";
        }

        if (mAccounts == null) {
            mAccounts = getUserDefaultAccount();
        }

        if (mSignType == null) {
            mSignType = "RSA";
        }

        if (mQueryIp == null) {
            mQueryIp = getIpAddressString();
        }

        if (mQueryIMEI == null) {
            mQueryIMEI = getIMEI();
        }

        if (mQueryModel == null) {
            mQueryModel = getModel();
        }

        if (mQueryMobile == null) {
            mQueryMobile = getPhoneNumber();
        } else if (mQueryMobile.equals("00000000000")) {
            mQueryMobile = getPhoneNumber();
        }

        if (mAppID == null) {
            mAppID = getAppName();
            mAppID = "DoovBrowser";
        }

        if (mAppEnv == null) {
            mAppEnv = getAppENV();
        }

        if (mBuildVersion == null) {
            mBuildVersion = getSystemVersion();
        }

        if (mCpuAbi == null) {
            mCpuAbi = getCpuAbi();
        }

        if (mHardware == null) {
            mHardware = getHardware();
        }

        if (mClientVersion == null) {
            getAPPVersion(mContext);
        }

        if (mPackageName == null) {
            mPackageName = mContext.getPackageName();
        }

        if (mImsiCode == null) {
            mImsiCode = getImsiCode();
        }

        if (mImsi == null) {
            mImsi = getImsi();
        }

        if (mAndroid_id == null) {
            mAndroid_id = getAndroidId();
        }
        if (mManufacturer == null) {
            mManufacturer = getManufacturer();
        }
        if (mVersionSDK == null) {
            mVersionSDK = getAppEnvSdkIntVersion();
        }
    }

    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    private String getMobileType() {
        return Build.DEVICE.replace(" ", "_");
    }

    /**
     * 获取进程名字
     */
    private String getAppName() {
        // TODO Auto-generated method stub
        return "DoovBrowser";
    }

    /**
     * 获取imei号
     */
    public String getIMEI() {
        if (mContext != null) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if (imei == null) {
                return "000000000000000";
            } else if (imei.equals("") || imei.length() < 14) {
                return "000000000000000";
            } else if (imei.length() > 15) {
                imei = imei.substring(imei.length() - 15);
            }
            return imei;
        } else {
            return "000000000000000";
        }
    }

    /**
     * 获取手机型号?获取手机的设备名称?
     *
     * @return
     */
    private String getModel() {
//        return Build.DEVICE.replace(" ", "_");
        return Build.MODEL;
    }

    /**
     * 获取电话号码
     *
     * @return
     */
    public String getPhoneNumber() {
        String nb = null;
        if (mContext != null) {
            try {
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                nb = tm.getLine1Number();
                if (nb == null) {
                    return nb = "00000000000";
                } else if (nb.equals("") || nb.length() < 11) {
                    return nb = "00000000000";
                } else if (nb.length() > 11) {
                    nb = nb.substring(nb.length() - 11);
                    return nb;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return nb;
        } else {
            return "00000000000";
        }

    }

    /**
     * 获取账号
     */
    public static String getUserDefaultAccount() {
        // TODO Auto-generated method stub
        return DEFAULTACCOUNT;
    }

    /**
     * 获取系统android版本号
     */
    private String getAppENV() {
        return "system:android,version:" + android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取系统android版本号
     */
    private String getSystemVersionCode() {
        return "" + android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取系统的versioncode
     */
    private String getAppEnvSdkIntVersion() {
        return "" + android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取系统编译的版本号
     */
    private String getSystemVersion() {
        return Build.DISPLAY;
    }

    /**
     * 获取cpu体系结构
     *
     * @return
     */
    private String getCpuAbi() {
        String cpuabistr = null;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            String[] cpuLst = android.os.Build.SUPPORTED_ABIS;
            for (int i = 0; i < cpuLst.length; i++) {
                if (cpuabistr != null) {
                    cpuabistr += ("," + cpuLst[i]);
                } else {
                    cpuabistr = cpuLst[i];
                }
            }
        } else if (android.os.Build.VERSION.SDK_INT >= 8) {
            cpuabistr = android.os.Build.CPU_ABI + "," + android.os.Build.CPU_ABI2;
        } else {
            cpuabistr = android.os.Build.CPU_ABI;
        }

        if (cpuabistr == null) {
            cpuabistr = "unknown";
        }

        return cpuabistr;
    }

    /**
     * 获取支付信息
     */
    private String getPayprovider() {
        // TODO Auto-generated method stub
        return "0";
    }

    /**
     * 获取cpu型号
     *
     * @return
     */
    private String getHardware() {
        return android.os.Build.HARDWARE;
    }

    /**
     * 获取APP版本信息
     */
    public static void getAPPVersion(Context con) {
        try {
            PackageInfo pkgInfo = con.getPackageManager().getPackageInfo(con.getPackageName(), 0);
            mClientVersionCode = pkgInfo.versionCode + "";
            mClientVersion = pkgInfo.versionName;
            return;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mClientVersionCode = "1";
        mClientVersion = "1.0";
    }

    /**
     * 获取运营商的编码
     *
     * @return
     */
    private String getImsiCode() {
        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                //中国移动
                return "1";
            } else if (imsi.startsWith("46001")) {
                //中国联通
                return "2";
            } else if (imsi.startsWith("46003")) {
                //中国电信
                return "3";
            }
        } else {
            return "0";
        }

        return "0";
    }

    public String getImsi() {
        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();
        if (TextUtils.isEmpty(imsi)) {
            return "0";
        }
        return imsi;
    }

    public String getAndroidId() {
        String ANDROID_ID = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public boolean isTablet() {
        return (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    public HashMap<String, String> getChannelNewsQueryMap(int start, int size, String channelCode) {
        HashMap<String, String> params = getBaseParams2();
        params.put("channel_code", channelCode);
        params.put("start", start + "");
        params.put("size", size + "");
        return params;
    }

    public HashMap<String, String> getReportActionMap(String article_id, String chlid, String action_type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("refer", refer);
        params.put("appkey", appkey);
        params.put("article_id", article_id);
        params.put("chlid", chlid);
        params.put("imei", mQueryIMEI);
        params.put("imsi", mImsi);
        params.put("action_type", action_type);
        params.put("devinfo", mQueryIMEI);// TODO: 2017-7-27
        return params;
    }

    public HashMap<String, String> getADMap() {
        HashMap<String, String> map = getBaseParams2();
        map.put("ad_count", "10");

        map.put("app_bundle_id", app_bundle_id);

        map.put("os", checkNull(os));
        map.put("os_version", checkNull(mAppEnv));
        map.put("model", checkNull(mQueryModel));
        map.put("manufacturer", checkNull(mManufacturer));
        map.put("device_type", String.valueOf(isTablet() ? 2 : 1));
        map.put("android_id", mAndroid_id);

        map.put("connect_type", NetUtils.getNetworkClass(mContext) + "");
        map.put("carrier", getImsiCode());

        map.put("carrier", getImsiCode());
//        ADRequestBean.Pos pos = new ADRequestBean.Pos();
//        pos.setId(Long.parseLong(ad_id));// 广告位id
//        pos.setAd_count(1);// 数量
//
//        ADRequestBean.Media media = new ADRequestBean.Media();
//        media.setApp_bundle_id(app_bundle_id);// 包名
//        media.setApp_id(app_id);// 广告联盟平台分配的应用id
//
//        ADRequestBean.Device device = new ADRequestBean.Device();
//        device.setOs(checkNull(os));
//        device.setOs_version(checkNull(mAppEnv));
//        device.setModel(checkNull(mQueryModel));
//        device.setManufacturer(checkNull(mManufacturer));
//        device.setDevice_type(isTablet() ? 2 : 1);
//        device.setImei(mQueryIMEI);
//        device.setAndroid_id(mAndroid_id);
//
//        ADRequestBean.Network network = new ADRequestBean.Network();
//        network.setConnect_type(NetUtils.getNetworkClass(mContext));
//        network.setCarrier(Integer.parseInt(getImsiCode()));
//
//        Gson gson = new Gson();
//
//        HashMap<String, String> params = new HashMap<>();
//        params.put("api_version", "3.0.0");
//        params.put("pos", gson.toJson(pos));
//        params.put("media", gson.toJson(media));
//        params.put("device", gson.toJson(device));
//        params.put("network", gson.toJson(network));
        return map;
    }

    private String checkNull(String value) {
        return value != null ? value : unknown;
    }

    public HashMap<String, String> getBaseParams1() {
        HashMap<String, String> map = new HashMap<>();
        map.put(QUERYIP, getIpAddressString());
        map.put(QUERYIMEI, mQueryIMEI);
        map.put(QUERYMOBILE, mQueryMobile);
        map.put(APPID, mAppID);
        map.put(APPENV, mAppEnv);

        map.put(CLIENTVERSION, mClientVersion);
        map.put(VERSIONSDK, mVersionSDK);
        map.put(VERSIONCODE, mClientVersionCode);

        map.put(ACCOUNTS, mAccounts);
        map.put(SIGNTYPE, mSignType);
        map.put(SIGN, mSign);
        map.put(PAYPROVIDER, mPayProvicer);
        map.put(MOBILETYPE, mQueryMobileType);
        return map;
    }

    public HashMap<String, String> getBaseParams2() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put(QUERYIMEI, mQueryIMEI);
        paramMap.put(QUERYMOBILE, mQueryMobile);
        paramMap.put(APPENV, mAppEnv);
        paramMap.put(BUILDVERSION, mBuildVersion);

        paramMap.put(CLIENTVERSION, mClientVersion);
        paramMap.put(VERSIONCODE, mClientVersionCode);
        paramMap.put(VERSIONSDK, mVersionSDK);

        paramMap.put(USERID, mAccounts);
        paramMap.put(MOBILETYPE, mQueryMobileType);
        paramMap.put(CPUABI, mCpuAbi);
        paramMap.put(HARDWARE, mHardware);
        return paramMap;
    }

    public HashMap<String, String> getServerAddressMap() {
        String latitude = (String) get(LocationUtils.LATITUDE, "22.53");
        String longitude = (String) get(LocationUtils.LONGITUDE, "114.03");
        HashMap<String, String> params = getBaseParams1();
        params.put(LONGITUDE, longitude);
        params.put(LATITUDE, latitude);
        return params;
    }

    public HashMap<String, String> getRecordServerAdressMap() {
        String latitude = (String) get(LocationUtils.LATITUDE, "22.53");
        String longitude = (String) get(LocationUtils.LONGITUDE, "114.03");
        HashMap<String, String> params = getBaseParams1();
        params.put(LONGITUDE, longitude);
        params.put(LATITUDE, latitude);
        params.put(APPID, "BrowserLog");
        return params;
    }

    private String getModelAbb() {
        String model = Build.PRODUCT;
        String result = model.replaceAll("[Dd][Oo][Oo][Vv]", "");
        result = result.replaceAll(" ", "");

        if (TextUtils.isEmpty(result)) {
            return model.replace(" ", "");
        }

        //Log.v("model", "model: " + Build.DEVICE + " display: " + Build.DISPLAY + " FINGERPRINT: " + Build.FINGERPRINT + " product: " + Build.PRODUCT);

        return result;
    }

    public HashMap<String, String> getFeedbackStrParam(String contact, String content, int type) {
        HashMap<String, String> paramMap = getBaseFeedbackMap();

        //检查一下参数
        if (TextUtils.isEmpty(contact)) {
            contact = "13800000000";
        }

        if (TextUtils.isEmpty(content)) {
            content = "no suggets";
        }

        paramMap.put("ContactMobile", contact);
        paramMap.put("Content", content);
        paramMap.put("Type", type + "");

        return paramMap;
    }

    @NonNull
    public HashMap<String, String> getBaseFeedbackMap() {
        HashMap<String, String> paramMap = new HashMap<>();

        paramMap.put("businessType", "2");
        paramMap.put("Machine", getModelAbb());
        paramMap.put("IMEI", mQueryIMEI);
        paramMap.put("OS", getSystemVersionCode());
        paramMap.put("ClientVersion", mClientVersion);
        paramMap.put("AppName", mAppID);
        paramMap.put("AppPackage", mPackageName);

        paramMap.put("AppVersion", mClientVersion);
        paramMap.put("BugMachine", mQueryMobileType);

        paramMap.put(VERSIONSDK, mVersionSDK);
        paramMap.put(VERSIONCODE, mClientVersionCode);
        return paramMap;
    }

    public HashMap<String, String> getBaseRecordParams(int lastPage, int currentPage, int event) {
        HashMap<String, String> params = getBaseParams2();
        params.put("Network", NetUtils.getNetworkClassStr(mContext));
        params.put("Channel", ChannelUtil.getChannel(mContext));
        String province = (String) SPUtils.get(LocationUtils.PROVINCE, "");
        String city = (String) SPUtils.get(LocationUtils.CITY, "");
        params.put("Province", province);
        params.put("City", city);
        params.put("Time", System.currentTimeMillis() + "");

        params.put("LastPage", lastPage + "");
        params.put("CurrentPage", currentPage + "");
        params.put("Event", event + "");

        return params;
    }

}

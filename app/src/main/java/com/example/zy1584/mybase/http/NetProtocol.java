package com.example.zy1584.mybase.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.example.zy1584.mybase.bean.ADRequestBean;
import com.example.zy1584.mybase.utils.NetUtils;
import com.google.gson.Gson;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NetProtocol {
    private static final String TAG = "NetProtocol";
    private static Context mContext = null;

    //基本参数定义成全局变量, 只初始化一次
    private static String mQueryMobile = null;
    private static String mQueryModel = null;
    private static String mQueryIMEI = null;
    private static String mAppEnv = null;
    public static String mClientVersion = null;
    private static String mClientVersionCode = null;

    private static String mBuildVersion = null;
    private static String mVersionCode = null;
    private static String mCpuAbi = null;
    private static String mHardware = null;

    private static String mPackageName = null;
    private static String mImsiCode = null;
    private static String mImsi = null;
    private static String mAndroid_id = null;
    private static String mManufacturer = null;

    private static String app_bundle_id = "com.test.android";
    private static String app_id = "1104241296";
    private static String ad_id = "8050018672826551";
    private static String os = "android";
    private static String unknown = "unknown";

    private final static class HolderClass {
        private final static NetProtocol INSTANCE = new NetProtocol();
    }

    public static NetProtocol getImpl(Context context) {
        mContext = context;
        return HolderClass.INSTANCE;
    }

    public NetProtocol() {
        initGlobalVar();
    }

    /**
     * 初始化静态变量
     */
    private void initGlobalVar() {

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

        if (mAppEnv == null) {
            mAppEnv = getAppENV();
        }

        if (mVersionCode == null) {
            mVersionCode = getAppEnvSdkIntVersion();
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

        if (mImsi == null){
            mImsi = getImsi();
        }

        if (mAndroid_id == null){
            mAndroid_id = getAndroidId();
        }
        if (mManufacturer == null){
            mManufacturer = getManufacturer();
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
        return "";
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
            }
            return imei;
        } else {
            return "000000000000000";
        }
    }

    /**
     * 获取手机型号?获取手机的设备名称?
     * @return
     */
    private String getModel() {
//        return Build.DEVICE.replace(" ", "_");
        return Build.MODEL;
    }

    /**
     * 获取电话号码
     * @return
     */
    public String getPhoneNumber() {
        String nb = null;
        if (mContext != null) {
            try{
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                nb =  tm.getLine1Number();
                if (nb == null) {
                    return nb = "00000000000";
                }else if(nb.equals("") || nb.length() < 11) {
                    return nb = "00000000000";
                }else if(nb.length() > 11) {
                    nb = nb.substring(nb.length()-11);
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
     * 获取系统android版本号
     */
    private String getAppENV() {
        return "system:android,version:" + android.os.Build.VERSION.RELEASE;
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
     * @return
     */
    private String getCpuAbi() {
        String cpuabistr = null;
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            String[] cpuLst = android.os.Build.SUPPORTED_ABIS;
            for(int i = 0; i < cpuLst.length; i++) {
                if(cpuabistr != null) {
                    cpuabistr += ("," + cpuLst[i]);
                }else {
                    cpuabistr = cpuLst[i];
                }
            }
        }else if(android.os.Build.VERSION.SDK_INT >= 8) {
            cpuabistr = android.os.Build.CPU_ABI + "," + android.os.Build.CPU_ABI2;
        }else {
            cpuabistr = android.os.Build.CPU_ABI;
        }

        if(cpuabistr == null) {
            cpuabistr = "unknown";
        }

        return cpuabistr;
    }

    /**
     * 获取cpu型号
     * @return
     */
    private String  getHardware() {
        return android.os.Build.HARDWARE;
    }

    /**
     * 获取APP版本信息
     */
    public static void getAPPVersion(Context con) {
        try {
            PackageInfo pkgInfo =  con.getPackageManager().getPackageInfo(con.getPackageName(), 0);
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
     * @return
     */
    private String getImsiCode() {
        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();
        if(imsi!=null){
            if(imsi.startsWith("46000") ||imsi.startsWith("46002")){
                //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                //中国移动
                return "1";
            }else if(imsi.startsWith("46001")){
                //中国联通
                return "2";
            }else if(imsi.startsWith("46003")){
                //中国电信
                return "3";
            }
        }else {
            return "0";
        }

        return "0";
    }

    public String getImsi() {
        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();
        return imsi;
    }

    public String getAndroidId() {
        String ANDROID_ID = Settings.System.getString(mContext.getContentResolver(), Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public HashMap<String, String> getRecommendNewsQueryMap(){
        HashMap<String, String> params = new HashMap<>();
        params.put("devid", mQueryIMEI);
        // TODO: 2017-7-25
        params.put("refer", "openapi_for_rcmduowei");
        params.put("appkey", "156847d34334b1cb5acd8ec8b742e40e");
        return params;
    }

    public HashMap<String, String> getChannelListQueryMap(){
        HashMap<String, String> params = new HashMap<>();
        // TODO: 2017-7-25
        params.put("refer", "openapi_for_rcmduowei");
        params.put("app_key", "156847d34334b1cb5acd8ec8b742e40e");
        return params;
    }

    public HashMap<String, String> getChannelNewsQueryMap(int start, int size, String channelCode){
        HashMap<String, String> params = new HashMap<>();
        params.put("refer", "openapi_for_rcmduowei");
        params.put("appkey", "156847d34334b1cb5acd8ec8b742e40e");
        params.put("channel_code", channelCode);
        params.put("start", start +"");
        params.put("size", size + "");
        return params;
    }

    public HashMap<String, String> getReportActionMap(String article_id, String chlid, String action_type){
        HashMap<String, String> params = new HashMap<>();
        params.put("refer", "openapi_for_rcmduowei");
        params.put("appkey", "156847d34334b1cb5acd8ec8b742e40e");
        params.put("article_id", article_id);
        params.put("chlid", chlid);
        params.put("imei", mQueryIMEI);
        params.put("imsi", mImsi);
        params.put("action_type", action_type);
        params.put("devinfo", mQueryIMEI);// TODO: 2017-7-27
        return params;
    }

    public HashMap<String, String> getADMap(){
        ADRequestBean.Pos pos = new ADRequestBean.Pos();
        pos.setId(Long.parseLong(ad_id));// 广告位id
        pos.setAd_count(1);// 数量

        ADRequestBean.Media media = new ADRequestBean.Media();
        media.setApp_bundle_id(app_bundle_id);
        media.setApp_id(app_id);// 广告联盟平台分配的应用id

        ADRequestBean.Device device = new ADRequestBean.Device();
        device.setOs(checkNull(os));
        device.setOs_version(checkNull(mAppEnv));
        device.setModel(checkNull(mQueryModel));
        device.setManufacturer(checkNull(mManufacturer));
        device.setDevice_type(1);// TODO: 2017-7-27
        device.setImei(mQueryIMEI);
        device.setAndroid_id(mAndroid_id);

        ADRequestBean.Network network = new ADRequestBean.Network();
        network.setConnect_type(NetUtils.getNetworkClass(mContext));
        network.setCarrier(Integer.parseInt(getImsiCode()));

        Gson gson = new Gson();

        HashMap<String, String> params = new HashMap<>();
        params.put("api_version", "3.0.0");
        params.put("pos", gson.toJson(pos));
        params.put("media", gson.toJson(media));
        params.put("device", gson.toJson(device));
        params.put("network", gson.toJson(network));
        return params;
    }

    private String checkNull(String value){
        return value != null ? value : unknown;
    }

}

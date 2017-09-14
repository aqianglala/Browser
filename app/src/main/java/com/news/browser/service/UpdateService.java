package com.news.browser.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.news.browser.bean.UpgradeBean;
import com.news.browser.http.Http;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.download.DownloadManagerActivity.TasksManager;
import com.news.browser.ui.download.db.FileItem;
import com.news.browser.utils.ForegroundCallbacks;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.NetUtils;
import com.news.browser.utils.PackageExcuteTool;
import com.news.browser.utils.SPUtils;
import com.news.browser.utils.UIUtils;
import com.news.browser.utils.Utils;

import java.io.File;
import java.util.HashMap;

import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by zy1584 on 2017-8-8.
 */

public class UpdateService extends Service {

    public static final String DOWNLOADED_FORCE = "force";
    private static final String APPSTORE_DOWNLOADED_FILE_PATH = "downloaded_file_path";
    private static final String APPSTORE_DOWNLOADED_VERSION = "downloaded_version";

    public static final int APP_DOWNLOAD_STATUS_DEFAULT = 0;
    public static final int APP_DOWNLOAD_STATUS_CHECKING = 1;
    public static final int APP_DOWNLOAD_STATUS_DOWNLOADING = 2;
    public static final int APP_DOWNLOAD_STATUS_DOWNLOADED = 3;
    public static final int APP_DOWNLOAD_STATUS_INSTALLING = 4;
    public static final int APP_DOWNLOAD_STATUS_FAIL = 5;

    private static int mDownloadStatus = APP_DOWNLOAD_STATUS_DEFAULT;
    private static Context mContext = null;
    private static IFileDownloadInfoReturn mFileDownloadInfoInterface = null;
    private static IAppStoreUpdateReturn mAppStoreUpdateInterface = null;
    private static IDetailAppListener mDetailAppInterface = null;
    private static String mDownloadDir;
    private static int mAppVersion = 0;
    private static boolean mIsAutoInstall = true;

    public static int taskId;
    public static boolean hasNewVersion;
    private static UpgradeBean mUpgradeBean;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null != intent) {
            boolean isForce = intent.getBooleanExtra(DOWNLOADED_FORCE, false);

            if (isForce)// 打开app时检测
            {
                checkAppStoreVersion();
            } else if (null == mAppStoreUpdateInterface && ForegroundCallbacks.get().isBackground())// 后台升级
            {
                checkAppStoreVersion();
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    public static void setCheckVersionInsterface(IFileDownloadInfoReturn fileInterface,
                                                 IDetailAppListener detailInterface,
                                                 IAppStoreUpdateReturn appStoreUpdateInterface) {
        if (APP_DOWNLOAD_STATUS_DOWNLOADING != mDownloadStatus) {
            mFileDownloadInfoInterface = fileInterface;
        } else {
            mFileDownloadInfoInterface = null;
        }

        mDetailAppInterface = detailInterface;
        mAppStoreUpdateInterface = appStoreUpdateInterface;
        if (taskId != 0){
            TasksManager.getImpl().addTaskListener(taskId, taskDownloadListener);
        }

    }

    public static void resetListener() {
        mFileDownloadInfoInterface = null;
        mDetailAppInterface = null;
        mAppStoreUpdateInterface = null;
        if (taskId != 0){
            TasksManager.getImpl().removeTaskListener(taskId);
        }
    }

    public static void resetFileDownloadInterface()
    {
        mFileDownloadInfoInterface = null;
    }

    public static void setFileDownloadInfoListener(IFileDownloadInfoReturn fileInterface)
    {
        mFileDownloadInfoInterface = fileInterface;
    }

    public void checkAppStoreVersion() {
        // 不是下载中，也不是检查更新中
        if (APP_DOWNLOAD_STATUS_DOWNLOADING != mDownloadStatus && APP_DOWNLOAD_STATUS_CHECKING != mDownloadStatus) {
            mDownloadStatus = APP_DOWNLOAD_STATUS_CHECKING;
            checkUpdateInfo();
        } else if (null != mAppStoreUpdateInterface && null == mFileDownloadInfoInterface) {// 下载中进入前台
            mAppStoreUpdateInterface.receiveAppStoreUpdateInfo(mDownloadStatus);
        }
    }

    private void checkUpdateInfo() {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseParams2();
        String url = Utils.getUrl(GlobalParams.UPGRADE_INFO);
        Subscription subscribe = Http.getHttpService().checkUpdateInfo(url, params)
                .compose(new ScheduleTransformer<UpgradeBean>())
                .subscribe(new Subscriber<UpgradeBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDownloadStatus = APP_DOWNLOAD_STATUS_DEFAULT;
                    }

                    @Override
                    public void onNext(UpgradeBean upgradeBean) {
                        if (APP_DOWNLOAD_STATUS_DOWNLOADING == mDownloadStatus || upgradeBean == null) {
                            return;
                        }
                        mIsAutoInstall = true;
                        mDownloadStatus = APP_DOWNLOAD_STATUS_DEFAULT;

                        if (upgradeBean.getRet() != 0) return;
                        mUpgradeBean = upgradeBean;
                        try {
                            PackageInfo packageInfo = getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                            int versionCode = packageInfo.versionCode;
                            int newVersionCode = upgradeBean.getData().get(0).getVersionCode();

                            if (newVersionCode > versionCode) {
                                if (isDownloaded(newVersionCode)) {// 已下载
                                    if (null != mAppStoreUpdateInterface)// 前台启动升级任务
                                    {
                                        mAppStoreUpdateInterface.receiveAppStoreUpdateInfo(mDownloadStatus);
                                    } else// 后台
                                    {
//                                        notificationAppstoreUpdateInstall();
                                    }
                                } else if (null != mDetailAppInterface) {// 未下载，且是前台
                                    mDetailAppInterface.receiveDetailAppInfo(true, upgradeBean);
                                } else {// 后台
                                    mIsAutoInstall = false;
                                    startDownloadAppStore(upgradeBean);
                                    if (null != mAppStoreUpdateInterface && null == mFileDownloadInfoInterface)// 前台，且是下载中
                                    {
                                        mAppStoreUpdateInterface.receiveAppStoreUpdateInfo(mDownloadStatus);
                                    }
                                }
                            } else if (null != mDetailAppInterface)// 无更新
                            {
                                mDetailAppInterface.receiveDetailAppInfo(false, upgradeBean);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
        addSubscription(subscribe);
    }

    public static void startDownloadAppStore(UpgradeBean upgradeBean) {
        if (APP_DOWNLOAD_STATUS_DOWNLOADING != mDownloadStatus) {
            // 判断网络限制
            int limitNet = upgradeBean.getData().get(0).getLimitNet();
            if (ForegroundCallbacks.get().isBackground() && limitNet == 1){// 在后台，且有网络限制，则当前如果是数据网络，则不进行下载
                boolean connected = NetUtils.isConnected(mContext);
                boolean wifi = NetUtils.isWifi(mContext);
                if (connected && !wifi){
                    return;
                }
            }
            UpgradeBean.DataBean info = upgradeBean.getData().get(0);
            String filename = URLUtil.guessFileName(info.getDownloadUrl(), null, null);
            FileItem item = TasksManager.getImpl().addTask(info.getDownloadUrl(), filename);
            if (item != null){
                taskId = item.getId();
                mAppVersion = info.getVersionCode();
                mDownloadDir = item.getPath();
                BaseDownloadTask task = TasksManager.getImpl().getTaskById(item.getId());
                mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
                if (task != null){// 下载中
                    task.setListener(taskDownloadListener);
                }else{// 任务已存在
                    task = FileDownloader.getImpl().create(item.getUrl())
                            .setPath(item.getPath())
                            .setCallbackProgressTimes(100)
                            .setAutoRetryTimes(5)
                            .setListener(taskDownloadListener);
                    task.start();
                }
            }else {
                if (!TextUtils.isEmpty(info.getDownloadUrl())){// 文件已存在
                    String path = TasksManager.getImpl().createPath(info.getDownloadUrl(), filename);
                    int version = Utils.getApkVersion(mContext, path);
                    if (version != -1 && version >= mAppVersion){// 是新版本包
                        mAppVersion = version;
                        mDownloadDir = path;
                        setDownloaded();
                        installApp();
                    }else{
                        mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
                        BaseDownloadTask task = FileDownloader.getImpl().create(item.getUrl())
                                .setPath(item.getPath())
                                .setCallbackProgressTimes(100)
                                .setAutoRetryTimes(5)
                                .setForceReDownload(true)
                                .setListener(taskDownloadListener);
                        task.start();
                    }
                }
            }
        }
    }

    public static boolean isDownloading()
    {
        return (mDownloadStatus == APP_DOWNLOAD_STATUS_DOWNLOADING);
    }

    public static boolean isDownloaded(int checkVersion) {
        int version = (int) SPUtils.get(APPSTORE_DOWNLOADED_VERSION, 0);
        String filePath = (String) SPUtils.get(APPSTORE_DOWNLOADED_FILE_PATH, "");

        if (checkVersion == version) {
            if (checkDownloadFile(filePath)) {
                mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADED;
                if (TextUtils.isEmpty(mDownloadDir)) {
                    mDownloadDir = filePath;
                }
                return true;
            }
        }

        return false;
    }

    private static void setDownloaded() {
        mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADED;
        SPUtils.put(APPSTORE_DOWNLOADED_VERSION, mAppVersion);
        SPUtils.put(APPSTORE_DOWNLOADED_FILE_PATH, mDownloadDir);
    }

    private static boolean checkDownloadFile(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            return true;
        }
        return false;
    }

    public static boolean normalInstall(Context context)
    {
        return PackageExcuteTool.normalInstall(mDownloadDir, context);
    }

    public static int getAppStoreStatus() {
        if (APP_DOWNLOAD_STATUS_DOWNLOADED == mDownloadStatus) {
            checkDownloadFile();
        }

        return mDownloadStatus;
    }

    public static UpgradeBean getUpgradeBean()
    {
        return mUpgradeBean;
    }

    public static boolean checkDownloadFile() {
        String filePath = (String) SPUtils.get(APPSTORE_DOWNLOADED_FILE_PATH, "");

        if (checkDownloadFile(filePath)) {
            mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADED;
            if (TextUtils.isEmpty(mDownloadDir)) {
                mDownloadDir = filePath;
            }
            return true;
        }

        mDownloadStatus = APP_DOWNLOAD_STATUS_DEFAULT;
        return false;
    }

    public static void installApp() {
        //Utils.clearSharedPreference(UpdateService.this);
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean installResult = PackageExcuteTool.installApp(mDownloadDir);
                if (!installResult) {// 无法静默安装
                    if (null != mAppStoreUpdateInterface) {// 前台
                        UIUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyStatusChange();
                            }
                        });
                        if (mIsAutoInstall) {
                            PackageExcuteTool.normalInstall(mDownloadDir, mContext);
                        }
                    } else {// 后台
//                        notificationAppstoreUpdateInstall();
                    }
                }
            }
        }).start();
    }

    private CompositeSubscription mCompositeSubscription;

    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }
        this.mCompositeSubscription.add(s);
    }

    public void unsubscribe() {
        if (this.mCompositeSubscription != null) {
            this.mCompositeSubscription.unsubscribe();
        }
    }

    public interface IAppStoreUpdateReturn {
        void receiveAppStoreUpdateInfo(int status);
    }

    public interface IDetailAppListener {
        void receiveDetailAppInfo(boolean isNewVersion, UpgradeBean bean);
    }

    //文件下载的监听器
    public interface IFileDownloadInfoReturn {
        //文件下载的信息通知函数
        void receiveFileDownloadInfo(BaseDownloadTask task, int soFarBytes, int totalBytes);
    }

    public static void cancelDownloadAppStore() {
        FileDownloader.getImpl().pause(taskId);
    }

    private static FileDownloadListener taskDownloadListener = new FileDownloadSampleListener() {
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
            notifyDownloadInfo(task, soFarBytes, totalBytes);
            notifyStatusChange();
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
            notifyDownloadInfo(task, 0, 0);
            notifyStatusChange();
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);
            mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
            notifyDownloadInfo(task, soFarBytes, totalBytes);
            notifyStatusChange();
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
            notifyDownloadInfo(task, soFarBytes, totalBytes);
            notifyStatusChange();
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            mDownloadStatus = APP_DOWNLOAD_STATUS_FAIL;
            notifyDownloadInfo(task, 0, 0);
            notifyStatusChange();
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
            mDownloadStatus = APP_DOWNLOAD_STATUS_DEFAULT;
            notifyDownloadInfo(task, soFarBytes, totalBytes);
            notifyStatusChange();
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            mDownloadStatus = APP_DOWNLOAD_STATUS_INSTALLING;
            notifyDownloadInfo(task, 0, 0);
            notifyStatusChange();
            // TODO: 2017-8-9 这里如果是预装的话，在安装过程中如果跳转到其他页面又回来会显示安装和忽略
            setDownloaded();
            installApp();
        }

    };

    /**
     * 前台下载
     * @param task
     * @param soFarBytes
     * @param totalBytes
     */
    private static void notifyDownloadInfo(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        if (null != mFileDownloadInfoInterface) {
            mFileDownloadInfoInterface.receiveFileDownloadInfo(task, soFarBytes, totalBytes);
        }
    }

    /**
     * 后台下载，进入前台
     */
    private static void notifyStatusChange() {
        if (null != mAppStoreUpdateInterface && null == mFileDownloadInfoInterface) {
            mAppStoreUpdateInterface.receiveAppStoreUpdateInfo(mDownloadStatus);
        }
    }

    public static void updateStatus(BaseDownloadTask task){
        if (task == null) return;
        if (task.getId() == taskId){
            byte status = task.getStatus();
            if (status == FileDownloadStatus.pending
                    || status == FileDownloadStatus.started
                    || status == FileDownloadStatus.connected
                    || status == FileDownloadStatus.progress){
                mDownloadStatus = APP_DOWNLOAD_STATUS_DOWNLOADING;
            }else if (status == FileDownloadStatus.error){
                mDownloadStatus = APP_DOWNLOAD_STATUS_FAIL;
            }else if (status == FileDownloadStatus.paused){
                mDownloadStatus = APP_DOWNLOAD_STATUS_DEFAULT;
            }else if (status == FileDownloadStatus.completed){
                mDownloadStatus = APP_DOWNLOAD_STATUS_INSTALLING;
                setDownloaded();
            }
        }
    }

}

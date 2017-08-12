package com.news.browser.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.news.browser.ui.download.GlobalMonitor;
import com.news.browser.utils.ForegroundCallbacks;
import com.news.browser.utils.LocationUtils;
import com.news.browser.receiver.PackageChangeReceiver;
import com.news.browser.service.UpdateService;
import com.news.browser.ui.main.db.BookmarkManager;
import com.liulishuo.filedownloader.FileDownloadMonitor;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.net.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.dreamtobe.threaddebugger.IThreadDebugger;
import cn.dreamtobe.threaddebugger.ThreadDebugger;
import cn.dreamtobe.threaddebugger.ThreadDebuggers;

public class BaseApplication extends Application {

    private static Context context;
    private static Thread mainThread;
    private static long mainThreadId;
    private static Handler mainHandler;
    private static Looper mainlooper;
    private PackageChangeReceiver mPackageReceiver = null;
    private String TAG = "base_tag";
    private final static String TAG_DOWNLOAD = "FileDownloadApplication";

    private static final Executor mIOThread = Executors.newSingleThreadExecutor();
    private static final Executor mTaskThread = Executors.newCachedThreadPool();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        new LocationUtils(context).getLocation();

        getMainThreadData();
        ForegroundCallbacks.init(this);
//        CrashHandler.getInstance().init(getApplicationContext());

//        LeakCanary.install(this);
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return true;
            }
        });
        initFileDownloader();

        //设置包安装，卸载的监听器
        registerPackageChangeReceivier();

        BookmarkManager.getInstance();

    }

    private void initFileDownloader() {
        // just for open the log in this demo project.
        FileDownloadLog.NEED_LOG = true;
        /**
         * just for cache Application's Context, and ':filedownloader' progress will NOT be launched
         * by below code, so please do not worry about performance.
         * @see FileDownloader#init(Context)
         */
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                        .proxy(Proxy.NO_PROXY) // set proxy
                ))
                .commit();
        FileDownloadMonitor.setGlobalMonitor(GlobalMonitor.getImpl());

        // below codes just for monitoring thread pools in the FileDownloader:
        IThreadDebugger debugger = ThreadDebugger.install(
                ThreadDebuggers.create() /** The ThreadDebugger with known thread Categories **/
                        // add Thread Category
                        .add("OkHttp").add("okio").add("Binder")
                        .add(FileDownloadUtils.getThreadPoolName("Network"), "Network")
                        .add(FileDownloadUtils.getThreadPoolName("Flow"), "FlowSingle")
                        .add(FileDownloadUtils.getThreadPoolName("EventPool"), "Event")
                        .add(FileDownloadUtils.getThreadPoolName("LauncherTask"), "LauncherTask")
                        .add(FileDownloadUtils.getThreadPoolName("BlockCompleted"), "BlockCompleted"),

                2000, /** The frequent of Updating Thread Activity information **/

                new ThreadDebugger.ThreadChangedCallback() {
                    /**
                     * The threads changed callback
                     **/
                    @Override
                    public void onChanged(IThreadDebugger debugger) {
                        // callback this method when the threads in this application has changed.
                        Log.d(TAG_DOWNLOAD, debugger.drawUpEachThreadInfoDiff());
                        Log.d(TAG_DOWNLOAD, debugger.drawUpEachThreadSizeDiff());
                        Log.d(TAG_DOWNLOAD, debugger.drawUpEachThreadSize());
                    }
                });
    }

    private void registerPackageChangeReceivier() {
        mPackageReceiver = new PackageChangeReceiver();
        IntentFilter InF = new IntentFilter();
        InF.addAction(Intent.ACTION_PACKAGE_ADDED);
        InF.addAction(Intent.ACTION_PACKAGE_REMOVED);
        InF.addAction(Intent.ACTION_PACKAGE_REPLACED);
        InF.addDataScheme("package");
        registerReceiver(mPackageReceiver, InF);
    }

    private void getMainThreadData() {
        mainThread = Thread.currentThread();//获取主线程

        mainThreadId = android.os.Process.myTid();//获取当前的线程id

        mainHandler = new Handler(); //在主线程初始化一个全局的handler。

        mainlooper = getMainLooper();// 获取主线程的looper
    }

    public static Context getContext() {
        return context;
    }

    public static Thread getMainThread() {
        return mainThread;
    }

    public static long getMainThreadId() {
        return mainThreadId;
    }

    public static Handler getMainHandler() {
        return mainHandler;
    }

    public static Looper getMainlooper() {
        return mainlooper;
    }

    @NonNull
    public static Executor getIOThread() {
        return mIOThread;
    }

    @NonNull
    public static Executor getTaskThread() {
        return mTaskThread;
    }

    @NonNull
    public static BaseApplication get(@NonNull Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

    public static void startCheckAppStoreUpdate(Context context, boolean isForce)
    {
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra(UpdateService.DOWNLOADED_FORCE, isForce);
        context.startService(intent);
    }

}

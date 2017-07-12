package com.example.zy1584.mybase.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.zy1584.mybase.utils.CrashHandler;
import com.example.zy1584.mybase.utils.ForegroundCallbacks;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.BuildConfig;
import com.orhanobut.logger.Logger;

public class BaseApplication extends Application {

    private static Context context;
    private static Thread mainThread;
    private static long mainThreadId;
    private static Handler mainHandler;
    private static Looper mainlooper;
    private String TAG = "base_tag";

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        getMainThreadData();
        ForegroundCallbacks.init(this);
        CrashHandler.getInstance().init(getApplicationContext());

//        LeakCanary.install(this);
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return BuildConfig.DEBUG;
            }
        });
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

}

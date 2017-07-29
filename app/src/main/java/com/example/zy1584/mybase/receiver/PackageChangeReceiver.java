package com.example.zy1584.mybase.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.zy1584.mybase.http.Http;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.download.DownloadManagerActivity.TasksManager;
import com.example.zy1584.mybase.ui.download.db.FileItem;
import com.example.zy1584.mybase.utils.Utils;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.util.List;

import okhttp3.ResponseBody;
import rx.functions.Action1;

import static com.example.zy1584.mybase.ui.download.GlobalMonitor.ACTION_ID_INSTALL_COMPLETE;

/**
 * Created by zy1584 on 2017-7-28.
 */

public class PackageChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            String suffix = new String("package:");
            packageName = packageName.substring(suffix.length());
            System.out.println("安装了:" +packageName + "包名的程序");

            List<FileItem> apkTasks = TasksManager.getImpl().getApkTasks();
            for (FileItem item: apkTasks){
                String pkgName = Utils.getPackageName(context, item.getPath());
                if (packageName.equals(pkgName)){
                    int status = TasksManager.getImpl().getStatus(item.getId(), item.getPath());
                    if (status == FileDownloadStatus.completed){
                        // 上报安装完成
                        reportConversion(item, ACTION_ID_INSTALL_COMPLETE);
                        break;
                    }
                }
            }
        }
        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            System.out.println("卸载了:"  + packageName + "包名的程序");

        }
    }

    private void reportConversion(FileItem fileItem, int action_id ) {
        if (fileItem != null){
            String clickId = fileItem.getClickId();
            String conversionLink = fileItem.getConversionLink();
            String url = conversionLink.replace("__ACTION_ID__", action_id + "")
                    .replace("__CLICK_ID__", clickId);
            Http.getHttpService().reportConversion(url)
                    .compose(new ScheduleTransformer<ResponseBody>())
                    .subscribe(new Action1<ResponseBody>() {
                        @Override
                        public void call(ResponseBody responseBody) {

                        }
                    });
            //todo 是否需要管理rx生命周期
        }
    }
}

package com.news.browser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.news.browser.http.Http;
import com.news.browser.ui.download.GlobalMonitor;
import com.news.browser.ui.download.db.FileItem;
import com.news.browser.utils.Utils;
import com.news.browser.ui.download.DownloadManagerActivity.TasksManager;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.util.List;

import rx.schedulers.Schedulers;

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
                        reportConversion(item, GlobalMonitor.ACTION_ID_INSTALL_COMPLETE);
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
                        .subscribeOn(Schedulers.io());
//                    .compose(new ScheduleTransformer<ResponseBody>())
//                    .subscribe(new Action1<ResponseBody>() {
//                        @Override
//                        public void call(ResponseBody responseBody) {
//                            if (responseBody != null) {
//                                try {
//                                    JSONObject jsonObject = new JSONObject(responseBody.string());
//                                    int ret = jsonObject.optInt("ret");
//                                    if (ret == 0) {
//                                        Logger.d("转化上报成功");
//                                    } else {
//                                        String msg = jsonObject.optString("msg");
//                                        Logger.d("转化上报失败，msg： " + msg);
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    });
        }
    }

}

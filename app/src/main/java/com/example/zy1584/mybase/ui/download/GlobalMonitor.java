/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.zy1584.mybase.ui.download;

import android.text.TextUtils;
import android.util.Log;

import com.example.zy1584.mybase.base.BaseApplication;
import com.example.zy1584.mybase.http.Http;
import com.example.zy1584.mybase.service.UpdateService;
import com.example.zy1584.mybase.ui.download.DownloadManagerActivity.TasksManager;
import com.example.zy1584.mybase.ui.download.db.FileItem;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadMonitor;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import rx.schedulers.Schedulers;


/**
 * Created by Jacksgong on 1/19/16.
 */
public class GlobalMonitor implements FileDownloadMonitor.IMonitor {
    private volatile int markStart;
    private volatile int markOver;

    public static final int ACTION_ID_DOWNLOAD_BEGIN = 5;
    public static final int ACTION_ID_INSTALL_COMPLETE = 6;
    public static final int ACTION_ID_DOWNLOAD_COMPLETE = 7;

    private final static class HolderClass {
        private final static GlobalMonitor INSTANCE = new GlobalMonitor();
    }

    public static GlobalMonitor getImpl() {
        return HolderClass.INSTANCE;
    }

    private final static String TAG = "GlobalMonitor";

    @Override
    public void onRequestStart(int count, boolean serial, FileDownloadListener lis) {
        markStart = 0;
        markOver = 0;
        Log.d(TAG, String.format("on request start %d %B", count, serial));
    }

    @Override
    public void onRequestStart(BaseDownloadTask task) {
    }

    @Override
    public void onTaskBegin(BaseDownloadTask task) {
        markStart++;
    }

    @Override
    public void onTaskStarted(BaseDownloadTask task) {
        reportConversion(task, ACTION_ID_DOWNLOAD_BEGIN);
        UpdateService.updateStatus(task);
    }

    private void reportConversion(BaseDownloadTask task, int action_id ) {
        FileItem fileItem = TasksManager.getImpl().getById(task.getId());
        if (fileItem != null){
            String clickId = fileItem.getClickId();
            String conversionLink = fileItem.getConversionLink();
            if (TextUtils.isEmpty(clickId) || TextUtils.isEmpty(conversionLink)) return;
            String url = conversionLink.replace("__ACTION_ID__", action_id + "")
                    .replace("__CLICK_ID__", clickId);
            Http.getHttpService().reportConversion(url)
                    .subscribeOn(Schedulers.io());
//                    .compose(new ScheduleTransformer<ResponseBody>())
//                    .subscribe(new Action1<ResponseBody>() {
//                        @Override
//                        public void call(ResponseBody responseBody) {
//                            if (responseBody != null){
//                                try {
//                                    JSONObject jsonObject = new JSONObject(responseBody.string());
//                                    int ret = jsonObject.optInt("ret");
//                                    if (ret == 0){
//                                        Logger.d("转化上报成功");
//                                    }else{
//                                        String msg = jsonObject.optString("msg");
//                                        Logger.d("转化上报失败，msg： "+ msg);
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

    @Override
    public void onTaskOver(BaseDownloadTask task) {
        markOver++;
        int status = TasksManager.getImpl().getStatus(task.getId(), task.getPath());
        if (status == FileDownloadStatus.completed){
            reportConversion(task, ACTION_ID_DOWNLOAD_COMPLETE);
            TasksManager.getImpl().updateModelSize(task.getId(), task.getSmallFileTotalBytes());
            boolean isApk = TasksManager.getImpl().isApk(task);
            if (isApk){
                TasksManager.getImpl().normalInstall(task.getPath(), BaseApplication.getContext());
            }
        }
        UpdateService.updateStatus(task);
    }

    public int getMarkStart() {
        return markStart;
    }

    public int getMarkOver() {
        return markOver;
    }

}

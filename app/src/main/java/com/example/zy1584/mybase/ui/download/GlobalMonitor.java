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

import com.example.zy1584.mybase.http.Http;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.download.DownloadManagerActivity.TasksManager;
import com.example.zy1584.mybase.ui.download.db.FileItem;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadMonitor;

import okhttp3.ResponseBody;
import rx.functions.Action1;


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
                    .compose(new ScheduleTransformer<ResponseBody>())
                    .subscribe(new Action1<ResponseBody>() {
                        @Override
                        public void call(ResponseBody responseBody) {

                        }
                    });
            //todo 是否需要管理rx生命周期
        }
    }

    @Override
    public void onTaskOver(BaseDownloadTask task) {
        markOver++;
        reportConversion(task, ACTION_ID_DOWNLOAD_COMPLETE);
    }

    public int getMarkStart() {
        return markStart;
    }

    public int getMarkOver() {
        return markOver;
    }
}

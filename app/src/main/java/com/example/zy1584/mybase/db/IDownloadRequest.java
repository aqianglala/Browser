package com.example.zy1584.mybase.db;

/**
 * Created by zy1584 on 2017-7-19.
 */

public interface IDownloadRequest {

    boolean startDownload(FileItem item);

    public boolean pauseDownload(FileItem item);
}

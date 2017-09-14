/*
 * Copyright 2014 A.C.R. Development
 */
package com.news.browser.ui.download;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.news.browser.BuildConfig;
import com.news.browser.R;
import com.news.browser.bus.RXEvent;
import com.news.browser.dialog.BrowserDialog;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.utils.RxBus;
import com.news.browser.utils.Utils;

import java.io.File;
import java.io.IOException;

import rx.functions.Action1;


/**
 * Handle download requests
 */
public class DownloadHandler {

    private static final String TAG = DownloadHandler.class.getSimpleName();
    private static final String COOKIE_REQUEST_HEADER = "Cookie";

    public static final String DEFAULT_DOWNLOAD_PATH =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .getPath();


    /**
     * Notify the host application a download should be done, or that the data
     * should be streamed if a streaming viewer is available.
     *
     * @param context            The context in which the download was requested.
     * @param url                The full url to the content that should be downloaded
     * @param userAgent          User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype           The mimetype of the content reported by the server
     */
    public static void onDownloadStart(@NonNull Context context, @NonNull PreferenceManager manager, String url, String userAgent,
                                       @Nullable String contentDisposition, String mimetype) {
        onDownloadStart(context, manager, url, userAgent, contentDisposition, mimetype, null, null);
    }

    /**
     * Notify the host application a download should be done, or that the data
     * should be streamed if a streaming viewer is available.
     *
     * @param context            The context in which the download was requested.
     * @param url                The full url to the content that should be downloaded
     * @param userAgent          User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype           The mimetype of the content reported by the server
     */
    public static void onDownloadStart(@NonNull Context context, @NonNull PreferenceManager manager, String url, String userAgent,
                                       @Nullable String contentDisposition, String mimetype,
                                       String click_id, String conversion_link) {
        // if we're dealing wih A/V content that's not explicitly marked
        // for download, check if it's streamable.
        if (contentDisposition == null
                || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10)) {
            // query the package manager to see if there's a registered handler
            // that matches.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), mimetype);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                intent.setSelector(null);
            }
            ResolveInfo info = context.getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                // If we resolved to ourselves, we don't want to attempt to
                // load the url only to try and download it again.
                if (BuildConfig.APPLICATION_ID.equals(info.activityInfo.packageName)
                        || BrowserActivity.class.getName().equals(info.activityInfo.name)) {
                    // someone (other than us) knows how to handle this mime
                    // type with this scheme, don't download.
                    try {
                        context.startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException ex) {
                        // Best behavior is to fall ic_back_white to a download in this
                        // case
                    }
                }
            }
        }
        // 广告联盟
        onDownloadStartNoStream(context, manager, url, userAgent, contentDisposition, mimetype, click_id, conversion_link);
    }

    // This is to work around the fact that java.net.URI throws Exceptions
    // instead of just encoding URL's properly
    // Helper method for onDownloadStartNoStream
    @NonNull
    private static String encodePath(@NonNull String path) {
        char[] chars = path.toCharArray();

        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return path;
        }

        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * Notify the host application a download should be done, even if there is a
     * streaming viewer available for thise type.
     *
     * @param context            The context in which the download is requested.
     * @param url                The full url to the content that should be downloaded
     * @param userAgent          User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype           The mimetype of the content reported by the server
     */
    /* package */
    private static void onDownloadStartNoStream(@NonNull final Context context, @NonNull PreferenceManager preferences,
                                                String url, String userAgent,
                                                String contentDisposition, @Nullable String mimetype) {
        onDownloadStartNoStream(context, preferences, url, userAgent, contentDisposition, mimetype, null, null);
    }

    /**
     * Notify the host application a download should be done, even if there is a
     * streaming viewer available for thise type.
     *
     * @param context            The context in which the download is requested.
     * @param url                The full url to the content that should be downloaded
     * @param userAgent          User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype           The mimetype of the content reported by the server
     */
    /* package */
    private static void onDownloadStartNoStream(@NonNull final Context context, @NonNull PreferenceManager preferences,
                                                final String url, String userAgent,
                                                String contentDisposition, String mimetype,
                                                final String click_id, final String conversion_link) {
        final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);

        // Check to see if we have an SDCard
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            int title;
            String msg;

            // Check to see if the SDCard is busy, same as the music app
            if (status.equals(Environment.MEDIA_SHARED)) {
                msg = context.getString(R.string.download_sdcard_busy_dlg_msg);
                title = R.string.download_sdcard_busy_dlg_title;
            } else {
                msg = context.getString(R.string.download_no_sdcard_dlg_msg);
                title = R.string.download_no_sdcard_dlg_title;
            }

            Dialog dialog = new AlertDialog.Builder(context).setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert).setMessage(msg)
                    .setPositiveButton(R.string.action_ok, null).show();
            BrowserDialog.setDialogSize(context, dialog);
            return;
        }

        // java.net.URI is a lot stricter than KURL so we have to encode some
        // extra characters. Fix for b 2538060 and b 1634719
        WebAddress webAddress;
        try {
            webAddress = new WebAddress(url);
            webAddress.setPath(encodePath(webAddress.getPath()));
        } catch (Exception e) {
            // This only happens for very bad urls, we want to catch the
            // exception here
            Log.e(TAG, "Exception while trying to parse url '" + url + '\'', e);
            RxBus.getInstance().post(new RXEvent(RXEvent.TAG_BROWSER_MSG, R.string.problem_download));
            return;
        }

        String location = preferences.getDownloadDirectory();
        Uri downloadFolder;
        location = addNecessarySlashes(location);
        downloadFolder = Uri.parse(location);

        File dir = new File(downloadFolder.getPath());
        if (!dir.isDirectory() && !dir.mkdirs()) {
            // Cannot make the directory
            RxBus.getInstance().post(new RXEvent(RXEvent.TAG_BROWSER_MSG, R.string.problem_location_download));
            return;
        }

        if (!isWriteAccessAvailable(downloadFolder)) {
            RxBus.getInstance().post(new RXEvent(RXEvent.TAG_BROWSER_MSG, R.string.problem_location_download));
            return;
        }

        String cookies = CookieManager.getInstance().getCookie(url);
        if (mimetype == null) {
            FetchUrlMimeType.getHeaderObservable(url, cookies, userAgent)
                    .subscribe(new Action1<FileHeader>() {
                        @Override
                        public void call(FileHeader fileHeader) {
                            DownloadManagerActivity.TasksManager.getImpl().addTask(url, fileHeader.getFileName(), click_id, conversion_link);
                        }
                    });
        } else {
            // 广告联盟
            DownloadManagerActivity.TasksManager.getImpl().addTask(url, filename, click_id, conversion_link);
        }
    }

    private static final String sFileName = "test";
    private static final String sFileExtension = ".txt";

    /**
     * Determine whether there is write access in the given directory. Returns false if a
     * file cannot be created in the directory or if the directory does not exist.
     *
     * @param directory the directory to check for write access
     * @return returns true if the directory can be written to or is in a directory that can
     * be written to. false if there is no write access.
     */
    public static boolean isWriteAccessAvailable(@Nullable String directory) {
        if (directory == null || directory.isEmpty()) {
            return false;
        }
        String dir = addNecessarySlashes(directory);
        dir = getFirstRealParentDirectory(dir);
        File file = new File(dir + sFileName + sFileExtension);
        for (int n = 0; n < 100; n++) {
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                    return true;
                } catch (IOException ignored) {
                    return false;
                }
            } else {
                file = new File(dir + sFileName + '-' + n + sFileExtension);
            }
        }
        return file.canWrite();
    }

    /**
     * Returns the first parent directory of a directory that exists. This is useful
     * for subdirectories that do not exist but their parents do.
     *
     * @param directory the directory to find the first existent parent
     * @return the first existent parent
     */
    @Nullable
    private static String getFirstRealParentDirectory(@Nullable String directory) {
        while (true) {
            if (directory == null || directory.isEmpty()) {
                return "/";
            }
            directory = addNecessarySlashes(directory);
            File file = new File(directory);
            if (!file.isDirectory()) {
                int indexSlash = directory.lastIndexOf('/');
                if (indexSlash > 0) {
                    String parent = directory.substring(0, indexSlash);
                    int previousIndex = parent.lastIndexOf('/');
                    if (previousIndex > 0) {
                        directory = parent.substring(0, previousIndex);
                    } else {
                        return "/";
                    }
                } else {
                    return "/";
                }
            } else {
                return directory;
            }
        }
    }

    private static boolean isWriteAccessAvailable(@NonNull Uri fileUri) {
        File file = new File(fileUri.getPath());
        try {
            if (file.createNewFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    @NonNull
    public static String addNecessarySlashes(@Nullable String originalPath) {
        if (originalPath == null || originalPath.length() == 0) {
            return "/";
        }
        if (originalPath.charAt(originalPath.length() - 1) != '/') {
            originalPath = originalPath + '/';
        }
        if (originalPath.charAt(0) != '/') {
            originalPath = '/' + originalPath;
        }
        return originalPath;
    }

    public static void promptDownload(final Activity activity, final PreferenceManager preference,
                                      final String fileName, final String url, final String userAgent,
                                      final String contentDisposition, final String mimetype,
                                      final String clickid, final String conversion_link) {
        promptDownload(activity, preference, fileName, url, userAgent,
                contentDisposition, mimetype, clickid, conversion_link, -1);
    }

    public static void promptDownload(final Activity activity, final PreferenceManager preference,
                                      final String fileName, final String url, final String userAgent,
                                      final String contentDisposition, final String mimetype,
                                      final String clickid, final String conversion_link, final long contentLength) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                        View layout = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_download, null);

                        TextView tv_file_name = (TextView) layout.findViewById(R.id.tv_file_name);
                        TextView tv_file_size = (TextView) layout.findViewById(R.id.tv_file_size);

                        tv_file_name.setText("名称：" + fileName);
                        if (contentLength != -1) {
                            tv_file_size.setVisibility(View.VISIBLE);
                            tv_file_size.setText("大小：" + Utils.formatFileSize(contentLength) + "M");
                        } else {
                            tv_file_size.setVisibility(View.GONE);
                        }

                        builder.setView(layout);

                        final Dialog dialog = builder.show();

                        layout.findViewById(R.id.tv_left).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();

                            }
                        });

                        layout.findViewById(R.id.tv_right).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                DownloadHandler.onDownloadStart(activity, preference, url, userAgent,
                                        contentDisposition, mimetype, clickid, conversion_link);
                            }
                        });

                    }

                    @Override
                    public void onDenied(String permission) {
                        //TODO showBrowserFragment message
                    }
                });
    }

}

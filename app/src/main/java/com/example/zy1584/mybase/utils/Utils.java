package com.example.zy1584.mybase.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.zy1584.mybase.R;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Date;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by zy1584 on 2017-3-30.
 */

public class Utils {

    public static boolean checkNull(Object object){
        if (null == object){
            return true;
        }else{
            return false;
        }
    }

    public static String formatDate(String date){
        Date d = DateUtil.str2Date(date, "yyyyMMdd");
        return DateUtil.date2Str(d, "MM月dd日  EEEE");
    }

    public static void shareToOther(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, "分享到..."));
    }

    /**
     * Quietly closes a closeable object like an InputStream or OutputStream without
     * throwing any errors or requiring you do do any checks.
     *
     * @param closeable the object to close
     */
    public static void close(@Nullable Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to close cursors. Cursor did not
     * implement Closeable until API 16, so using this
     * method for when we want to close a cursor.
     *
     * @param cursor the cursor to close
     */
    public static void close(@Nullable Cursor cursor) {
        if (cursor == null) {
            return;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDialogSize(@NonNull Context context, @NonNull Dialog dialog) {
        int maxWidth = UIUtils.getDimen(R.dimen.dialog_max_size);
        int padding = UIUtils.getDimen(R.dimen.dialog_padding);
        int screenSize = ScreenUtils.getScreenWidth(context);
        if (maxWidth > screenSize - 2 * padding) {
            maxWidth = screenSize - 2 * padding;
        }
        dialog.getWindow().setLayout(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static boolean doesSupportHeaders() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * Creates a new intent that can launch the email
     * app with a subject, address, body, and cc. It
     * is used to handle mail:to links.
     *
     * @param address the address to send the email to.
     * @param subject the subject of the email.
     * @param body    the body of the email.
     * @param cc      extra addresses to CC.
     * @return a valid intent.
     */
    @NonNull
    public static Intent newEmailIntent(String address, String subject,
                                        String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    /**
     * Displays a snackbar to the user with a String resource.
     *
     * @param activity the activity needed to create a snackbar.
     * @param resource the string resource to show to the user.
     */
    public static void showSnackbar(@NonNull Activity activity, @StringRes int resource) {
        View view = activity.findViewById(R.id.coordinator_layout);
        if (view == null) {
            Log.d(TAG, "Unable to find coordinator layout, using content mContentView");
            view = activity.findViewById(android.R.id.content);
        }
        if (view == null) return;
        Snackbar.make(view, resource, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Displays a snackbar to the user with a string message.
     *
     * @param activity the activity needed to create a snackbar.
     * @param message  the string message to show to the user.
     */
    public static void showSnackbar(@NonNull Activity activity, @NonNull String message) {
        View view = activity.findViewById(R.id.coordinator_layout);
        if (view == null) {
            Log.d(TAG, "Unable to find coordinator layout, using content mContentView");
            view = activity.findViewById(android.R.id.content);
        }
        if (view == null) return;
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Creates a dialog with only a title, message, and okay button.
     *
     * @param activity the activity needed to create a dialog.
     * @param title    the title of the dialog.
     * @param message  the message of the dialog.
     */
    public static void createInformativeDialog(@NonNull Activity activity, @StringRes int title, @StringRes int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(activity.getResources().getString(R.string.action_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
        setDialogSize(activity, alert);
    }

    /**
     * Extracts the domain name from a URL.
     *
     * @param url the URL to extract the domain from.
     * @return the domain name, or the URL if the domain
     * could not be extracted. The domain name may include
     * HTTPS if the URL is an SSL supported URL.
     */
    @Nullable
    public static String getDomainName(@Nullable String url) {
        if (url == null || url.isEmpty()) return "";

        boolean ssl = url.startsWith(Constants.HTTPS);
        int index = url.indexOf('/', 8);
        if (index != -1) {
            url = url.substring(0, index);
        }

        URI uri;
        String domain;
        try {
            uri = new URI(url);
            domain = uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            domain = null;
        }

        if (domain == null || domain.isEmpty()) {
            return url;
        }
        if (ssl)
            return Constants.HTTPS + domain;
        else
            return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static String formatFileSize(long size){
        return new DecimalFormat("##0.00").format(size*1.0/(1024*1024));
    }

    /**
     * speed为kb
     * @param speed
     * @return
     */
    public static String formatSpeed(double speed){
        if (speed >1024){
            return new DecimalFormat("##0.0").format(speed*1.0/1024)+"MB/s";
        }else{
            return new DecimalFormat("##0.0").format(speed*1.0)+"KB/s";
        }
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }

    public static String getPackageName(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            return info.packageName;
        }
        return null;
    }

}

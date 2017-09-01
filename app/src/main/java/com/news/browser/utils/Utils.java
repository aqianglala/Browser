package com.news.browser.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.news.browser.R;
import com.news.browser.preference.PreferenceManager;
import com.news.browser.ui.download.DownloadHandler;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param resource the string resource to showBrowserFragment to the user.
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
     * @param message  the string message to showBrowserFragment to the user.
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

    public static int getApkVersion(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (context.getPackageName().equals(packInfo.packageName)){
            return packInfo.versionCode;
        }
        return -1;
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

    public static String getUrl(String sub) {
        String newIp = (String) SPUtils.get(GlobalParams.IP, GlobalParams.HOLDER_HOST);
        String newPort = (String) SPUtils.get(GlobalParams.PORT, GlobalParams.HOLDER_PORT + "");
        if (TextUtils.isEmpty(newIp)){
            newIp = GlobalParams.HOLDER_HOST;
        }
        if (TextUtils.isEmpty(newPort)){
            newPort = GlobalParams.HOLDER_PORT + "";
        }
        return "http://" + newIp + ":" + newPort + "/" + sub;
    }

    public static boolean isInstallSpecialOS()
    {
        String model = android.os.Build.MODEL;

        //发现DOOV A8有该问题
        if (!TextUtils.isEmpty(model) && (model.equals("DOOV A8")))
        {
            return true;
        }

        return false;
    }

    /**
     * yhw
     * 手机号匹配规则
     * @param mobiles
     * @return
     */
    public static boolean isTelephoneNum(String mobiles) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
    /**yhw
     * 邮箱匹配规则
     *   1:必须包含一个并且只有一个符号@
     *   2:第一个字符不允许是@或者.
     *   3:不允许出现@.或者.@
     *   4:结尾不得是字符@或者.
     *   5:不允许@前出现字符+
     *   6:不允许+在最前面或者 +@
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        String emailStr = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p = Pattern.compile(emailStr);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Creates and returns a new favicon which is the same as the provided
     * favicon but with horizontal or vertical padding of 4dp
     *
     * @param bitmap is the bitmap to pad.
     * @return the padded bitmap.
     */
    public static Bitmap padFavicon(@NonNull Bitmap bitmap) {
        int padding = DensityUtils.dpToPx(4);

        Bitmap paddedBitmap = Bitmap.createBitmap(bitmap.getWidth() + padding, bitmap.getHeight()
                + padding, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0x00, 0x00, 0x00, 0x00); // this represents white color
        canvas.drawBitmap(bitmap, padding / 2, padding / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        return paddedBitmap;
    }

    public static String halfUp(float d){
        BigDecimal bigDecimal = new BigDecimal(d).setScale(0, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.toString();
    }

    /**
     * Downloads a file from the specified URL. Handles permissions
     * requests, and creates all the necessary dialogs that must be
     * showed to the user.
     *
     * @param activity           activity needed to created dialogs.
     * @param url                url to download from.
     * @param userAgent          the user agent of the browser.
     * @param contentDisposition the content description of the file.
     */
    public static void downloadFile(final Activity activity, final PreferenceManager manager, final String url,
                                    final String userAgent, final String contentDisposition) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                String fileName = URLUtil.guessFileName(url, null, null);
                DownloadHandler.onDownloadStart(activity, manager, url, userAgent, contentDisposition, null);
                Log.i(Constants.TAG, "Downloading" + fileName);
            }

            @Override
            public void onDenied(String permission) {
                // TODO Show Message
            }
        });

    }

}

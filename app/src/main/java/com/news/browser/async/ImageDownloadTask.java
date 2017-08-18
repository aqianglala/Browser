package com.news.browser.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.news.browser.utils.Constants;
import com.news.browser.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImageDownloadTask {

    private static final String TAG = ImageDownloadTask.class.getSimpleName();

    public static Observable<Bitmap> getIcon(final Context context, final String mUrl){
        if (TextUtils.isEmpty(mUrl) || context == null) return null;
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                Bitmap bitmap = downloadImage(context, mUrl);
                subscriber.onNext(bitmap);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Bitmap downloadImage(Context context, String mUrl){
        Bitmap mIcon = null;
        // unique path for each url that is bookmarked.
        if (mUrl == null) {
            return null;
        }
        if (context == null) {
            return null;
        }
        File cache = context.getCacheDir();
        final Uri uri = Uri.parse(mUrl);
        if (uri.getHost() == null || uri.getScheme() == null) {
            return null;
        }
        final String hash = String.valueOf(uri.getHost().hashCode());
        final File image = new File(cache, hash + ".png");
        final String urlDisplay = uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
        if (Constants.FILE.startsWith(uri.getScheme())) {
            return null;
        }
        // checks to see if the image exists
        if (!image.exists()) {
            FileOutputStream fos = null;
            InputStream in = null;
            try {
                // if not, download it...
                final URL urlDownload = new URL(urlDisplay);
                final HttpURLConnection connection = (HttpURLConnection) urlDownload.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.connect();
                in = connection.getInputStream();

                if (in != null) {
                    mIcon = BitmapFactory.decodeStream(in);
                }
                // ...and cache it
                if (mIcon != null) {
                    fos = new FileOutputStream(image);
                    mIcon.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    Log.d(Constants.TAG, "Downloaded: " + urlDisplay);
                }

            } catch (Exception ignored) {
                Log.d(TAG, "Could not download: " + urlDisplay);
            } finally {
                Utils.close(in);
                Utils.close(fos);
            }
        } else {
            // if it exists, retrieve it from the cache
            mIcon = BitmapFactory.decodeFile(image.getPath());
        }
        if (mIcon == null) {
            InputStream in = null;
            FileOutputStream fos = null;
            try {
                // if not, download it...
                final URL urlDownload = new URL("https://www.google.com/s2/favicons?domain_url=" + uri.toString());
                final HttpURLConnection connection = (HttpURLConnection) urlDownload.openConnection();
                connection.setDoInput(true);
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.connect();
                in = connection.getInputStream();

                if (in != null) {
                    mIcon = BitmapFactory.decodeStream(in);
                }
                // ...and cache it
                if (mIcon != null) {
                    fos = new FileOutputStream(image);
                    mIcon.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                }

            } catch (Exception e) {
                Log.d(TAG, "Could not download Google favicon");
            } finally {
                Utils.close(in);
                Utils.close(fos);
            }
        }
        return mIcon;
    }

}

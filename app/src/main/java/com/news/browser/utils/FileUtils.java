package com.news.browser.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zy1584 on 2017-8-11.
 */

public class FileUtils {

    public static final String HOME_NAVIGATION_FILE_NAME = "homeNavigation.txt";
    public static final String CHANNEL_LIST_FILE_NAME = "channelList.txt";
    public static final String HOT_SITE_FILE_NAME = "hotTag.txt";

    public static Observable<String> loadFile(@NonNull final Context context, final String fileName) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                AssetManager asset = context.getAssets();
                BufferedReader reader = null;
                StringBuilder builder = new StringBuilder();
                try {
                    //noinspection IOResourceOpenedButNotSafelyClosed
                    reader = new BufferedReader(new InputStreamReader(
                            asset.open(fileName), "UTF-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                        builder.append("\n");
                    }
                } catch (IOException e) {
                    Log.wtf("loadFile", "Reading blocked  from file '"
                            + fileName + "' failed.", e);
                } finally {
                    Utils.close(reader);
                }
                subscriber.onNext(builder.toString());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Writes a stacktrace to the downloads folder with
     * the following filename: [EXCEPTION]_[TIME OF CRASH IN MILLIS].txt
     *
     * @param throwable the Throwable to log to external storage
     */
    public static void writeCrashToStorage(@NonNull Throwable throwable) {
        String fileName = throwable.getClass().getSimpleName() + '_' + System.currentTimeMillis() + ".txt";
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        FileOutputStream outputStream = null;
        try {
            //noinspection IOResourceOpenedButNotSafelyClosed
            outputStream = new FileOutputStream(outputFile);
            throwable.printStackTrace(new PrintStream(outputStream));
            outputStream.flush();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Unable to write bundle to storage");
        } finally {
            Utils.close(outputStream);
        }
    }
}

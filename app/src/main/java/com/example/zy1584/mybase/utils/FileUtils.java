package com.example.zy1584.mybase.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by zy1584 on 2017-8-11.
 */

public class FileUtils {

    public static final String HOME_NAVIGATION_FILE_NAME = "homeNavigation.txt";

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
}

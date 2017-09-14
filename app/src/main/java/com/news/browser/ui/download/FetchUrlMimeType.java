/*
 * Copyright 2014 A.C.R. Development
 */
package com.news.browser.ui.download;

import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * This class is used to pull down the http headers of a given URL so that we
 * can analyse the mimetype and make any correction needed before we give the
 * URL to the download manager. This operation is needed when the user
 * long-clicks on a link or image and we don't know the mimetype. If the user
 * just clicks on the link, we will do the same steps of correcting the mimetype
 * down in android.os.webkit.LoadListener rather than handling it here.
 */
public class FetchUrlMimeType {

    private static final String TAG = FetchUrlMimeType.class.getSimpleName();

    public static Observable<FileHeader> getHeaderObservable(final String uri, final String cookies, final String userAgent) {
        return Observable.create(new Observable.OnSubscribe<FileHeader>() {
            @Override
            public void call(Subscriber<? super FileHeader> subscriber) {
                FileHeader fileHeader = getFileHeader(uri, cookies, userAgent);
                subscriber.onNext(fileHeader);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static FileHeader getFileHeader(String uri, String cookies, String userAgent) {
        // User agent is likely to be null, though the AndroidHttpClient
        // seems ok with that.
        String mimeType = null;
        String contentDisposition = null;
        FileHeader fileHeader = new FileHeader();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            if (cookies != null && !cookies.isEmpty()) {
                connection.addRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", userAgent);
            }
            connection.connect();
            // We could get a redirect here, but if we do lets let
            // the download manager take care of it, and thus trust that
            // the server sends the right mimetype
            if (connection.getResponseCode() == 200) {
                String header = connection.getHeaderField("Content-Type");
                if (header != null) {
                    mimeType = header;
                    final int semicolonIndex = mimeType.indexOf(';');
                    if (semicolonIndex != -1) {
                        mimeType = mimeType.substring(0, semicolonIndex);
                    }
                }
                String contentDispositionHeader = connection.getHeaderField("Content-Disposition");
                if (contentDispositionHeader != null) {
                    contentDisposition = contentDispositionHeader;
                    fileHeader.setContentDisposition(contentDisposition);
                }
            }
        } catch (@NonNull IllegalArgumentException | IOException ex) {
            if (connection != null)
                connection.disconnect();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        String filename = "";
        if (mimeType != null) {
            fileHeader.setMimeType(mimeType);
            if (mimeType.equalsIgnoreCase("text/plain")
                    || mimeType.equalsIgnoreCase("application/octet-stream")) {
                String newMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri));
                if (newMimeType != null) {
                    fileHeader.setMimeType(newMimeType);
                }
            }
        }
        filename = URLUtil.guessFileName(uri, contentDisposition, mimeType);
        fileHeader.setFileName(filename);

        return fileHeader;
    }

}

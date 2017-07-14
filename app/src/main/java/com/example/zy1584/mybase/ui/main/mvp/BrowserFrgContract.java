package com.example.zy1584.mybase.ui.main.mvp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by zy1584 on 2017-7-13.
 */

public interface BrowserFrgContract {
    interface FrgView {

        void showActionBar();

        void hideActionBar();

        void updateProgress(int newProgress);

        boolean isShown();

        void updateUrl(@Nullable String title, boolean shortUrl);

        void updateHistory(@Nullable String title, @NonNull String url);

    }

    interface Presenter{


    }
}

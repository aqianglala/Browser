package com.news.browser.ui.main.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.news.browser.R;
import com.news.browser.utils.UIUtils;


public class BrowserViewTitle {

    @Nullable
    private static Bitmap DEFAULT_DARK_ICON;
    @Nullable
    private static Bitmap DEFAULT_LIGHT_ICON;

    @Nullable
    private Bitmap mFavicon = null;
    @Nullable
    private Bitmap mShot = null;
    @NonNull
    private String mTitle;
    @NonNull
    private final Context mContext;

    public BrowserViewTitle(@NonNull Context context) {
        mContext = context;
        mTitle = context.getString(R.string.action_new_tab);
    }

    /**
     * Set the current favicon to a new Bitmap.
     * May be null, if null, the default will be used.
     *
     * @param favicon the potentially null favicon to set.
     */
    public void setFavicon(@Nullable Bitmap favicon) {
        if (favicon == null) {
            mFavicon = null;
        } else {
//            mFavicon = Utils.padFavicon(favicon);
            mFavicon = favicon;
        }
    }

    /**
     * Helper method to initialize the DEFAULT_ICON variables
     *
     * @param context   the context needed to initialize the Bitmap.
     * @param darkTheme whether the icon should be themed dark or not.
     * @return a not null icon.
     */
    @NonNull
    private static Bitmap getDefaultIcon(@NonNull Context context, boolean darkTheme) {
//        if (darkTheme) {
//            if (DEFAULT_DARK_ICON == null) {
//                DEFAULT_DARK_ICON = ThemeUtils.getThemedBitmap(context, R.drawable.xiaomei48, true);
//            }
//            return DEFAULT_DARK_ICON;
//        } else {
//            if (DEFAULT_LIGHT_ICON == null) {
//                DEFAULT_LIGHT_ICON = ThemeUtils.getThemedBitmap(context, R.drawable.xiaomei48, false);
//            }
//            return DEFAULT_LIGHT_ICON;
//        }
        return BitmapFactory.decodeResource(UIUtils.getResources(), R.mipmap.ic_launcher);
    }

    /**
     * Set the current title to a new title.
     * Must not be null.
     *
     * @param title the non-null title to set.
     */
    public void setTitle(@Nullable String title) {
        if (title == null) {
            mTitle = "";
        } else {
            mTitle = title;
        }
    }

    /**
     * Gets the current title, which is not null.
     * Can be an empty string.
     *
     * @return the non-null title.
     */
    @NonNull
    public String getTitle() {
        return mTitle;
    }

    /**
     * Gets the favicon of the page, which is not null.
     * Either the favicon, or a default icon.
     *
     * @return the favicon or a default if that is null.
     */
    @NonNull
    public Bitmap getFavicon(boolean darkTheme) {
        if (mFavicon == null) {
            return getDefaultIcon(mContext, darkTheme);
        }
        return mFavicon;
    }

    @Nullable
    public Bitmap getmShot() {
        return mShot;
    }

    public void setmShot(@Nullable Bitmap mShot) {
        this.mShot = mShot;
    }

}

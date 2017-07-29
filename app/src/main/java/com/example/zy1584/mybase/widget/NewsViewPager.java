package com.example.zy1584.mybase.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zy1584 on 2017-6-30.
 */

public class NewsViewPager extends ViewPager {
    private boolean isPagingEnabled = false;
    private static final String TAG = "NewsViewPager";

    public NewsViewPager(Context context) {
        super(context);
    }

    public NewsViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

    public boolean isPagingEnabled() {
        return isPagingEnabled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isPagingEnabled){
            if (ev.getAction() == MotionEvent.ACTION_DOWN){
                requestDisallowInterceptTouchEvent(true);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}

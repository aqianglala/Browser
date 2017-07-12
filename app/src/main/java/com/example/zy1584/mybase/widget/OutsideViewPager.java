package com.example.zy1584.mybase.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zy1584 on 2017-7-3.
 */

public class OutsideViewPager extends ViewPager {
    private boolean isPagingEnabled = false;

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

    public OutsideViewPager(Context context) {
        super(context);
    }

    public OutsideViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
//    float x = 0;
//    float y = 0;
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//
//            case MotionEvent.ACTION_DOWN:
//                Log.i("tt", "ACTION_DOWN");
//                // 点击下去的时候获取坐标，父控件下发事件
//                x = ev.getX();
//                y = ev.getY();
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_MOVE:
//                Log.i("tt", "ACTION_MOVE");
//                float dx = ev.getX() - x;
//                float dy = ev.getY() - y;
//                if (Math.abs(dx) > Math.abs(dy)) {// 左右滑动
//                    return isPagingEnabled;
//                } else {// 上下滑动
//                    Log.i("tag", "父控件");
//                    return true;
//                }
//
//        }
//        return super.dispatchTouchEvent(ev);
//    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
//    }


    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof NewsViewPager){
            return ((NewsViewPager) v).isPagingEnabled();
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}

package com.news.browser.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.news.browser.widget.behavior.uc.UcNewsHeaderPagerBehavior;

/**
 * Created by zy1584 on 2017-7-3.
 */

public class OutsideViewPager extends ViewPager {
    private NewsViewPager mNewsViewPager;
    private boolean isPagingEnabled = true;

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

    public OutsideViewPager(Context context) {
        super(context);
    }

    public OutsideViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private UcNewsHeaderPagerBehavior mBehavior;

    public void setBehavior(UcNewsHeaderPagerBehavior mBehavior) {
        this.mBehavior = mBehavior;
    }

    private void findNewsViewPager() {
        if (mNewsViewPager != null) return;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof CoordinatorLayout) {
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) childAt;
                int count = coordinatorLayout.getChildCount();
                for (int j = 0; j < count; j++) {
                    View child = coordinatorLayout.getChildAt(j);
                    if (child instanceof NewsViewPager) {
                        mNewsViewPager = (NewsViewPager) child;
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 解决向上滑动展开新闻页再快速向左滑导致页面错乱的问题
        if (mBehavior != null && mBehavior.isScrolling()) {
            return true;
        }
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 解决向上滑动展开新闻页再快速向左滑导致页面错乱的问题
        if (mBehavior != null && mBehavior.isScrolling()) {
            return true;
        }
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
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


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        int childCount = getChildCount();
//        NewsViewPager newsViewPager = null;
//        for (int i = 0; i< childCount; i++){
//            View child = getChildAt(i);
//            if (child instanceof NewsViewPager){
//                newsViewPager = (NewsViewPager) child;
//                break;
//            }
//        }
//        if (newsViewPager != null && newsViewPager.isPagingEnabled()){
//            return true;
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        int childCount = getChildCount();
//        NewsViewPager newsViewPager = null;
//        for (int i = 0; i< childCount; i++){
//            View child = getChildAt(i);
//            if (child instanceof NewsViewPager){
//                newsViewPager = (NewsViewPager) child;
//                break;
//            }
//        }
//        if (newsViewPager != null && newsViewPager.isPagingEnabled()){
//            return false;
//        }
//        return super.onInterceptTouchEvent(ev);
//    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        findNewsViewPager();
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof NewsViewPager || v instanceof TabLayout) {
            if (mNewsViewPager != null) {
                return mNewsViewPager.isPagingEnabled();
            }
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

}

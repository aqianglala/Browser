package com.example.zy1584.mybase.ui.news.listener;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.BaseNewsItem;

import java.util.List;

/**
 * Created by zy1584 on 2017-7-29.
 */

public class onADVisibilityListener extends RecyclerView.OnScrollListener {

    private LinearLayoutManager mLayoutManager;
    private List<BaseNewsItem> mData;
    private Handler mTimingHandler;

    public onADVisibilityListener(LinearLayoutManager mLayoutManager, List<BaseNewsItem> mData, Handler mTimingHandler) {
        this.mLayoutManager = mLayoutManager;
        this.mData = mData;
        this.mTimingHandler = mTimingHandler;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mData.size() == 0) return;
        int first = mLayoutManager.findFirstVisibleItemPosition();
        int last = mLayoutManager.findLastVisibleItemPosition();
        for (int i = first; i <= last; i ++){
            BaseNewsItem item = mData.get(i);
            if (item instanceof ADResponseBean.DataBean._$8050018672826551Bean.ListBean){// 广告
                ADResponseBean.DataBean._$8050018672826551Bean.ListBean bean = (ADResponseBean.DataBean._$8050018672826551Bean.ListBean) item;
                if (bean.isHasExpose()) continue; // 已经上报
                View childAt = mLayoutManager.getChildAt(i - first);
                int percents = bean.getVisibilityPercents(childAt);
                if (percents >= 50){
                    if (!bean.isTiming()){
                        Message msg = mTimingHandler.obtainMessage();
                        if (msg != null) {
                            msg.obj = bean;
                            bean.setTiming(true);
                            mTimingHandler.sendMessageDelayed(msg, 1000);
                        }
                    }
                }else{
                    bean.setTiming(false);
                    mTimingHandler.removeCallbacksAndMessages(bean);
                }
            }
        }
    }
}

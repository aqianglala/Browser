package com.example.zy1584.mybase.ui.news.mvp.recommend;

import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.ui.news.RecommendBean;
import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface NewsRecommendContract {

    interface View{

        void onReceiveRecommendList(RecommendBean bean);

        void onGetRecommendListError(Throwable e);

        void onReceiveADList(ADResponseBean bean);

        void onGetADListError(Throwable e);

        void onReceiveReportClick(ClickLinkResponseBean bean, ListBean listBean);
    }

    interface Presenter{

        void getNewsRecommendList();

        void getADList();

        void reportClick(String url, ListBean item);

        void reportADExpose(ListBean bean);

    }
}

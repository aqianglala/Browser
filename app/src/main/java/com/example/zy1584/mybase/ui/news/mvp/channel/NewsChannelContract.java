package com.example.zy1584.mybase.ui.news.mvp.channel;

import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface NewsChannelContract {

    interface View{

        void onReceiveNewsChannelList(NewsChannelBean bean);

        void onGetNewsChannelListError(Throwable e);

        void onReceiveADList(ADResponseBean bean);

        void onGetADListError(Throwable e);

        void onReceiveReportClick(ClickLinkResponseBean bean, ListBean listBean);

    }

    interface Presenter{

        void getNewsChannelList(int start, int size, String channelCode);

        void reportActionType(ContentBean bean, String actionType);

        void getADList();

        void reportClick(String url, ListBean item);

        void reportADExpose(ListBean bean);

    }
}

package com.news.browser.ui.news.mvp.channel;

import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.ui.news.bean.NewsChannelBean;
import com.news.browser.bean.ADResponseBean;
import com.news.browser.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;

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

        void reportActionType(NewsChannelBean.DataBean.ListBean.ContentBean bean, String actionType);

        void getADList();

        void reportClick(String url, ListBean item);

        void reportADExpose(ListBean bean);

    }
}

package com.news.browser.ui.news.mvp.channel;

import com.news.browser.bean.ADBean;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.ui.news.bean.NewsChannelBean;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface NewsChannelContract {

    interface View{

        void onReceiveNewsChannelList(NewsChannelBean bean);

        void onGetNewsChannelListError(Throwable e);

        void onReceiveReportClick(ClickLinkResponseBean bean, ADBean listBean);

    }

    interface Presenter{

        void getNewsChannelList(int start, int size, String channelCode);

        void reportActionType(NewsChannelBean.DataBean.ListBean.ContentBean bean, String actionType);

        void reportClick(String url, ADBean item);

        void reportADExpose(ADBean bean);

    }
}

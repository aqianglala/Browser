package com.example.zy1584.mybase.ui.news.mvp.channel;

import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface NewsChannelContract {

    interface View{

        void onSuccess(NewsChannelBean bean);

        void onError(Throwable e);

    }

    interface Presenter{

        void getNewsChannelList(int start, int size, String channelCode);

        void reportActionType(ContentBean bean, String actionType);

    }
}

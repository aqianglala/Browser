package com.example.zy1584.mybase.ui.main.mvp;

import com.example.zy1584.mybase.bean.HomeNavigationBean;
import com.example.zy1584.mybase.ui.main.bean.ChannelBean;

import java.util.Map;

/**
 * Created by zy1584 on 2017-7-13.
 */

public interface MainFrgContract {
    interface View {

        void onReceiveChannelList(Map<String, ChannelBean> channelMap);

        void onGetChannelListError(Throwable e);

        void onReceiveNavigationList(HomeNavigationBean bean);

        void onGetHomeNavigationListError(Throwable e);
    }

    interface Presenter{

        void getChannelList();

        void getHomeNavigationList();
    }
}

package com.news.browser.ui.navigation.mvp;

import com.news.browser.bean.HotTagBean;

/**
 * Created by zy1584 on 2017-7-13.
 */

public interface HotBookmarkContract {
    interface View {

        void onReceiveHotBookmarkList(HotTagBean bean);

        void onGetHotBookmarkListError(Throwable e);
    }

    interface Presenter{

        void getHotBookmarkList();
    }
}

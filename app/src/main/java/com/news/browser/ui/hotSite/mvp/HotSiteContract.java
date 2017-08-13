package com.news.browser.ui.hotSite.mvp;

import com.news.browser.bean.HotSiteBean;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface HotSiteContract {

    interface View{
        void receiveHotSite(HotSiteBean bean);

        void onGetHotSiteError(Throwable e);
    }

    interface Presenter{
        void getHotSite();
    }
}

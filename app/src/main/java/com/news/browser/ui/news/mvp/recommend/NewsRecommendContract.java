package com.news.browser.ui.news.mvp.recommend;

import com.news.browser.bean.ADBean;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.bean.RecommendBean;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface NewsRecommendContract {

    interface View {

        void onReceiveRecommendList(RecommendBean bean);

        void onGetRecommendListError(Throwable e);

        void onReceiveReportClick(ClickLinkResponseBean bean, ADBean listBean);
    }

    interface Presenter {

        void getNewsRecommendList();

        void reportClick(String url, ADBean item);

        void reportADExpose(ADBean bean);

    }
}

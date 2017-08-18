package com.news.browser.ui.feedback.mvp;

import com.news.browser.bean.FeedbackTypeBean;

import java.util.List;

/**
 * Created by zy1584 on 2017-7-25.
 */

public interface FeedbackContract {

    interface View{
        void receiveFeedbackType(FeedbackTypeBean bean);

        void onGetFeedbackTypeError(Throwable e);

        void sendFeedbackSuccess();

        void sendFeedbackError();
    }

    interface Presenter{

        void getFeedbackType();

        void sendFeedback(int feedType, String content, String contact, List<String> imgList);
    }
}

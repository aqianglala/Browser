package com.news.browser.ui.news.mvp.recommend;

import com.news.browser.base.BasePresenter;
import com.news.browser.bean.ADBean;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.bean.RecommendBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.news.NewsRecommendFragment;
import com.orhanobut.logger.Logger;

import java.util.HashMap;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsRecommendPresenter extends BasePresenter<NewsRecommendFragment> implements NewsRecommendContract.Presenter {

    private final NewsRecommendBiz mBiz;

    public NewsRecommendPresenter() {
        mBiz = new NewsRecommendBiz();
    }

    @Override
    public void getNewsRecommendList() {
        HashMap<String, String> queryMap = NetProtocol.getImpl().getBaseParams2();
        Subscription subscribe = mBiz.getNewsRecommendList(queryMap).compose(new ScheduleTransformer<RecommendBean>())
                .subscribe(new Subscriber<RecommendBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetRecommendListError(e);
                    }

                    @Override
                    public void onNext(RecommendBean recommendBean) {
                        if (recommendBean == null) {
                            getIView().onGetRecommendListError(new Exception("返回值为空"));
                            return;
                        }
                        if (recommendBean.getRet() != 0) {
                            getIView().onGetRecommendListError(new Exception("ret != 0"));
                        } else {
                            getIView().onReceiveRecommendList(recommendBean);
                        }
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void reportClick(String url, final ADBean item) {
        Subscription subscribe = mBiz.reportClick(url).compose(new ScheduleTransformer<ClickLinkResponseBean>())
                .subscribe(new Subscriber<ClickLinkResponseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ClickLinkResponseBean bean) {
                        if (bean != null && bean.getRet() == 0) {
                            getIView().onReceiveReportClick(bean, item);
                        }
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void reportADExpose(final ADBean bean) {
        Subscription subscribe = mBiz.reportADExposed(bean.getImpression_link())
                .compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        bean.setThirdTiming(false);
                        Logger.e(e, "个性推荐广告曝光上报失败");
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        bean.setThirdTiming(false);
                        bean.setHasExpose2Third(true);
                    }
                });
        addSubscription(subscribe);
    }
}

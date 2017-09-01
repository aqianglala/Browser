package com.news.browser.ui.hotSite.mvp;

import com.news.browser.base.BasePresenter;
import com.news.browser.bean.HotSiteBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.hotSite.HotSiteFragment;

import java.util.HashMap;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zy1584 on 2017-8-3.
 */

public class HotSitePresenter extends BasePresenter<HotSiteFragment> implements HotSiteContract.Presenter {

    @Override
    public void getHotSite() {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseParams2();
        Subscription subscribe = new HotSiteBiz().getHotSite(params)
                .compose(new ScheduleTransformer<HotSiteBean>())
                .subscribe(new Subscriber<HotSiteBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetHotSiteError(e);
                    }

                    @Override
                    public void onNext(HotSiteBean bean) {
                        if (bean != null && bean.getRet() == 0){
                            getIView().receiveHotSite(bean);
                        }
                    }
                });
        addSubscription(subscribe);
    }
}

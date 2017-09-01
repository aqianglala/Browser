package com.news.browser.ui.navigation.mvp;

import com.news.browser.bean.HotTagBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.navigation.HotTagFragment;
import com.news.browser.base.BasePresenter;

import java.util.HashMap;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class HotBookmarkPresenter extends BasePresenter<HotTagFragment> implements HotBookmarkContract.Presenter {

    private HotBookmarkBiz mBiz;
    public HotBookmarkPresenter() {
        mBiz = new HotBookmarkBiz();
    }


    @Override
    public void getHotBookmarkList() {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseParams2();
        Subscription subscribe = mBiz.getHotTags(params).compose(new ScheduleTransformer<HotTagBean>())
                .subscribe(new Subscriber<HotTagBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetHotBookmarkListError(new Exception("ret != 0"));
                    }

                    @Override
                    public void onNext(HotTagBean hotTagBean) {
                        if (hotTagBean == null) return;
                        if (hotTagBean.getRet() == 0){
                            getIView().onReceiveHotBookmarkList(hotTagBean);
                        }else{
                            getIView().onGetHotBookmarkListError(new Exception("ret != 0"));
                        }
                    }
                });
        addSubscription(subscribe);
    }
}

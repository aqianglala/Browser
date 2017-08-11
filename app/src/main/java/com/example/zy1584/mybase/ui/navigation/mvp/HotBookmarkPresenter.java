package com.example.zy1584.mybase.ui.navigation.mvp;

import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.bean.HotTagBean;
import com.example.zy1584.mybase.http.NetProtocol;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.navigation.HotBookmarkFragment;

import java.util.HashMap;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class HotBookmarkPresenter extends BasePresenter<HotBookmarkFragment> implements HotBookmarkContract.Presenter {

    private HotBookmarkBiz mBiz;
    public HotBookmarkPresenter() {
        mBiz = new HotBookmarkBiz();
    }


    @Override
    public void getHotBookmarkList() {
        HashMap<String, String> params = NetProtocol.getImpl(getIView().getContext()).getBaseParams2();
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

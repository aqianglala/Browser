package com.example.zy1584.mybase.ui.news.mvp.recommend;

import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.http.NetProtocol;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.news.NewsRecommendFragment;
import com.example.zy1584.mybase.ui.news.RecommendBean;
import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

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
        HashMap<String, String> queryMap = NetProtocol.getImpl(getIView().getActivity()).getRecommendNewsQueryMap();
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
                        getIView().onReceiveRecommendList(recommendBean);
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void getADList() {
        HashMap<String, String> adMap = NetProtocol.getImpl(getIView().getActivity()).getADMap();
        Subscription subscribe = mBiz.getADList(adMap).compose(new ScheduleTransformer<ADResponseBean>())
                .subscribe(new Subscriber<ADResponseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetADListError(e);
                    }

                    @Override
                    public void onNext(ADResponseBean adResponseBean) {
                        int ret = adResponseBean.getRet();
                        if (ret != 0){
                            getIView().onGetADListError(new Exception(adResponseBean.getMsg()));
                        }else{
                            getIView().onReceiveADList(adResponseBean);
                        }
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void reportClick(String url, final ListBean item) {
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
                        getIView().onReceiveReportClick(bean, item);
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void reportADExpose(final ListBean bean) {
        Subscription subscribe = mBiz.reportADExposed(bean.getImpression_link())
                .compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        if (responseBody == null) return;
                        try {
                            JSONObject object = new JSONObject(responseBody.string());
                            int ret = object.optInt("ret");
                            if (ret == 0){
                                bean.setHasExpose(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        addSubscription(subscribe);
    }
}

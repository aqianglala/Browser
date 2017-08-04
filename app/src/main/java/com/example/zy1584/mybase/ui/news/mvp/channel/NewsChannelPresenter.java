package com.example.zy1584.mybase.ui.news.mvp.channel;

import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.bean.ADResponseBean;
import com.example.zy1584.mybase.bean.ClickLinkResponseBean;
import com.example.zy1584.mybase.http.NetProtocol;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.news.NewsChannelFragment;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class NewsChannelPresenter extends BasePresenter<NewsChannelFragment> implements NewsChannelContract.Presenter {

    private NewsChannelBiz mBiz;
    public NewsChannelPresenter() {
        mBiz = new NewsChannelBiz();
    }

    @Override
    public void getNewsChannelList(int start, int size, String channelCode) {
        HashMap<String, String> queryMap = NetProtocol.getImpl(getIView().getActivity())
                .getChannelNewsQueryMap(start, size, channelCode);
        Subscription subscribe = mBiz.getNewsChannelList(queryMap)
                .compose(new ScheduleTransformer<NewsChannelBean>())
                .subscribe(new Subscriber<NewsChannelBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetNewsChannelListError(e);
                    }

                    @Override
                    public void onNext(NewsChannelBean bean) {
                        if (bean == null) return;
                        if (bean.getRet() != 0){
                            getIView().onGetNewsChannelListError(new Exception(bean.getMsg()));
                        }else{
                            getIView().onReceiveNewsChannelList(bean);
                        }
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void reportActionType(ContentBean bean, String actionType) {
        HashMap<String, String> actionMap = NetProtocol.getImpl(getIView().getActivity())
                .getReportActionMap(bean.getId(), bean.getChannel_id(), actionType);
        Subscription subscribe = mBiz.reportActionType(actionMap)
                .compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        if (responseBody == null) return;
                        try {
                            String response = responseBody.string();
                            JSONObject obj = new JSONObject(response);
                            int ret = obj.optInt("ret");
                            if (ret == 0){
                                Logger.d("行为上报成功");
                            }else{
                                String msg = obj.optString("msg");
                                Logger.d("行为上报失败，msg： "+ msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                        if (adResponseBean == null){
                            getIView().onGetADListError(new Exception("返回值为空"));
                            return;
                        }
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
                        if (bean != null && bean.getRet() == 0){
                            getIView().onReceiveReportClick(bean, item);
                        }
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
                        bean.setHasExpose(true);
                    }
                });
        addSubscription(subscribe);
    }
}

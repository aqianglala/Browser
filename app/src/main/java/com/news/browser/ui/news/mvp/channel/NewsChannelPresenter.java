package com.news.browser.ui.news.mvp.channel;

import com.news.browser.base.BasePresenter;
import com.news.browser.bean.ADBean;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.news.NewsChannelFragment;
import com.news.browser.ui.news.bean.NewsChannelBean;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;

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
        HashMap<String, String> queryMap = NetProtocol.getImpl()
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
                        if (bean.getRet() != 0) {
                            getIView().onGetNewsChannelListError(new Exception(bean.getMsg()));
                        } else {
                            getIView().onReceiveNewsChannelList(bean);
                        }
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void reportActionType(NewsChannelBean.DataBean.ListBean.ContentBean bean, String actionType) {
        HashMap<String, String> actionMap = NetProtocol.getImpl()
                .getReportActionMap(bean.getId(), bean.getChannel_id(), actionType);
        Subscription subscribe = mBiz.reportActionType(actionMap)
                .compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "垂直频道上报失败");
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        if (body == null) return;
                        try {
                            String response = body.string();
                            JSONObject obj = new JSONObject(response);
                            int ret = obj.optInt("ret");
                            if (ret == 0) {
                                Logger.d("行为上报成功");
                            } else {
                                String msg = obj.optString("msg");
                                Logger.d("行为上报失败，msg： " + msg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
                        Logger.e(e, "垂直频道新闻点击上报失败");
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
                        Logger.e(e, "垂直频道广告曝光上报失败");
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

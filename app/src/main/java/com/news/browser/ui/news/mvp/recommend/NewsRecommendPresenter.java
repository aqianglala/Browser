package com.news.browser.ui.news.mvp.recommend;

import com.news.browser.base.BasePresenter;
import com.news.browser.bean.ADBean;
import com.news.browser.bean.ClickLinkResponseBean;
import com.news.browser.bean.RecommendBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.news.NewsRecommendFragment;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

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
    public void getADList() {
        HashMap<String, String> adMap = NetProtocol.getImpl().getADMap();
        Subscription subscribe = mBiz.getADList(adMap).compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetADListError(e);
                    }

                    @Override
                    public void onNext(ResponseBody body) {
                        if (body == null) {
                            getIView().onGetADListError(new Exception("返回值为空"));
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(body.string());
                            int ret = jsonObject.optInt("ret");
                            if (ret == 0){
                                JSONObject data = jsonObject.optJSONObject("data");
                                Iterator<String> iterator = data.keys();
                                if (iterator.hasNext()){
                                    String key = iterator.next();
                                    JSONObject obj = data.getJSONObject(key);
                                    JSONArray list = obj.getJSONArray("list");
                                    if (list != null && list.length() > 0){
                                        JSONObject jo = list.getJSONObject(0);
                                        String click_link = jo.optString("click_link");
                                        String conversion_link = jo.optString("conversion_link");
                                        int crt_type = jo.optInt("crt_type");
                                        String description = jo.optString("description");
                                        String img_url = jo.optString("img_url");
                                        String impression_link = jo.optString("impression_link");
                                        int interact_type = jo.optInt("interact_type");
                                        boolean is_full_screen_interstitial = jo.optBoolean("is_full_screen_interstitial");
                                        String title = jo.optString("title");

                                        ADBean adBean = new ADBean();
                                        adBean.setClick_link(click_link);
                                        adBean.setConversion_link(conversion_link);
                                        adBean.setCrt_type(crt_type);
                                        adBean.setDescription(description);
                                        adBean.setImg_url(img_url);
                                        adBean.setImpression_link(impression_link);
                                        adBean.setInteract_type(interact_type);
                                        adBean.setIs_full_screen_interstitial(is_full_screen_interstitial);
                                        adBean.setTitle(title);

                                        getIView().onReceiveADList(adBean);
                                    }
                                }
                            }else{
                                getIView().onGetADListError(new Exception("ret = " + ret));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getIView().onGetADListError(e);
                        } catch (IOException e) {
                            e.printStackTrace();
                            getIView().onGetADListError(e);
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

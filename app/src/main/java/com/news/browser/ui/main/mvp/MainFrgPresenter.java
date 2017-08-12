package com.news.browser.ui.main.mvp;

import com.news.browser.base.BasePresenter;
import com.news.browser.bean.HomeNavigationBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.ui.main.MainFragment;
import com.news.browser.ui.main.bean.ChannelBean;
import com.news.browser.utils.GlobalParams;
import com.news.browser.utils.SPUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zy1584 on 2017-7-26.
 */

public class MainFrgPresenter extends BasePresenter<MainFragment> implements MainFrgContract.Presenter {

    private MainFrgBiz mBiz;
    public MainFrgPresenter() {
        mBiz = new MainFrgBiz();
    }

    @Override
    public void getChannelList() {
        HashMap<String, String> queryMap = NetProtocol.getImpl(getIView().getActivity()).getBaseParams2();
        Subscription subscribe = mBiz.getChannelList(queryMap).compose(new ScheduleTransformer<ResponseBody>())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetChannelListError(e);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (responseBody == null) return;
                        try {
                            String jsonStr = responseBody.string();
                            JSONObject object = new JSONObject(jsonStr);
                            int ret = object.optInt("ret");
                            if (ret == 0){
                                SPUtils.put(GlobalParams.DATA_CHANNEL, jsonStr);// 缓存
                                Map<String, ChannelBean> result = new HashMap();

                                JSONObject data = object.getJSONObject("data");
                                Iterator<String> iterator = data.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    JSONObject obj = data.getJSONObject(key);
                                    String chanCode = obj.optString("chanCode");
                                    String chanId = obj.optString("chanId");
                                    String chanName = obj.optString("chanName");
                                    String chanName_m = obj.optString("chanName_m");
                                    String isKB = obj.optString("isKB");
                                    ChannelBean bean = new ChannelBean(chanCode, chanId, chanName, chanName_m, isKB);
                                    result.put(key, bean);
                                }
                                getIView().onReceiveChannelList(result);
                            }else{
                                getIView().onGetChannelListError(new Exception("ret: " + ret));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            getIView().onGetChannelListError(e);
                        }
                    }
                });
        addSubscription(subscribe);
    }

    @Override
    public void getHomeNavigationList() {
        HashMap<String, String> queryMap = NetProtocol.getImpl(getIView().getActivity()).getBaseParams2();
        Subscription subscribe = mBiz.getHomeNavigationList(queryMap)
                .compose(new ScheduleTransformer<HomeNavigationBean>())
                .subscribe(new Subscriber<HomeNavigationBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIView().onGetHomeNavigationListError(e);
                    }

                    @Override
                    public void onNext(HomeNavigationBean homeNavigationBean) {
                        if (homeNavigationBean == null) return;
                        if (homeNavigationBean.getRet() == 0){
                            getIView().onReceiveNavigationList(homeNavigationBean);
                        }else{
                            getIView().onGetHomeNavigationListError(new Exception("ret != 0"));
                        }
                    }
                });
        addSubscription(subscribe);
    }


}

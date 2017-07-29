package com.example.zy1584.mybase.ui.news.mvp.channel;

import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.http.NetProtocol;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.news.NewsChannelFragment;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
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
                        getIView().onError(e);
                    }

                    @Override
                    public void onNext(NewsChannelBean bean) {
                        getIView().onSuccess(bean);
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
}

package com.example.zy1584.mybase.ui.main.mvp;

import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.http.NetProtocol;
import com.example.zy1584.mybase.http.transformer.ScheduleTransformer;
import com.example.zy1584.mybase.ui.main.MainFragment;
import com.example.zy1584.mybase.ui.main.bean.ChannelBean;

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

    @Override
    public void getChannelList() {
        HashMap<String, String> queryMap = NetProtocol.getImpl(getIView().getActivity()).getChannelListQueryMap();
        Subscription subscribe = new MainFrgBiz().getChannelList(queryMap).compose(new ScheduleTransformer<ResponseBody>())
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
                        try {
                            String jsonStr = responseBody.string();
                            JSONObject object = new JSONObject(jsonStr);
                            int ret = object.optInt("ret");
                            if (ret == 0){
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
}

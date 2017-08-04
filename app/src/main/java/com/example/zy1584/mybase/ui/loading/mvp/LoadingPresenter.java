package com.example.zy1584.mybase.ui.loading.mvp;

import com.example.zy1584.mybase.base.BasePresenter;
import com.example.zy1584.mybase.ui.loading.LoadingActivity;

/**
 * Created by zy1584 on 2017-8-3.
 */

public class LoadingPresenter extends BasePresenter<LoadingActivity> {

    public void requestServerAddress(String longitude, String latitude){
//        HashMap<String, String> queryMap = NetProtocol.getImpl(getIView()).getRequestServerAddressQueryMap(longitude, latitude);
//        Subscription subscribe = new LoadingBiz().requestServerAddress(queryMap)
//                .compose(new ScheduleTransformer<ResponseBody>())
//                .subscribe(new Subscriber<ResponseBody>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody body) {
//                        if (body != null){
//                            try {
//                                JSONArray jsonArray = new JSONArray(body.string());
//                                JSONObject obj = (JSONObject) jsonArray.get(0);
//                                String ip = obj.optString("IP");
//                                String port = obj.optString("Port");
//                                SPUtils.put(GlobalParams.IP, ip);
//                                SPUtils.put(GlobalParams.PORT, port);
//                                getIView().onReceiveServerAddress(body);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//        addSubscription(subscribe);
    }
}

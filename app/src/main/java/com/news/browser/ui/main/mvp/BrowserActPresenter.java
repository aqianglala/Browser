package com.news.browser.ui.main.mvp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.news.browser.R;
import com.news.browser.base.BasePresenter;
import com.news.browser.bean.ADBean;
import com.news.browser.bean.EngineBean;
import com.news.browser.http.NetProtocol;
import com.news.browser.http.transformer.ScheduleTransformer;
import com.news.browser.manager.TabsManager;
import com.news.browser.ui.main.BrowserActivity;
import com.news.browser.ui.main.BrowserFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.Subscription;


/**
 * Created by zy1584 on 2017-7-15.
 */

public class BrowserActPresenter extends BasePresenter<BrowserActivity> implements BrowserActContract.Presenter {
    private final BrowserActBiz mBiz;
    @NonNull
    private TabsManager mTabsModel;
    private BrowserFragment mCurrentTab;

    public BrowserActPresenter() {
        mBiz = new BrowserActBiz();
    }

    @Override
    public boolean newTab(@Nullable String url, boolean isIncognito, boolean show) {
        mTabsModel = getIView().getTabModel();
        if (mTabsModel.size() >= 20) {
            getIView().showSnackBar(R.string.max_tabs);
            return false;
        }
        mTabsModel.newTab(url, isIncognito, show);
        return true;
    }

    @Override
    public void loadUrlInCurrentView(@NonNull String url) {
        final BrowserFragment currentTab = mTabsModel.getCurrentFragment();
        if (currentTab == null) {
            getIView().loadUrlInNewFragment(url);
            return;
        }
        currentTab.loadUrl(url);
    }

    @Override
    public void getSearchEngine() {
        HashMap<String, String> params = NetProtocol.getImpl().getBaseParams2();
        Subscription subscribe = mBiz.getSearchEngine(params).compose(new ScheduleTransformer<EngineBean>())
                .subscribe(new Subscriber<EngineBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(EngineBean bean) {
                        if (bean == null) return;
                        if (bean.getRet() == 0){
                            getIView().receiveSearchEngine(bean);
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
                                        ArrayList<ADBean> adList = new ArrayList<>();
                                        for(int i = 0; i< list.length(); i++){
                                            JSONObject jo = list.getJSONObject(i);
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

                                            adList.add(adBean);
                                        }
                                        getIView().onReceiveADList(adList);
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

}

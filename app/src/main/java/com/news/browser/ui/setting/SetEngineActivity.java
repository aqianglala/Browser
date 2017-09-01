package com.news.browser.ui.setting;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.news.browser.R;
import com.news.browser.base.BaseActivity;
import com.news.browser.base.BasePresenter;
import com.news.browser.bean.EngineBean.EngineItem;
import com.news.browser.bus.RXEvent;
import com.news.browser.db.EngineDatabase;
import com.news.browser.utils.RxBus;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class SetEngineActivity extends BaseActivity {
    private EngineDatabase mEngineDatabase;
    private List<EngineItem> mEngineItems = new ArrayList<>();
    private EngineItem mDefaultEngine;
    private EnginesAdapter mEnginesAdapter;

    @BindView(R.id.tv_title)
    TextView tv_title;

    @BindView(R.id.rv_engines)
    RecyclerView rv_engines;

    @OnClick(R.id.iv_back)
    void back(){
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_engine;
    }

    @Override
    protected BasePresenter loadPresenter() {
        return null;
    }

    @Override
    protected void initView() {
        super.initView();
        mEnginesAdapter = new EnginesAdapter(mActivity, R.layout.item_list_engine, mEngineItems);
        rv_engines.setLayoutManager(new LinearLayoutManager(mActivity));
        rv_engines.setHasFixedSize(true);
        rv_engines.setItemAnimator(new DefaultItemAnimator());
        rv_engines.addItemDecoration(getDefaultDivider());
        rv_engines.setAdapter(mEnginesAdapter);
    }

    @Override
    protected void doBusiness(Bundle savedInstanceState) {
        tv_title.setText(R.string.set_search_engine);
        mEngineDatabase = EngineDatabase.getInstance();

        getEngineList().subscribe(new Action1<List<EngineItem>>() {
            @Override
            public void call(List<EngineItem> engineItems) {
                if (engineItems != null && engineItems.size() > 0){
                    mEngineItems.clear();
                    mEngineItems.addAll(engineItems);
                    mEnginesAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private Observable<List<EngineItem>> getEngineList() {
        return Observable.create(new Observable.OnSubscribe<List<EngineItem>>() {
            @Override
            public void call(Subscriber<? super List<EngineItem>> subscriber) {
                List<EngineItem> engineList = mEngineDatabase.getAllEngineItems();
                subscriber.onNext(engineList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    class EnginesAdapter extends CommonAdapter<EngineItem> {


        public EnginesAdapter(Context context, int layoutId, List<EngineItem> datas) {
            super(context, layoutId, datas);
        }

        @Override
        protected void convert(final ViewHolder holder, EngineItem item, int position) {
            Context context = holder.getConvertView().getContext();
            ImageView iv_icon = holder.getView(R.id.iv_icon);
            ImageView iv_choose = holder.getView(R.id.iv_choose);
            Glide.with(context).load(item.getIconUrl()).into(iv_icon);
            holder.setText(R.id.tv_name, item.getName());
            if (item.getIsDefault() == 1){
                iv_choose.setVisibility(View.VISIBLE);
            }else{
                iv_choose.setVisibility(View.GONE);
            }
            holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    updateDefaultEngine(layoutPosition)
                            .subscribe(new Action1<EngineItem>() {
                                @Override
                                public void call(EngineItem item) {
                                    RxBus.getInstance().post(new RXEvent(RXEvent.TAG_UPDATE_DEFAULT_ENGINE, ""));
                                    notifyDataSetChanged();
                                }
                            });
                }
            });
        }
    }

    private Observable updateDefaultEngine(final int layoutPosition) {
        return Observable.create(new Observable.OnSubscribe<EngineItem>() {
            @Override
            public void call(Subscriber<? super EngineItem> subscriber) {
                for (int i = 0; i< mEngineItems.size(); i++){
                    EngineItem engineItem = mEngineItems.get(i);
                    if (i == layoutPosition){
                        engineItem.setIsDefault(1);
                        mDefaultEngine = engineItem;
                    }else{
                        engineItem.setIsDefault(0);
                    }
                }
                updateEngineDB(mDefaultEngine);
                subscriber.onNext(mDefaultEngine);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void updateEngineDB(EngineItem defaultEngine) {
        mEngineDatabase.setDefaultEngine(defaultEngine.getAddrUrl());
    }
}

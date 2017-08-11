package com.example.zy1584.mybase.ui.navigation.Adapter;

import android.content.Context;

import com.example.zy1584.mybase.bean.HotTagBean;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-7.
 */

public class HotTagAdapter extends MultiItemTypeAdapter<HotTagBean.DataBean>{
    public HotTagAdapter(Context context, List<HotTagBean.DataBean> datas) {
        super(context, datas);
        addItemViewDelegate(new ItemDelegate());
        addItemViewDelegate(new ItemAddDelegate());
    }
}

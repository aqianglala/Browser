package com.example.zy1584.mybase.ui.news.adapter.channel;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemThreeImgDelegate implements ItemViewDelegate<ContentBean> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_3_img;
    }

    @Override
    public boolean isForViewType(ContentBean item, int position) {
        if ("1".equals(item.getArticletype())){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, ContentBean newslistBean, int position) {
        Context context = holder.getConvertView().getContext();
        holder.setText(R.id.tv_title, newslistBean.getTitle());
        holder.setText(R.id.tv_source, newslistBean.getSrc());

        List<String> image33_l = newslistBean.getImage33_l();
        if (image33_l.size() != 3) return;
        Glide.with(context)
                .load(image33_l.get(0))
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_first));
        Glide.with(context)
                .load(image33_l.get(1))
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_second));
        Glide.with(context)
                .load(image33_l.get(2))
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_third));
    }
}

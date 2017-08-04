package com.example.zy1584.mybase.ui.news.adapter.channel;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.bean.BaseNewsItem;
import com.example.zy1584.mybase.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean ;
import com.example.zy1584.mybase.utils.DateUtil;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemBigImgDelegate implements ItemViewDelegate<BaseNewsItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_big_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof ContentBean){
            ContentBean bean = (ContentBean) item;
            if (!"1".equals(bean.getArticletype()) && position % 10 == 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        ContentBean bean = (ContentBean) item;
        Context context = holder.getConvertView().getContext();
        long timeStamp = Long.parseLong(bean.getTimestamp());
        String time = DateUtil.getStandardTime(timeStamp);
        holder.setText(R.id.tv_time, time);
        Glide.with(context)
                .load(bean.getThumbnails_pic().getQqnews_thu_big())
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_thumbnail));
    }
}

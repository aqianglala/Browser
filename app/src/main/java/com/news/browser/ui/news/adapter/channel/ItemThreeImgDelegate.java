package com.news.browser.ui.news.adapter.channel;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.news.browser.R;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.ui.news.bean.NewsChannelBean.DataBean.ListBean.ContentBean;
import com.news.browser.utils.DateUtil;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.List;


/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemThreeImgDelegate implements ItemViewDelegate<BaseNewsItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_3_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof ContentBean) {
            ContentBean bean = (ContentBean) item;
            if ("1".equals(bean.getArticletype())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        ContentBean bean = (ContentBean) item;
        Context context = holder.getConvertView().getContext();
        holder.setText(R.id.tv_title, bean.getTitle());
        holder.setText(R.id.tv_source, bean.getSrc());
        String timestamp = bean.getTimestamp();
        String convertTime = DateUtil.converTime(Long.parseLong(timestamp));
        holder.setText(R.id.tv_time, convertTime);

        List<String> image33_l = bean.getImage33_l();
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

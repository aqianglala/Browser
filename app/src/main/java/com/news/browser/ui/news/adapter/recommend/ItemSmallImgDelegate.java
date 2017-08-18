package com.news.browser.ui.news.adapter.recommend;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.news.browser.R;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.bean.RecommendBean;
import com.news.browser.utils.DateUtil;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemSmallImgDelegate implements ItemViewDelegate<BaseNewsItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_small_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof RecommendBean.NewslistBean) {
            RecommendBean.NewslistBean bean = (RecommendBean.NewslistBean) item;
            if (!"1".equals(bean.getArticletype()) && position % 10 != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        RecommendBean.NewslistBean bean = (RecommendBean.NewslistBean) item;
        Context context = holder.getConvertView().getContext();
        holder.setText(R.id.tv_title, bean.getTitle());
        holder.setText(R.id.tv_source, bean.getSource());
        String convertTime = DateUtil.converTime(bean.getTimestamp());
        holder.setText(R.id.tv_time, convertTime);
        Glide.with(context)
                .load(bean.getThumbnails_qqnews().getQqnews_thu())
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_thumbnail));
    }
}

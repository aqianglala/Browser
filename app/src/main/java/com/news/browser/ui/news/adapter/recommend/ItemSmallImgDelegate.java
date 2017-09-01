package com.news.browser.ui.news.adapter.recommend;

import android.content.Context;
import android.widget.ImageView;

import com.news.browser.R;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.bean.RecommendBean;
import com.news.browser.utils.DateUtil;
import com.news.browser.utils.GlideUtils;
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
            if (!"1".equals(bean.getArticletype())) {
                if (position == 0 || position % 10 != 0)
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
        String convertTime = DateUtil.convertTime(bean.getTimestamp());
        holder.setText(R.id.tv_time, convertTime);
        GlideUtils.loadNewsImage(context, bean.getThumbnails_qqnews().getQqnews_thu(),
                (ImageView) holder.getView(R.id.iv_thumbnail));
    }
}

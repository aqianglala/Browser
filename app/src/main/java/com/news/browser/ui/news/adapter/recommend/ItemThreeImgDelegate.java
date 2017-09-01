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

public class ItemThreeImgDelegate implements ItemViewDelegate<BaseNewsItem> {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_3_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof RecommendBean.NewslistBean) {
            RecommendBean.NewslistBean bean = (RecommendBean.NewslistBean) item;
            if ("1".equals(bean.getArticletype())) {
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

        String fimgurl33 = bean.getFimgurl33();
        String[] imgList = fimgurl33.split(",");
        if (imgList.length != 3) return;
        GlideUtils.loadNewsImage(context, imgList[0], (ImageView) holder.getView(R.id.iv_first));
        GlideUtils.loadNewsImage(context, imgList[1], (ImageView) holder.getView(R.id.iv_second));
        GlideUtils.loadNewsImage(context, imgList[2], (ImageView) holder.getView(R.id.iv_third));
    }
}

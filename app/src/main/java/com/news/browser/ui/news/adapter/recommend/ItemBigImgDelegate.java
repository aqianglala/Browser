package com.news.browser.ui.news.adapter.recommend;

import android.content.Context;
import android.widget.ImageView;

import com.news.browser.R;
import com.news.browser.base.BaseApplication;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.bean.RecommendBean.NewslistBean;
import com.news.browser.utils.DateUtil;
import com.news.browser.utils.GlideUtils;
import com.news.browser.utils.ScreenUtils;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemBigImgDelegate implements ItemViewDelegate<BaseNewsItem> {

    private final int screenWidth;

    public ItemBigImgDelegate() {
        screenWidth = ScreenUtils.getScreenWidth(BaseApplication.getContext());
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_big_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof NewslistBean) {
            NewslistBean bean = (NewslistBean) item;
            if (!"1".equals(bean.getArticletype()) && position != 0 && position % 10 == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        NewslistBean bean = (NewslistBean) item;
        Context context = holder.getConvertView().getContext();

        String imgUrl = null;
        if (screenWidth > 720) {
            imgUrl = bean.getFimgurl30();
        } else {
            imgUrl = bean.getFimgurl29();
        }
        holder.setText(R.id.tv_title, bean.getTitle());

        holder.setText(R.id.tv_source, bean.getSource());
        String convertTime = DateUtil.convertTime(bean.getTimestamp());
        holder.setText(R.id.tv_time, convertTime);

        GlideUtils.loadNewsImage(context, imgUrl,
                (ImageView) holder.getView(R.id.iv_thumbnail));
    }
}

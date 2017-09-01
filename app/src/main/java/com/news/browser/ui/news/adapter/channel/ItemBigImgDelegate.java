package com.news.browser.ui.news.adapter.channel;

import android.content.Context;
import android.widget.ImageView;

import com.news.browser.R;
import com.news.browser.base.BaseApplication;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.ui.news.bean.NewsChannelBean;
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
        if (item instanceof NewsChannelBean.DataBean.ListBean.ContentBean) {
            NewsChannelBean.DataBean.ListBean.ContentBean bean = (NewsChannelBean.DataBean.ListBean.ContentBean) item;
            if (!"1".equals(bean.getArticletype()) && position != 0 && position % 10 == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        NewsChannelBean.DataBean.ListBean.ContentBean bean = (NewsChannelBean.DataBean.ListBean.ContentBean) item;
        Context context = holder.getConvertView().getContext();

        holder.setText(R.id.tv_title, bean.getTitle());

        holder.setText(R.id.tv_source, bean.getSrc());
        String timestamp = bean.getTimestamp();
        String convertTime = DateUtil.convertTime(Long.parseLong(timestamp));
        holder.setText(R.id.tv_time, convertTime);

        String imgUrl;
        if (screenWidth > 720){
            imgUrl = bean.getImage30();
        }else{
            imgUrl = bean.getThumbnails_pic().getQqnews_thu_big();
        }
        GlideUtils.loadNewsImage(context, imgUrl,
                (ImageView) holder.getView(R.id.iv_thumbnail));
    }
}

package com.example.zy1584.mybase.ui.news.adapter.recommend;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.bean.BaseNewsItem;
import com.example.zy1584.mybase.ui.news.RecommendBean.NewslistBean;
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
        if (item instanceof NewslistBean){
            NewslistBean bean = (NewslistBean) item;
            if ("1".equals(bean.getArticletype())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        NewslistBean newslistBean = (NewslistBean) item;
        Context context = holder.getConvertView().getContext();
        holder.setText(R.id.tv_title, newslistBean.getTitle());
        holder.setText(R.id.tv_source, newslistBean.getSource());

        String fimgurl33 = newslistBean.getFimgurl33();
        String[] imgList = fimgurl33.split(",");
        if (imgList.length != 3) return;
        Glide.with(context)
                .load(imgList[0])
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_first));
        Glide.with(context)
                .load(imgList[1])
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_second));
        Glide.with(context)
                .load(imgList[2])
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_third));
    }
}

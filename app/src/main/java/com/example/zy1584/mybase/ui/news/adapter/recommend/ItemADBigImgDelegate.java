package com.example.zy1584.mybase.ui.news.adapter.recommend;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.zy1584.mybase.R;
import com.example.zy1584.mybase.bean.ADResponseBean.DataBean._$8050018672826551Bean.ListBean;
import com.example.zy1584.mybase.bean.BaseNewsItem;
import com.example.zy1584.mybase.ui.news.adapter.recommend.NewsRecommendAdapter.onADItemClickListener;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemADBigImgDelegate implements ItemViewDelegate<BaseNewsItem> {
    private onADItemClickListener mListener;

    public ItemADBigImgDelegate(onADItemClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_ad_big_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof ListBean){
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        ListBean bean = (ListBean) item;
        Context context = holder.getConvertView().getContext();
        holder.setText(R.id.tv_title, bean.getTitle());
        Glide.with(context)
                .load(bean.getImg_url())
//                .placeholder(R.mipmap.ic_launcher)// TODO: 2017-7-26
                .into((ImageView) holder.getView(R.id.iv_thumbnail));
        holder.itemView.setOnTouchListener(new MyOnTouchListener(holder, bean, position, mListener));
    }

    private class MyOnTouchListener implements View.OnTouchListener {

        private int downX = 0;
        private int downY = 0;
        private int upX = 0;
        private int upY = 0;
        private long downTime = 0;

        private ViewHolder holder;
        private ListBean item;
        private int position;
        private onADItemClickListener listener;

        public MyOnTouchListener(ViewHolder holder, ListBean item, int position, onADItemClickListener listener) {
            this.holder = holder;
            this.item = item;
            this.position = position;
            this.listener = listener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = (int) event.getX();
                    downY = (int) event.getY();
                    downTime = System.currentTimeMillis();
                    Log.i("onTouch", "ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("onTouch", "ACTION_MOVE");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("onTouch", "ACTION_UP");
                    upX = (int) event.getX();
                    upY = (int) event.getY();
                    if (downX == upX && System.currentTimeMillis() - downTime < 300) {
                        // 获取当前的position
                        if (this.listener != null) {
                            this.listener.onADItemClick(holder, item, position, downX, downY, upX, upY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:// 取消
                    Log.i("onTouch", "ACTION_CANCEL");
                    break;
            }
            return true;
        }
    }
}

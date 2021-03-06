package com.news.browser.ui.news.adapter;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.news.browser.R;
import com.news.browser.bean.ADBean;
import com.news.browser.bean.BaseNewsItem;
import com.news.browser.ui.news.interfaces.OnADItemClickListener;
import com.news.browser.utils.GlideUtils;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;


/**
 * Created by zy1584 on 2017-7-25.
 */

public class ItemADBigImgDelegate implements ItemViewDelegate<BaseNewsItem> {
    private OnADItemClickListener mListener;

    public ItemADBigImgDelegate(OnADItemClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_news_ad_big_img;
    }

    @Override
    public boolean isForViewType(BaseNewsItem item, int position) {
        if (item instanceof ADBean) {
            return true;
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, BaseNewsItem item, int position) {
        ADBean bean = (ADBean) item;
        Context context = holder.getConvertView().getContext();
        holder.setText(R.id.tv_title, bean.getTitle());
        GlideUtils.loadNewsImage(context, bean.getImg_url(), (ImageView) holder.getView(R.id.iv_thumbnail));
        holder.itemView.setOnTouchListener(new MyOnTouchListener(holder, bean, position, mListener));
    }

    private class MyOnTouchListener implements View.OnTouchListener {

        private int downX = 0;
        private int downY = 0;
        private int upX = 0;
        private int upY = 0;
        private long downTime = 0;

        private ViewHolder holder;
        private ADBean item;
        private int position;
        private OnADItemClickListener listener;

        public MyOnTouchListener(ViewHolder holder, ADBean item, int position, OnADItemClickListener listener) {
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

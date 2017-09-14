package com.news.browser.ui.navigation.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.news.browser.R;
import com.news.browser.async.ImageDownloadTask;
import com.news.browser.bean.HotTagBean;
import com.news.browser.utils.DensityUtils;
import com.news.browser.utils.GlideUtils;
import com.news.browser.utils.Utils;
import com.zhy.adapter.recyclerview.base.ItemViewDelegate;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import rx.functions.Action1;


/**
 * Created by zy1584 on 2017-8-7.
 */

public class ItemDelegate implements ItemViewDelegate<HotTagBean.DataBean> {

    private boolean isEditable;
    private HotTagAdapter.OnSiteRemoveClickListener listener;

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public ItemDelegate(HotTagAdapter.OnSiteRemoveClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewLayoutId() {
        return R.layout.item_list_hot_tag;
    }

    @Override
    public boolean isForViewType(HotTagBean.DataBean item, int position) {
        if (item != null) {
            return true;
        }
        return false;
    }

    @Override
    public void convert(final ViewHolder holder, final HotTagBean.DataBean dataBean, final int position) {
        holder.setText(R.id.tv_name, dataBean.getName());
        final ImageView iv_icon = holder.getView(R.id.iv_icon);
        final ImageView iv_icon_full = holder.getView(R.id.iv_icon_full);
        final ImageView iv_circle_bg = holder.getView(R.id.iv_circle_bg);
        final Context context = holder.getConvertView().getContext();
        String iconUrl = dataBean.getIconUrl();
        if (!TextUtils.isEmpty(iconUrl)) {
            iv_circle_bg.setVisibility(View.GONE);
            iv_icon.setVisibility(View.GONE);
            GlideUtils.loadIconImage(context, dataBean.getIconUrl(), iv_icon_full);
        } else {
            if (dataBean.getBitmap() == null) {
                if (dataBean.getImageRes() != 0){
                    iv_circle_bg.setVisibility(View.GONE);
                    iv_icon.setVisibility(View.GONE);
                    iv_icon_full.setImageResource(R.drawable.icon_default);
                }else{
                    ImageDownloadTask.getIcon(context, dataBean.getAddrUrl())
                            .subscribe(new Action1<Bitmap>() {
                                @Override
                                public void call(Bitmap bitmap) {
                                    if (bitmap == null) {
                                        iv_circle_bg.setVisibility(View.GONE);
                                        iv_icon.setVisibility(View.GONE);
                                        iv_icon_full.setImageResource(R.drawable.icon_default);
                                        dataBean.setImageRes(R.drawable.icon_default);
                                    } else {
                                        iv_circle_bg.setVisibility(View.VISIBLE);
                                        iv_icon.setVisibility(View.VISIBLE);

                                        iv_icon.setImageBitmap(bitmap);
                                        dataBean.setBitmap(bitmap);

                                        Palette.Builder builder = Palette.from(bitmap);
                                        builder.generate(new Palette.PaletteAsyncListener() {

                                            @Override
                                            public void onGenerated(Palette palette) {
                                                //获取到充满活力的这种色调
                                                Palette.Swatch vibrant = palette.getVibrantSwatch();
                                                GradientDrawable gradientDrawable = new GradientDrawable();
                                                gradientDrawable.setColor(vibrant != null ? vibrant.getRgb() : Color.BLACK);
                                                gradientDrawable.setCornerRadius(DensityUtils.dpToPx(4));

                                                int size = (int) context.getResources().getDimension(R.dimen.hot_tag_icon_full_size);
                                                Bitmap b = Utils.convertToBitmap(gradientDrawable, size, size);
                                                iv_icon_full.setImageBitmap(b);
                                                dataBean.setBg_bitmap(b);
                                            }
                                        });
                                    }
                                }
                            });
                }
            } else {
                iv_icon.setVisibility(View.VISIBLE);
                iv_circle_bg.setVisibility(View.VISIBLE);
                iv_icon.setImageBitmap(dataBean.getBitmap());
                iv_icon_full.setImageBitmap(dataBean.getBg_bitmap());
            }
        }
        if (isEditable && dataBean.getIsErase() == 1) {
            holder.setVisible(R.id.iv_delete, true);
        } else {
            holder.setVisible(R.id.iv_delete, false);
        }
        holder.setOnClickListener(R.id.iv_delete, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRemoveClick(holder, dataBean, holder.getLayoutPosition());
            }
        });
    }
}

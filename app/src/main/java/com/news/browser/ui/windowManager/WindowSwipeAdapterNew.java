package com.news.browser.ui.windowManager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.ui.main.view.BrowserViewTitle;
import com.news.browser.utils.DensityUtils;

import java.util.ArrayList;


/**
 * Created by zy1584 on 2017-7-11.
 */

public class WindowSwipeAdapterNew extends RecyclerView.Adapter<WindowSwipeAdapterNew.MyViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<BrowserViewTitle> mData;
    private onSwipeDismissListener mDismissListener;

    public WindowSwipeAdapterNew(Context context, ArrayList<BrowserViewTitle> data) {
        inflater = LayoutInflater.from(context);
        mData = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(inflater.inflate(R.layout.item_list_tabs_new, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.tv_name.setText(mData.get(position).getTitle());
        holder.iv_shot.setImageBitmap(mData.get(position).getmShot());
        holder.iv_shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                mOnItemClickLitener.onItemClick(holder.itemView, pos);
            }
        });
        holder.iv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                mOnItemClickLitener.onItemRemove(holder.itemView, pos);
            }
        });
        if (position == 0){
            RelativeLayout itemView = (RelativeLayout) holder.itemView;
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            layoutParams.leftMargin = DensityUtils.dpToPx(72);
            layoutParams.rightMargin = 0;
            itemView.setLayoutParams(layoutParams);
        }else if (position == mData.size() - 1){
            RelativeLayout itemView = (RelativeLayout) holder.itemView;
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            layoutParams.leftMargin = 0;
            layoutParams.rightMargin = DensityUtils.dpToPx(72);
            itemView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name;
        ImageView iv_shot;
        ImageView iv_remove;
        public MyViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            iv_shot = (ImageView) itemView.findViewById(R.id.iv_shot);
            iv_remove = (ImageView) itemView.findViewById(R.id.iv_remove);
        }
    }

    public void removeData(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        if (mDismissListener != null){
            mDismissListener.onDismiss(position);
        }
    }

    interface onSwipeDismissListener{
        void onDismiss(int position);
    }

    public void setOnSwipeDismissListener(onSwipeDismissListener listener){
        this.mDismissListener = listener;
    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
        void onItemRemove(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}

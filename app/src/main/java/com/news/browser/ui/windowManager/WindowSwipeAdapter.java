package com.news.browser.ui.windowManager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.ui.main.view.LightningViewTitle;

import java.util.ArrayList;


/**
 * Created by zy1584 on 2017-7-11.
 */

public class WindowSwipeAdapter extends RecyclerView.Adapter<WindowSwipeAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<LightningViewTitle> mData;
    private onSwipeDismissListener mDismissListener;

    public WindowSwipeAdapter(Context context, ArrayList<LightningViewTitle> data) {
        inflater = LayoutInflater.from(context);
        mData = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(inflater.inflate(R.layout.item_list_tabs, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.tv_name.setText(mData.get(position).getTitle());
        holder.iv_shot.setImageBitmap(mData.get(position).getmShot());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                mOnItemClickLitener.onItemClick(holder.itemView, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name;
        ImageView iv_shot;
        public MyViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            iv_shot = (ImageView) itemView.findViewById(R.id.iv_shot);
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
        void onItemLongClick(View view , int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }
}

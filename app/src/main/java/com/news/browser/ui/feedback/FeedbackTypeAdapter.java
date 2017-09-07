package com.news.browser.ui.feedback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.news.browser.R;
import com.news.browser.bean.FeedbackTypeBean;
import com.news.browser.utils.UIUtils;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-14.
 */

public class FeedbackTypeAdapter extends BaseAdapter {


    private List<FeedbackTypeBean.TypelistBean> mTypeList;
    private LayoutInflater inflater;

    public FeedbackTypeAdapter(List<FeedbackTypeBean.TypelistBean> typeList, Context context) {
        this.mTypeList = typeList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mTypeList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.item_list_feedback_type, parent, false);
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            holder.iv_drop_down = (ImageView) convertView.findViewById(R.id.iv_drop_down);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        FeedbackTypeBean.TypelistBean bean = mTypeList.get(position);
        if (bean != null) {
            String typeName = bean.getTypeName();
            holder.textView.setText(typeName);
            holder.textView.setTextColor(position == 0 ? UIUtils.getColor(R.color.text_gray)
                    : UIUtils.getColor(R.color.text_black));
            holder.iv_drop_down.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        }
        return convertView;
    }

    class Holder {
        TextView textView;
        ImageView iv_drop_down;
    }
}

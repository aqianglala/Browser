package net.rdrei.android.dirchooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zy1584 on 2017-8-19.
 */

public class FileAdapter extends BaseAdapter{

    private List<String> mData;
    private LayoutInflater inflater;

    public FileAdapter(Context context, List<String> mData) {
        inflater = LayoutInflater.from(context);
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData.size();
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
        if (convertView == null){
            convertView = inflater.inflate(R.layout.item_list_folder, parent, false);
            holder = new Holder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }
        holder.tv_name.setText(mData.get(position));
        return convertView;
    }

    class Holder {
        TextView tv_name;
    }
}

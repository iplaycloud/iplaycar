package com.iplay.car.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.iplay.car.R;

import java.util.List;

/**
 * Created by Administrator on 2016/8/5.
 * 视频文件之日期页面的适配器
 */
public class VideoFileAdapterThree extends BaseAdapter {
    private Context context;
    private List<String> list;
    private LayoutInflater inflater;

    public VideoFileAdapterThree(Context context, List<String> list) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return null == list ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        VideoFileHolder holder;

        if (null == convertView) {
            holder = new VideoFileHolder();
            convertView = inflater.inflate(R.layout.item_videothree_list, null);
            holder.name = (TextView) convertView.findViewById(R.id.videoThree_tv);

            convertView.setTag(holder);

        } else {
            holder = (VideoFileHolder) convertView.getTag();
        }

//        holder.name.setText(list.get(position).getName());
        holder.name.setText(list.get(position));

        return convertView;
    }

    private class VideoFileHolder {
        private TextView name;
    }

}

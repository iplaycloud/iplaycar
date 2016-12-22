package com.iplay.car.common.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iplay.car.R;
import com.iplay.car.common.bean.VideoFile;
import com.iplay.car.common.utils.DataUtils;
import com.iplay.car.common.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/5.
 * 视频文件之视频文件信息页面的适配器
 */
public class VideoFileAdapterFour extends BaseAdapter {
    private Context context;
    private List<VideoFile> list;
    private Handler handler;
    private LayoutInflater inflater;
    private boolean isShow;// 是否显示选择按钮
    private List<Boolean> data;// 选择按钮的状态

    public VideoFileAdapterFour(Context context, List<VideoFile> list, Handler handler) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
        this.handler = handler;
        data = new ArrayList<Boolean>();
        for (int i = 0; i < (null == list ? 0 : list.size()); i++) {
            data.add(false);
        }
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
            convertView = inflater.inflate(R.layout.item_videofour_list, null);

            holder.check = (ImageView) convertView.findViewById(R.id.videoFour_check);
            holder.icon = (ImageView) convertView.findViewById(R.id.videoFour_iv);
            holder.name = (TextView) convertView.findViewById(R.id.videoFour_name);
            holder.size = (TextView) convertView.findViewById(R.id.videoFour_size);
            holder.date = (TextView) convertView.findViewById(R.id.videoFour_date);
            holder.delete = (ImageView) convertView.findViewById(R.id.videoFour_delete);
            holder.lock = (ImageView) convertView.findViewById(R.id.videoFour_lock);

            convertView.setTag(holder);

        } else {
            holder = (VideoFileHolder) convertView.getTag();
        }

        // 开启异步任务获取视频缩略图，并展示
//        new ImageAsyncTask(holder.icon).execute(list.get(position).getPath());

        holder.name.setText(list.get(position).getName());
        holder.size.setText(list.get(position).getSize() + "MB");
        holder.date.setText(list.get(position).getDate());

        if (list.get(position).isLok()) {
            holder.check.setVisibility(View.GONE);
            holder.delete.setImageResource(R.mipmap.video_undelete);
            holder.lock.setImageResource(R.drawable.selector_video_four_lock);
        } else {
            if (isShow) {
                holder.check.setVisibility(View.VISIBLE);
                holder.check.setSelected(data.get(position));
            } else {
                holder.check.setVisibility(View.GONE);
            }
            holder.delete.setImageResource(R.drawable.selector_video_four_delete);
            holder.lock.setImageResource(R.drawable.selector_video_four_unlock);
        }

        holder.check.setTag(position);
        holder.delete.setTag(position);
        holder.lock.setTag(position);

        holder.check.setOnClickListener(onClickListener);
        holder.delete.setOnClickListener(onClickListener);
        holder.lock.setOnClickListener(onClickListener);

        return convertView;
    }

    private class VideoFileHolder {
        private ImageView check;
        private ImageView icon;
        private TextView name;
        private TextView size;
        private TextView date;
        private ImageView delete;
        private ImageView lock;

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            switch (v.getId()) {

                case R.id.videoFour_check:
                    data.set(position, !data.get(position));
                    v.setSelected(data.get(position));
                    break;
                case R.id.videoFour_delete:
                    if (!list.get(position).isLok()) {
                        File file = new File(list.get(position).getPath());
                        file.delete();

                        list.remove(position);
                        data.remove(position);

                        notifyDataSetChanged();
                    }
                    if (data.size() < 1) {// 如果没有了数据，跳回上一层页面，日期页面
                        handler.sendEmptyMessage(0);
                    }
                    break;
                case R.id.videoFour_lock:

                    if (list.get(position).isLok()) {
                        toReplaceName(DataUtils.UNLOCK, position);

                    } else {

                        toReplaceName(DataUtils.LOCK, position);
                    }
                    list.get(position).setLok(!list.get(position).isLok());
                    notifyDataSetChanged();
                    break;


            }

        }
    };

    // 修改文件名，及数据
    private void toReplaceName(String str, int position) {
        String name = list.get(position).getName();
        name = name.replace(name.substring(0, str.length()), str);
        list.get(position).setName(name);// 修改名字

        String path = list.get(position).getPath();
        File file = new File(path);
        path = file.getParent() + "/" + name;
        list.get(position).setPath(path);// 修改路径

        file.renameTo(new File(path));// 修改文件
    }

    private class ImageAsyncTask extends AsyncTask<String, String, Bitmap> {
        private ImageView iv;

        private ImageAsyncTask(ImageView iv) {
            this.iv = iv;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            Bitmap bitmap = FileUtils.comp(path, 60, 60);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (null != bitmap) {
                iv.setImageBitmap(bitmap);
            }

        }
    }

    public List<Boolean> getData() {
        return data;
    }

    public void setData(List<Boolean> data) {
        this.data = data;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}

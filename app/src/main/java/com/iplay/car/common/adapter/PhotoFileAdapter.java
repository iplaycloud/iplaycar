package com.iplay.car.common.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iplay.car.R;
import com.iplay.car.common.bean.PhotoFileBean;
import com.iplay.car.common.utils.FileUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 * <p/>
 * 相册页面 --- 文件夹页面的GridView的适配器
 */
public class PhotoFileAdapter extends BaseAdapter {
    private Context context;
    private List<PhotoFileBean> list;
    private LayoutInflater layoutInflater;

    public PhotoFileAdapter(Context context, List<PhotoFileBean> list) {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
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

        PhotoFileHolder holder;
        if (convertView == null) {
            holder = new PhotoFileHolder();
            convertView = layoutInflater.inflate(R.layout.picture_gridview_layout, null);
            holder.three = (ImageView) convertView.findViewById(R.id.iv_picture_last);
            holder.two = (ImageView) convertView.findViewById(R.id.iv_picture_second);
            holder.one = (ImageView) convertView.findViewById(R.id.iv_picture_first);
            holder.date = (TextView) convertView.findViewById(R.id.tv_picture_date);

            convertView.setTag(holder);

        } else {
            holder = (PhotoFileHolder) convertView.getTag();
        }

        // 设置文件夹名
        holder.date.setText(list.get(position).getName());
        // 对应的文件路径
        List<String> path = list.get(position).getList();
        int size = path.size();
        if (size == 1) {
            // 只有一张图片，显示一张
            new ImageAsyncTask(holder.one).execute(path.get(0));
        } else if (size == 2) {
            // 有2张显示两张
            new ImageAsyncTask(holder.one).execute(path.get(0));
            new ImageAsyncTask(holder.two).execute(path.get(1));
        } else if (size >= 3) {
            // 3张以上显示3张
            new ImageAsyncTask(holder.one).execute(path.get(0));
            new ImageAsyncTask(holder.two).execute(path.get(1));
            new ImageAsyncTask(holder.three).execute(path.get(2));
        }
        return convertView;
    }

    class PhotoFileHolder {
        private ImageView three;
        private ImageView two;
        private ImageView one;
        private TextView date;
    }

    private class ImageAsyncTask extends AsyncTask<String, String, Bitmap> {
        private ImageView iv;

        private ImageAsyncTask(ImageView iv) {
            this.iv = iv;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            Bitmap bitmap = FileUtils.ratio(path, 160, 70);
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
}

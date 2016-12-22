package com.iplay.car.common.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.iplay.car.R;
import com.iplay.car.common.utils.FileUtils;
import com.iplay.car.common.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 * <p/>
 * 相册页面 --- 图片浏览页面的GridView的适配器
 */
public class PhotoFileAdapterTwo extends BaseAdapter {
    private Context context;
    private List<String> list;
    private LayoutInflater layoutInflater;
    private List<Boolean> data;// 存储图片是否选中的数据
    private boolean isShow;// 是否显示选择按钮
    private ImageLoader imageLoader;

    public PhotoFileAdapterTwo(Context context, List<String> list) {
        this.context = context;
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        data = new ArrayList<Boolean>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                data.add(false);
            }
        }
        this.imageLoader = ImageLoader.getInstance(2 , ImageLoader.Type.LIFO);
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public List<Boolean> getData() {
        return data;
    }

    public void setData(List<Boolean> data) {
        this.data = data;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        PhotoFileHolder holder;
        if (convertView == null) {
            holder = new PhotoFileHolder();
            convertView = layoutInflater.inflate(R.layout.picture_gridview_item_layout, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.iv_picture_item);
            holder.cb = (ImageView) convertView.findViewById(R.id.cb_picture_item);

            convertView.setTag(holder);

        } else {
            holder = (PhotoFileHolder) convertView.getTag();
        }

        // 对应的文件路径
        new ImageAsyncTask(holder.iv).execute(list.get(position));
//            imageLoader.loadImage(list.get(position),holder.iv);

        if (isShow) {
            // 显示选择按钮
            holder.cb.setVisibility(View.VISIBLE);
            holder.cb.setSelected(data.get(position));

            holder.cb.setTag(position);
            holder.cb.setOnClickListener(onClickListener);

        } else {
            // 隐藏选择按钮
            holder.cb.setVisibility(View.GONE);
        }

        return convertView;
    }

    class PhotoFileHolder {
        private ImageView iv;
        private ImageView cb;

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int position = (int) v.getTag();

            data.set(position, !data.get(position));
            v.setSelected(data.get(position));

        }
    };

    private class ImageAsyncTask extends AsyncTask<String, String, Bitmap> {
        private ImageView iv;

        private ImageAsyncTask(ImageView iv) {
            this.iv = iv;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String path = params[0];
            Bitmap bitmap = FileUtils.ratio(path, 210, 120);
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

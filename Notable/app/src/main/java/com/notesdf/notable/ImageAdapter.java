package com.notesdf.notable;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> arrayList;

    public ImageAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView ==  null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.image_list, parent, false);
            holder.imageView = convertView.findViewById(R.id.imageNotes);
            convertView.setTag(holder);
        }else{
            holder = (ImageAdapter.ViewHolder) convertView.getTag();
        }

        Glide.with(context).load(arrayList.get(position)).placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.imageView);
        return convertView;
    }

    // Belangrijk!!!!!!!!!
    // https://stackoverflow.com/questions/21501316/what-is-the-benefit-of-viewholder-pattern-in-android
    static class ViewHolder{
        public ImageView imageView;
    }
}

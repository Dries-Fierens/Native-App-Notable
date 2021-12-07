package com.notesdf.notable;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> arrayList;

    public GroupAdapter(Context context, ArrayList<String> arrayList) {
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.group_list, parent, false);
        }
        Button button;
        button = convertView.findViewById(R.id.group_button);
        button.setText(arrayList.get(position));
        return convertView;
    }

    @Override
    public String toString() {
        return "GroupAdapter{" +
                "context=" + context +
                ", arrayList=" + arrayList +
                '}';
    }
}

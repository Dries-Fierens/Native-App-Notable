package com.notesdf.notable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

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
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.group_list, parent, false);
            holder.button = convertView.findViewById(R.id.group_button);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        // http://www.migapro.com/click-events-listview-gridview/
        holder.button = convertView.findViewById(R.id.group_button);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("buttonText", holder.button.getText().toString());
                ChatRoomFragment chatRoomFragment = new ChatRoomFragment();
                chatRoomFragment.setArguments(bundle);
                ((NavigationHost) context).navigateTo(chatRoomFragment, true);
            }
        });
        holder.button.setText(arrayList.get(position));
        return convertView;
    }

    @Override
    public String toString() {
        return "GroupAdapter{" +
                "context=" + context +
                ", arrayList=" + arrayList +
                '}';
    }

    // Om de buttons te kunnen aan clicken!!!!!
    // https://stackoverflow.com/questions/34941919/what-is-best-way-to-implement-viewholder-design-pattern
    // https://stackoverflow.com/questions/13220657/android-gridview-button-click-handler
    static class ViewHolder{
        public Button button;
    }
}

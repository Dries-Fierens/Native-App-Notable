package com.notesdf.notable;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MessageHolder extends RecyclerView.ViewHolder {

    TextView mText;
    TextView mUsername;
    TextView mTime;
    ImageView imgProfile;

    public MessageHolder(View itemView) {
        super(itemView);
        mText = itemView.findViewById(R.id.message_text);
        mUsername = itemView.findViewById(R.id.message_user);
        mTime = itemView.findViewById(R.id.message_time);
        imgProfile = itemView.findViewById(R.id.imgDps);
    }
}


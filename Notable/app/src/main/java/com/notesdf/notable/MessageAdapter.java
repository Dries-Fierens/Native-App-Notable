package com.notesdf.notable;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;

public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageHolder> {
    private final String TAG = "MessageAdapter";
    Context context;
    String userId;
    StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();
    private final int MESSAGE_IN_VIEW_TYPE  = 1;
    private final int MESSAGE_OUT_VIEW_TYPE = 2;

    public MessageAdapter(@NonNull Context context, Query query, String userID) {
        super(new FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build());
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getMessageUserId().equals(userId)){
            return MESSAGE_OUT_VIEW_TYPE;
        }
        return MESSAGE_IN_VIEW_TYPE;
    }

    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType==MESSAGE_IN_VIEW_TYPE){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mssg, parent, false);
        }
        else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mssg_out, parent, false);
        }
        return new MessageHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message model) {
        final TextView mText = holder.mText;
        final TextView mUsername = holder.mUsername;
        final TextView mTime = holder.mTime;
        final ImageView imgProfile = holder.imgProfile;

        mUsername.setText(model.getMessageUser());
        mText.setText(model.getMessageText());
        mTime.setText(DateFormat.format("dd MMM  (h:mm a)", model.getMessageTime()));
        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(storageReference.child(model.getMessageUserId()))
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imgProfile);
    }
}

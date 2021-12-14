package com.notesdf.notable;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;

public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> {
    private final String TAG = "MessageAdapter";
    Context context;
    String userId;
    StorageReference storageReference;
    private RequestOptions requestOptions = new RequestOptions();
    private final int MESSAGE_IN_VIEW_TYPE  = 1;
    private final int MESSAGE_OUT_VIEW_TYPE = 2;

    public MessageAdapter(@NonNull Context context, Query query, String userID) {
        super(new FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build());
        this.context = context;
        this.userId = userID;
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getMessageUserId().equals(userId)){
            return MESSAGE_IN_VIEW_TYPE;
        }
        return MESSAGE_OUT_VIEW_TYPE;
    }

    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType==MESSAGE_IN_VIEW_TYPE){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_other, parent, false);
        }
        return new MessageHolder(view);
    }

    //Voordeel van Recyclerview is dat het automatisch een Viewholder heeft, dit heeft gridview niet automatisch
    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message model) {
        final TextView mText = holder.mText;
        final TextView mUsername = holder.mUsername;
        final TextView mTime = holder.mTime;
        final TextView mDate = holder.mDate;
        final ImageView imgProfile = holder.imgProfile;

        if(mUsername != null && imgProfile != null){
            mUsername.setText(model.getMessageUser());
            //Glide.with(context).setDefaultRequestOptions(requestOptions).load(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(imgProfile);
            //storageReference.child(model.getMessageUserId())
        }
        mText.setText(model.getMessageText());
        mDate.setText(DateFormat.format("dd MMM", model.getMessageTime()));
        mTime.setText(DateFormat.format("h:mm", model.getMessageTime()));

    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        TextView mText;
        TextView mUsername;
        TextView mTime;
        TextView mDate;
        ImageView imgProfile;

        public MessageHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.message_text);
            mUsername = itemView.findViewById(R.id.message_user);
            mTime = itemView.findViewById(R.id.message_time);
            mDate = itemView.findViewById(R.id.message_date);
            imgProfile = itemView.findViewById(R.id.imgDps);
        }
    }
}

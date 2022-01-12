package com.notesdf.notable;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        final ImageView mImage = holder.mImage;

        if(mUsername != null && imgProfile != null){
            mUsername.setText(model.getMessageUser());
            //Glide.with(context).setDefaultRequestOptions(requestOptions).load(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(imgProfile);
            //storageReference.child(model.getMessageUserId())
        }
        if(model.getMessageText().startsWith("https://firebasestorage.googleapis.com/v0/b/odisee-notable.appspot.com") && mImage != null){
            mImage.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
            Glide.with(context).load(Uri.parse(model.getMessageText())).placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(mImage);
            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString("image", model.getMessageText());
                    ImageCommentFragment imageCommentFragment = new ImageCommentFragment();
                    imageCommentFragment.setArguments(bundle);
                    // Ik navigeerde eerst naar een new imagedetailsfragment en dus werden de arguments verwijdert!!!
                    // https://stackoverflow.com/questions/14970790/fragment-getarguments-returns-null
                    ((NavigationHost) context).navigateTo(imageCommentFragment, true);
                }
            });
        }else{
            mImage.setVisibility(View.GONE);
            mText.setVisibility(View.VISIBLE);
            mText.setText(model.getMessageText());
        }
        mDate.setText(DateFormat.format("dd MMM", model.getMessageTime()));
        mTime.setText(DateFormat.format("kk:mm", model.getMessageTime()));
        //kk = 13, hh = 01 pm
    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        TextView mText;
        TextView mUsername;
        TextView mTime;
        TextView mDate;
        ImageView imgProfile;
        ImageView mImage;

        public MessageHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.message_text);
            mUsername = itemView.findViewById(R.id.message_user);
            mTime = itemView.findViewById(R.id.message_time);
            mDate = itemView.findViewById(R.id.message_date);
            imgProfile = itemView.findViewById(R.id.imgDps);
            mImage = itemView.findViewById(R.id.message_image);
        }
    }
}

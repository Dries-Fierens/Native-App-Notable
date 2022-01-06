package com.notesdf.notable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GroupAdapter extends BaseAdapter {
    private static final String TAG = "GroupAdapter";
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
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                db.collection("users").document(currentUser.getUid())
                        .collection("groups").whereEqualTo("groupName", holder.button.getText().toString())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                bundle.putParcelable("Group", document.toObject(Chatroom.class));
                            }
                            ChatRoomFragment chatRoomFragment = new ChatRoomFragment();
                            chatRoomFragment.setArguments(bundle);
                            ((NavigationHost) context).navigateTo(chatRoomFragment, true);
                        }else{
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
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

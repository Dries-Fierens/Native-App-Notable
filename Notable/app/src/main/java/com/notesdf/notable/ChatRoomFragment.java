package com.notesdf.notable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = "Notable:ChatRoom";
    private FirebaseFirestore db;
    private Query query;
    private FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatroom_fragment, container, false);
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        Button sendButton = view.findViewById(R.id.send_message);
        EditText input = view.findViewById(R.id.message_edit_text);
        RecyclerView chatRecyclerView = view.findViewById(R.id.chat_recyclerview);
        chatRecyclerView.setHasFixedSize(true);// noodzakelijk voor SetStackFromEnd
        String key = mAuth.getCurrentUser().getUid();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        String title = this.getArguments().getString("buttonText");
        toolbar.setTitle(title);

        // Voeg zeker een index toe voor de query want anders werkt recyclerview in realtime met firestore
        query = db.collection("messages").whereEqualTo("chatGroup", title).orderBy("messageTime");
        adapter = new MessageAdapter(getActivity(), query, key);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                chatRecyclerView.smoothScrollToPosition(itemCount - 1);
                //smoothScrollToPosition ipv scrollToPosition
            }
        });
        chatRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = input.getText().toString();
                if(!text.equals("")){
                    db.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()){
                                String firstName = documentSnapshot.get("firstname").toString();
                                db.collection("messages").add(new Message(firstName, text, key, title));
                            }else{
                                Log.w(TAG, "No such document");
                            }
                        }
                    });
                }
                input.setText("");
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}

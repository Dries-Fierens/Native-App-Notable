package com.notesdf.notable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = "Notable:ChatRoom";
    private FirebaseFirestore db;
    private Query query;
    private FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ArrayList<Message> messageList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED).build();
        db.setFirestoreSettings(settings);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatroom_fragment, container, false);
        MaterialToolbar topBar = view.findViewById(R.id.topAppBar);
        Button sendButton = view.findViewById(R.id.send_message);
        EditText input = view.findViewById(R.id.message_edit_text);
        RecyclerView chatRecyclerView = view.findViewById(R.id.chat_recyclerview);
        String email = mAuth.getCurrentUser().getEmail();
        String key = mAuth.getCurrentUser().getUid();
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        String title = this.getArguments().getString("buttonText");
        topBar.setTitle(title);

        query = db.collection("messages").orderBy("messageTime");
        adapter = new MessageAdapter(getActivity(), query, key);
        chatRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = input.getText().toString();
                db.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String firstName = documentSnapshot.get("firstname").toString();
                            db.collection("messages").add(new Message(firstName, text, key));
                        }else{
                            Log.w(TAG, "No such document");
                        }
                    }
                });
                input.setText("");
            }
        });

        return view;
    }
}

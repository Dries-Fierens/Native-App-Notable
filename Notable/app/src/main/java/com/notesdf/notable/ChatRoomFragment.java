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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = "Notable:ChatRoom";
    private FirebaseFirestore db;
    private Query query;
    private FirestoreRecyclerAdapter<Message, MessageHolder> adapter;
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

        //https://medium.com/@mendhie/building-real-time-android-chatroom-with-firebase-99a5b51cb4f7
        //gebruik geen document


        query = db.collection("messages").orderBy("messageTime");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    //pgBar.setVisibility(View.GONE);
                }
            }
        });
        adapter = new MessageAdapter(getActivity(), query, key);
        chatRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] names = getName(email);
                db.collection("messages").add(new Message(names[0], input.getText().toString(), key));
                input.setText("");
            }
        });

        return view;
    }

    private String[] getName(String email){
        String name = email.substring(0, email.indexOf("@"));
        String[] names = new String[2];
        if(!name.contains(".")){
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            names[0] = name;
            names[1] = "";
        }else{
            String firstName = email.substring(0, email.indexOf("."));
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
            String lastName = email.substring(email.indexOf(".") + 1, email.indexOf("@"));
            lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
            names[0] = firstName;
            names[1] = lastName;
        }
        return names;
    }
}

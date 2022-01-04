package com.notesdf.notable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = "Notable:ChatRoom";
    private FirebaseFirestore db;
    private Query query;
    private FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String key;
    private String title;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        key = currentUser.getUid();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // https://stackoverflow.com/questions/15653737/oncreateoptionsmenu-inside-fragments
        // setsupportactionbar & setHasOptionsMenu in oncreateview zijn ook noodzakelijk
        inflater.inflate(R.menu.chat_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatroom_fragment, container, false);
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        Button sendButton = view.findViewById(R.id.send_message);
        EditText input = view.findViewById(R.id.message_edit_text);
        RecyclerView chatRecyclerView = view.findViewById(R.id.chat_recyclerview);
        chatRecyclerView.setHasFixedSize(true);// noodzakelijk voor SetStackFromEnd
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        title = this.getArguments().getString("buttonText");
        toolbar.setTitle(title);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.dots));

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

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.add_image){
                    addImage();
                }else if(id == R.id.invite_user){
                    inviteUser();
                }else{
                    removeUser();
                }
                return true;
            }
        });

        return view;
    }

    private void addImage() {
        Log.w(TAG, "add image");
    }

    private void inviteUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter email van de gebruiker die je wil uitnodigen: ");
        final EditText emailField = new EditText(getActivity());
        emailField.setHint("bv. example@gmail.com");
        emailField.setWidth(100);
        builder.setView(emailField);

        builder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = emailField.getText().toString().trim();
                db.collection("users").document(key).collection("groups").whereEqualTo("groupName", title)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.w(TAG, "Input emailField: " + email);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                sendInvite(email, document);
                            }
                        }else{
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void removeUser() {
        Toast.makeText(getActivity(), "In progress", Toast.LENGTH_LONG).show();
    }

    public void sendInvite(String email, QueryDocumentSnapshot groupDocument){
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if (TextUtils.isEmpty(email)){
                        Toast.makeText(getActivity(), "Vul een email aub...", Toast.LENGTH_LONG).show();
                    }else if (!isValidEmailAddress(email)){
                        Log.w(TAG, "Ongeldige email");
                        Toast.makeText(getActivity(), "Ongeldige email", Toast.LENGTH_LONG).show();
                    }else{
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            String userId = document.getId();
                            Log.w(TAG, "onComplete sendInvite:" + groupDocument.toObject(Chatroom.class).toString());
                            db.collection("users").document(userId)
                                    .collection("invites")
                                    .add(new Invite(groupDocument.toObject(Chatroom.class)));
                        }
                    }
                }else{
                    Toast.makeText(getActivity(), "Kan gebruiker niet vinden", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private static boolean isValidEmailAddress(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
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

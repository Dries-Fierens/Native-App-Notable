package com.notesdf.notable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class ImageDetailsFragment extends Fragment {

    private static final String TAG = "Notable:ImageDetails";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String key;
    private ArrayList<Chatroom> chatroomList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        key = currentUser.getUid();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.imagedetails_fragment, container, false);
        BottomNavigationView bottomNav = view.findViewById(R.id.bottomAppBar);
        ImageView fullscreenImage = view.findViewById(R.id.fullscreen_image);

        String data = this.getArguments().getString("image");
        Uri imageUri = Uri.parse(data);
        //https://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
        //https://stackoverflow.com/questions/8232608/fit-image-into-imageview-keep-aspect-ratio-and-then-resize-imageview-to-image-d
        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(fullscreenImage);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.share_image) {
                    UploadImage(data);
                } else  {
                    DeleteImage();
                }
                return true;
            }
        });

        return view;
    }

    private void UploadImage(String uri){
        Log.w(TAG, "UploadImage: " + uri);
        db.collection("users").document(key).collection("groups").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomList = new ArrayList<>();
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        chatroomList.add(document.toObject(Chatroom.class));
                    }
                    if(chatroomList != null){
                        CharSequence[] options = new CharSequence[chatroomList.size() + 1];
                        for (int i = 0; i <= chatroomList.size() - 1; i++) {
                            options[i] = chatroomList.get(i).getGroupName();
                        }
                        options[chatroomList.size()] = "Cancel";
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("To which group?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(options[i].equals("Cancel")){
                                    dialogInterface.dismiss();
                                }else{
                                    Chatroom chatroom = chatroomList.get(i);
                                    Log.w(TAG, "onClick chatroom: " + chatroom);
                                    db.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()){
                                                String firstName = documentSnapshot.get("firstname").toString();
                                                db.collection("messages").add(new Message(firstName, uri, currentUser.getUid(), chatroom.getGroupName(), chatroom.getAdminId()));
                                                Toast.makeText(getActivity(), "Image succesfully uploaded to group", Toast.LENGTH_LONG).show();
                                            }else{
                                                Log.w(TAG, "No such document");
                                                Toast.makeText(getActivity(), "Image upload failed", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        builder.show();
                    }else{
                        chatroomList = new ArrayList<>();
                        Toast.makeText(getActivity(), "Je hebt geen chatgroepen", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Log.w(TAG, "Empty query snapshot");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error getting query snapshot: " + e.getMessage());
            }
        });
    }

    private void DeleteImage() {
    }
}

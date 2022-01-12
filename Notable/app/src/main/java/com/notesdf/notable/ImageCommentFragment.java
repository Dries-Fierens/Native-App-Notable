package com.notesdf.notable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ImageCommentFragment extends Fragment {
    private static final String TAG = "Notable:ImageCommentary";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String key;
    private ArrayList<TextView> comments;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        key = currentUser.getUid();
    }

    //De ontouch is nu niet voor blinde mensen
    //https://stackoverflow.com/questions/47107105/android-button-has-setontouchlistener-called-on-it-but-does-not-override-perform
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.imagecomment_fragment, container, false);
        ImageView fullscreenImage = view.findViewById(R.id.fullscreen_image);

        String data = this.getArguments().getString("image");
        Uri imageUri = Uri.parse(data);
        //https://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
        //https://stackoverflow.com/questions/8232608/fit-image-into-imageview-keep-aspect-ratio-and-then-resize-imageview-to-image-d
        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(fullscreenImage);



        fullscreenImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    showDialog(x, y, data);
                }
                return false;
            }
        });

        return view;
    }

    private void showDialog(float x, float y, String image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter jouw opmerking: ");
        final EditText commentField = new EditText(getActivity());
        commentField.setHint("bv. Wat betekent dit?");
        commentField.setWidth(100);
        builder.setView(commentField);

        builder.setPositiveButton("Comment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String comment = commentField.getText().toString();
                Toast.makeText(getActivity(), comment, Toast.LENGTH_SHORT).show();
                db.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String firstName = documentSnapshot.get("firstname").toString();
                            db.collection("comments").add(new Comment(firstName, comment, key, x, y, image));
                        }else{
                            Log.w(TAG, "No such document");
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
}

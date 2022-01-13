package com.notesdf.notable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ImageCommentFragment extends Fragment {
    private static final String TAG = "Notable:ImageCommentary";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String key;
    private ArrayList<Comment> comments = new ArrayList<>();
    private String image;
    private String adminId;
    private String chatGroup;
    private RelativeLayout layout;

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
        layout = view.findViewById(R.id.comments);

        image = this.getArguments().getString("image");
        adminId = this.getArguments().getString("adminId");
        chatGroup = this.getArguments().getString("chatGroup");
        Uri imageUri = Uri.parse(image);
        //https://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
        //https://stackoverflow.com/questions/8232608/fit-image-into-imageview-keep-aspect-ratio-and-then-resize-imageview-to-image-d
        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(fullscreenImage);

        displayComments(layout);

        fullscreenImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    showDialog(x, y, image);
                }
                return false;
            }
        });

        return view;
    }

    private void displayComments(RelativeLayout layout) {
        comments = new ArrayList<>();
        db.collection("comments").whereEqualTo("image", image).whereEqualTo("adminId", adminId).orderBy("commentTime").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    comments.add(document.toObject(Comment.class));
                }
                for (int i = 0; i <= comments.size() - 1; i++){
                    float x = comments.get(i).getX();
                    float y = comments.get(i).getY();
                    TextView textView = new TextView(getActivity());
                    textView.setId(View.generateViewId());
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(Integer.toString(i + 1));
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundResource(R.drawable.comment_background);
                    GradientDrawable drawable = (GradientDrawable) textView.getBackground();
                    drawable.setColor(Color.parseColor("#00C2FF"));
                    Log.w(TAG, "X: " + x + ", Y: " + y);
                    textView.setTranslationX(x);
                    textView.setTranslationY(y);
                    String comment = comments.get(i).getCommentText();
                    String name = comments.get(i).getCommentUser();
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenu popupMenu = new PopupMenu(getActivity(), textView);
                            popupMenu.getMenu().add(name + ": " + comment);
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    Toast.makeText(getActivity(), menuItem.getTitle(), Toast.LENGTH_LONG).show();
                                    return true;
                                }
                            });
                            popupMenu.show();
                        }
                    });
                    layout.addView(textView);
                }
            }
        });
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
                db.collection("users").document(key).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String firstName = documentSnapshot.get("firstname").toString();
                            db.collection("comments").add(new Comment(firstName, comment, key, x, y, image, adminId, chatGroup));
                            displayComments(layout);
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

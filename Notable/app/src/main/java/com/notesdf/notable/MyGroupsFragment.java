package com.notesdf.notable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;

public class MyGroupsFragment extends Fragment {

    private static final String TAG = "Notable:MyGroups";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressBar mProgressCircle;
    private GridView gridView;
    private String key;
    private GroupAdapter adapter;
    //https://firebase.google.com/docs/firestore/query-data/get-data#source_options
    private Source source = Source.CACHE;
    FirebaseFirestore db;
    ArrayList<String> groupList = new ArrayList<>();

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment, container, false);
        BottomNavigationView bottomNav = view.findViewById(R.id.bottomAppBar);
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        mProgressCircle = view.findViewById(R.id.progress_circle);
        key = mAuth.getCurrentUser().getUid();

        gridView = view.findViewById(R.id.groups_gridview);
        getGroups();
        db.collection("groups").document(key).get(source).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    groupList = (ArrayList<String>) documentSnapshot.get("group_names");
                    Log.w(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                    adapter = new GroupAdapter(getActivity(), groupList);
                    Log.w(TAG, adapter.toString());
                    //https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it
                    //nullpointException wordt gegeven omdat de gridview null was niet de adapter en er dus een methode werdt op opgeroepen.
                    gridView.setAdapter(adapter);
                    mProgressCircle.setVisibility(View.INVISIBLE);
                } else {
                    Log.w(TAG, "No such document");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error getting documents: " + e.getMessage());
            }
        });

        // getHeight wordt 0 omdat de view nog niet gesized is en op het scherm wordt getoond, dus deze methode verhelpt dit.
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            @Override
            public boolean onPreDraw()
            {
                // zorgt ervoor dat het maar 1 keer wordt uitgevoerd
                if (view.getViewTreeObserver().isAlive())
                    view.getViewTreeObserver().removeOnPreDrawListener(this);

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) gridView.getLayoutParams();
                params.setMargins(0, topAppBar.getHeight(), 0, bottomNav.getHeight());
                gridView.setLayoutParams(params);

                return true;
            }
        });

        bottomNav.setSelectedItemId(R.id.groups);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.notes) {
                    ((NavigationHost) getActivity()).navigateTo(new MyNotesFragment(), false);
                } else {
                    ((NavigationHost) getActivity()).navigateTo(new MyGroupsFragment(), false);
                }
                return true;
            }
        });

        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.settings){
                    ((NavigationHost) getActivity()).navigateTo(new SettingsFragment(), true);
                }else{
                    RequestNewGroup();
                }
                return true;
            }
        });

        return view;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter groep naam: ");
        final EditText groupNameField = new EditText(getActivity());
        groupNameField.setHint("bv. School groep");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(getActivity(), "Please write Group Name...", Toast.LENGTH_LONG);
                }else{
                    CreateNewGroup(groupName);
                }
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

    private void CreateNewGroup(String groupName) {
        HashMap<String, ArrayList<String>> groupData = new HashMap<>();
        groupList.add(groupName);
        groupData.put("group_names", groupList);
        db.collection("groups").document(key).set(groupData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.w(TAG, "New group added successfully to Firestore");
                ((NavigationHost) getActivity()).navigateTo(new MyGroupsFragment(), false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed adding new group to firestore", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getGroups(){
        db.collection("groups").document(key).get(source).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    groupList = (ArrayList<String>) documentSnapshot.get("group_names");
                } else {
                    Log.w(TAG, "No such document");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error getting documents: " + e.getMessage());
            }
        });
    }
}

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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class MyGroupsFragment extends Fragment {

    private static final String TAG = "Notable:MyGroups";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressBar mProgressCircle;
    FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment, container, false);
        BottomNavigationView BottomNav = view.findViewById(R.id.bottomAppBar);
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        mProgressCircle = view.findViewById(R.id.progress_circle);

        BottomNav.setSelectedItemId(R.id.groups);
        BottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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

    //https://www.youtube.com/watch?v=sgMO1AbUJmA

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
        //https://www.youtube.com/watch?v=eizfx5lRE4M&list=PLxefhmF0pcPmtdoud8f64EpgapkclCllj&index=12
        //https://youtu.be/sgMO1AbUJmA?t=624
    }
}

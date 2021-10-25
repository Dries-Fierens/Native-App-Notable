package com.example.notable;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyNotesFragment extends Fragment {

    private FirebaseStorage storage;
    private StorageReference mStorageRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notes_fragment, container, false);
        BottomNavigationView BottomNav = view.findViewById(R.id.bottomAppBar);
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);

        BottomNav.setSelectedItemId(R.id.notes);
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
                    /*Camera openen*/
                    askCameraPermissions();

                    //Gebruik zeker deze video voor ingelogd te blijven!!!
                    //https://www.youtube.com/watch?v=gD9uQf5UU-g
                }
                return true;
            }
        });

        return view;
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 101);
        }else{
            //openCamera();
            //https://stackoverflow.com/questions/66551781/android-onrequestpermissionsresult-is-deprecated-are-there-any-alternatives
        }
    }
}

package com.example.notable;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyNotesFragment extends Fragment {

    private static final String TAG = "Notable:MyNotesFragment";
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
                    mPermissionResult.launch(Manifest.permission.CAMERA);
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
            openCamera();
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launchSomeActivity.launch(camera);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap image = (Bitmap) data.getExtras().get("data");

                    }
                }
            });

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result) {
                        Log.w(TAG, "onActivityResult: PERMISSION GRANTED");
                    } else {
                        Log.w(TAG, "onActivityResult: PERMISSION DENIED");
                    }
                }
            });
}

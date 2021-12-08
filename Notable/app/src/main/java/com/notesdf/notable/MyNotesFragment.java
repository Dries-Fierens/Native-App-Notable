package com.notesdf.notable;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
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
import android.view.ViewTreeObserver;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyNotesFragment extends Fragment {

    private static final String TAG = "Notable:MyNotesFragment";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private StorageReference mUserRef;
    private Uri mImageUri;
    private GridView gridView;
    private ImageAdapter adpter;
    private ProgressBar mProgressCircle;
    ArrayList<String> imageList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notes_fragment, container, false);
        BottomNavigationView bottomNav = view.findViewById(R.id.bottomAppBar);
        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        bottomNav.setSelectedItemId(R.id.notes);
        mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);

        currentUser = mAuth.getCurrentUser();
        mUserRef = storage.getReference().child(currentUser.getEmail());
        gridView = view.findViewById(R.id.notes_gridview);
        mUserRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                imageList = new ArrayList<>();
                for(StorageReference file:listResult.getItems()){
                    Log.w(TAG, file.toString());
                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageList.add(uri.toString());
                            Log.w(TAG,"URI: " + uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            adpter = new ImageAdapter(getActivity(), imageList);
                            gridView.setAdapter(adpter);
                            mProgressCircle.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("image", adpter.getItem(position).toString());
                ImageDetailsFragment imageDetailsFragment = new ImageDetailsFragment();
                imageDetailsFragment.setArguments(bundle);
                // Ik navigeerde eerst naar een new imagedetailsfragment en dus werden de arguments verwijdert!!!
                // https://stackoverflow.com/questions/14970790/fragment-getarguments-returns-null
                ((NavigationHost) getActivity()).navigateTo(imageDetailsFragment, true);
            }
        });

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
                    selectImage();
                }
                return true;
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

        return view;
    }

    private void selectImage() {
        final CharSequence[] options = { "Camera", "Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Camera"))
                {
                    mPermissionResult.launch(Manifest.permission.CAMERA);
                    askCameraPermissions();
                }
                else if (options[item].equals("Gallery"))
                {
                    mPermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    askGalleryPermissions();
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 101);
        }else{
            openCamera();
        }
    }

    private void askGalleryPermissions() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else{
            openGallery();
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        launchCameraActivity.launch(camera);
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launchGalleryActivity.launch(gallery);
    }

    ActivityResultLauncher<Intent> launchGalleryActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                Toast.makeText(getActivity(), "Your image is being uploaded", Toast.LENGTH_LONG).show();
                Intent data = result.getData();
                //de intent heeft dit keer een uri door EXTERNAL_CONTENT_URI dus dit kan opgeroepen worden door nog een keer getdata() te doen.
                mImageUri = data.getData();
                //compressen zodat het minder bytes inneemt op firebase en dan sneller inlaadt
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri);
                    mImageUri = getImageUri(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String timestamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss", Locale.getDefault()).format(new Date());
                Log.w(TAG, "Add image, uri: " + mImageUri);
                String imageFileName = "JPEG" + timestamp + "." + getFileExt(mImageUri);
                StorageReference imageRef = mStorageRef.child(currentUser.getEmail()).child(imageFileName);
                Log.w(TAG, "Add image, uri: " + imageFileName);
                imageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.w(TAG, "onSuccess: Uploaded Image URL is " + uri.toString());
                            }
                        });
                        ((NavigationHost) getActivity()).navigateTo(new MyNotesFragment(), false);
                        Toast.makeText(getActivity(), "Image is uploaded", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });

    ActivityResultLauncher<Intent> launchCameraActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                Bitmap image = (Bitmap) data.getExtras().get("data");
                mImageUri = getImageUri(image);
                String timestamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss", Locale.getDefault()).format(new Date());
                Log.w(TAG, "Add image, uri: " + mImageUri);
                String imageFileName = "JPEG" + timestamp + "." + getFileExt(mImageUri);
                StorageReference imageRef = mStorageRef.child(currentUser.getEmail()).child(imageFileName);
                Log.w(TAG, "Add image, uri: " + imageFileName);
                imageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.w(TAG, "onSuccess: Uploaded Image URL is " + uri.toString());
                            }
                        });
                        ((NavigationHost) getActivity()).navigateTo(new MyNotesFragment(), false);
                        Toast.makeText(getActivity(), "Image successfully captured", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result) {
                        Log.w(TAG, "permissionResult: PERMISSION GRANTED");
                    } else {
                        Log.w(TAG, "permissionResult: PERMISSION DENIED");
                    }
                }
            });

    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // of 0
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}

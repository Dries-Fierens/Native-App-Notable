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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;

public class MyGroupsFragment extends Fragment {

    private static final String TAG = "Notable:MyGroups";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressBar mProgressCircle;
    private GridView gridView;
    private String key;
    private GroupAdapter adapter;
    private FirebaseFirestore db;
    private ArrayList<String> chatroomList = new ArrayList<>();
    private ArrayList<String> users = new ArrayList<>();
    private ArrayList<String> admin = new ArrayList<>();
    private ArrayList<Invite> invites;

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
        key = currentUser.getUid();
        admin.add(key);

        gridView = view.findViewById(R.id.groups_gridview);
        // Online
        db.collection("users").document(key).collection("groups").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomList = new ArrayList<>();
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        chatroomList.add(document.toObject(Chatroom.class).getGroupName());
                    }
                    if(chatroomList != null){
                        Collections.sort(chatroomList);
                        adapter = new GroupAdapter(getActivity(), chatroomList);
                        Log.w(TAG, adapter.toString());
                        //https://stackoverflow.com/questions/218384/what-is-a-nullpointerexception-and-how-do-i-fix-it
                        //nullpointException wordt gegeven omdat de gridview null was niet de adapter en er dus een methode werdt op opgeroepen.
                        gridView.setAdapter(adapter);
                        mProgressCircle.setVisibility(View.INVISIBLE);
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

        // Offline cache ophalen zodat de groepen sneller inladen
        setOfflineGroups();

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
                    selectGroupOption();
                }
                return true;
            }
        });

        return view;
    }

    private void selectGroupOption() {
        final CharSequence[] options = { "Create new group", "Invites","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add group");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Create new group"))
                {
                    requestNewGroup();
                }
                else if (options[item].equals("Invites"))
                {
                    showInvites();
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void showInvites() {
        ArrayList<String> documentIds = new ArrayList<>();
        Query invitations = db.collection("users").document(key).collection("invites")
                .whereEqualTo("receiver", currentUser.getEmail()).whereEqualTo("accepted", false);
        invitations.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    invites = new ArrayList<>();
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        documentIds.add(document.getId());
                        invites.add(document.toObject(Invite.class));
                    }
                    Log.w(TAG, "showInvites: " + invites);
                    clickInvite(documentIds);
                }else{
                    Log.w(TAG, "Empty query snapshot");
                }
            }
        });
    }

    private void clickInvite(ArrayList<String> documentIds) {
        CharSequence[] options = new CharSequence[invites.size() + 1];
        for (int i = 0; i <= invites.size() - 1; i++) {
            options[i] = invites.get(i).getChatroom().getGroupName();
        }
        options[invites.size()] = "Cancel";
        AlertDialog.Builder invitesBuilder = new AlertDialog.Builder(getActivity());
        invitesBuilder.setTitle("Invites");
        invitesBuilder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(options[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }else{
                    Invite accepted = invites.get(i);
                    accepted.setAccepted(true);
                    Chatroom joined = accepted.getChatroom();
                    users = joined.getUsers();
                    users.add(currentUser.getUid());
                    joined.setUsers(users);
                    accepted.setChatroom(joined);
                    Log.w(TAG, "onClick invite: " + accepted);
                    db.collection("users").document(key)
                            .collection("invites").document(documentIds.get(i)).set(accepted);
                    db.collection("users").document(joined.getAdminId())
                            .collection("groups").document(accepted.getDocumentId()).set(joined);
                    db.collection("users").document(key)
                            .collection("groups").add(joined);
                }
            }
        });
        invitesBuilder.show();
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter groepsnaam: ");
        final EditText groupNameField = new EditText(getActivity());
        groupNameField.setHint("bv. Schoolgroep");
        groupNameField.setWidth(100);
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();
                Boolean exist = false;
                for (String chatroom : chatroomList) {
                    if(chatroom.equals(groupName)){
                        exist = true;
                        break;
                    }
                }
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(getActivity(), "Schrijf een groepsnaam AUB...", Toast.LENGTH_LONG).show();
                }else if (groupName.length() > 20){
                    Toast.makeText(getActivity(), "Groepsnaam is te lang", Toast.LENGTH_LONG).show();
                }else if (exist){
                    Toast.makeText(getActivity(), "Je hebt deze groep al toegevoegd", Toast.LENGTH_LONG).show();
                }else{
                    createNewGroup(groupName);
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

    private void createNewGroup(String groupName) {
        //HashMap<String, ArrayList<String>> groupData = new HashMap<>();
        //chatroomList.add(groupName);
        //groupData.put("chat_rooms", chatroomList);

        db.collection("users").document(key).collection("groups").add(new Chatroom(key, groupName, admin)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(@NonNull DocumentReference documentReference) {
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

    private void setOfflineGroups(){
        //https://firebase.google.com/docs/firestore/query-data/get-data#source_options
        Source source = Source.CACHE; // moet pas gebruikt worden als we offline zijn!!!
        db.collection("users").document(key).collection("groups").get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                chatroomList = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        chatroomList.add(document.toObject(Chatroom.class).getGroupName());
                    }
                    Collections.sort(chatroomList);
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
}

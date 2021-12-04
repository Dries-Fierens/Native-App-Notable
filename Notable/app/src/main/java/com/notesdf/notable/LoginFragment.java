package com.notesdf.notable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment {

    private static final String TAG = "Notable:LoginFragment";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.Oauth_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
        checkUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        TextInputLayout passwordTextInput = view.findViewById(R.id.passwordLogin_text_input);
        TextInputEditText emailEditText = view.findViewById(R.id.emailLogin_edit_text);
        TextInputEditText passwordEditText = view.findViewById(R.id.passwordLogin_edit_text);
        MaterialButton nextButton = view.findViewById(R.id.login_button);
        TextView no_accountText = view.findViewById(R.id.no_accountText);
        SignInButton googleButton = view.findViewById(R.id.google_sign_in_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPasswordValid(passwordEditText.getText().toString().trim())) {
                    passwordTextInput.setError(getString(R.string.error_password));
                } else {
                    passwordTextInput.setError(null); // Clear the error
                    signIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
                }
            }
        });

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText().toString().trim())) {
                    passwordTextInput.setError(null); //Clear the error
                }
                return false;
            }
        });

        no_accountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationHost) getActivity()).navigateTo(new RegistrationFragment(), true);
            }
        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        return view;
    }

    /* Methods/functions */

    private void checkUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
            Log.w(TAG, "checkUser: Already logged in");
            ((NavigationHost) getActivity()).navigateTo(new MyNotesFragment(), false);
        }
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        launchSomeActivity.launch(signInIntent);
    }

    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }
                }
            });

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.w(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addFirestoreUser();
                            Log.w(TAG, "signInWithGoogle:success", task.getException());
                            ((NavigationHost) getActivity()).navigateTo(new MyNotesFragment(), false);
                        } else {
                            Log.w(TAG, "signInWithGoogle:failure", task.getException());
                            Toast.makeText(getActivity(), "Login failed, try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // werkt niet met de campusroam of extended wifi, gebruik hotspot 4g van gsm of originele wifi!!!!!!!
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addFirestoreUser();
                            Log.w(TAG, "signInWithEmail:success", task.getException());
                            ((NavigationHost) getActivity()).navigateTo(new MyNotesFragment(), false); // Navigate to the next Fragment
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Login failed, try again later", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean isPasswordValid(@Nullable String text) {
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(text);
        return text != null && text.length() >= 8 && matcher.matches();
    }

    private void addFirestoreUser(){
        String key = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        HashMap<String, String> userData = new HashMap<>();
        String[] names = getName(email);
        userData.put("firstname", names[0]);
        userData.put("lastname", names[1]);
        userData.put("email", email);
        HashMap<String, Object> update = new HashMap<>();
        update.put(key, userData);
        db.collection("users").document(key).set(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.w(TAG, "User added successfully to Firestore");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed adding user to firestore", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String[] getName(String email){
        String name = email.substring(0, email.indexOf("@"));
        String[] names = new String[2];
        if(!name.contains(".")){
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            names[0] = name;
            names[1] = "";
        }else{
            String firstName = email.substring(0, email.indexOf("."));
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
            String lastName = email.substring(email.indexOf(".") + 1, email.indexOf("@"));
            lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
            names[0] = firstName;
            names[1] = lastName;
        }
        return names;
    }
}

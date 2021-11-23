package com.notesdf.notable;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationFragment extends Fragment {

    private static final String TAG = "Notable";
    private FirebaseAuth mAuth;

    public RegistrationFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_fragment, container, false);

        TextInputLayout passwordTextInput = view.findViewById(R.id.passwordRegistration_text_input);
        TextInputEditText emailEditText = view.findViewById(R.id.emailRegistration_edit_text);
        TextInputEditText passwordEditText = view.findViewById(R.id.passwordRegistration_edit_text);
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        MaterialButton registerButton = view.findViewById(R.id.register_button);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationHost) getActivity()).navigateTo(new LoginFragment(), false);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPasswordValid(passwordEditText.getText())) {
                    passwordTextInput.setError(getString(R.string.error_password));
                } else {
                    passwordTextInput.setError(null);
                    createAccount(emailEditText.getText().toString(), passwordEditText.getText().toString());
                }
            }
        });

        return view;
    }

    // Campusroam wifi werkt niet voor de emulator dus verbindt via hotspot 4G
    //bekijk ook altijd de run error als je app crasht!!!!
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.w(TAG, "createAccount:success", task.getException());
                            ((NavigationHost) getActivity()).navigateTo(new LoginFragment(), false);
                        } else {
                            Log.w(TAG, "createAccount:failure", task.getException());
                            Toast.makeText(getActivity(), "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isPasswordValid(@Nullable Editable text) {
        return text != null && text.length() >= 8;
    }
}

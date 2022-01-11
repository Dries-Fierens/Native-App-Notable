package com.notesdf.notable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.regex.Pattern;

public class ContactUsFragment extends Fragment {

    private static final String TAG = "Notable:ContactUs";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment, container, false);
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        MaterialButton sendButton = view.findViewById(R.id.send_complaint_button);
        EditText contactEmailEditText = view.findViewById(R.id.contactEmail_edit_text);
        EditText subjectEditText = view.findViewById(R.id.subject_edit_text);
        EditText descriptionEditText = view.findViewById(R.id.textarea_edit_text);
        TextInputLayout contactEmailTextInput = view.findViewById(R.id.contactEmail_textInput);
        TextInputLayout subjectTextInput = view.findViewById(R.id.subject_textInput);
        TextInputLayout textareaTextInput = view.findViewById(R.id.textarea_textInput);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean send = true;
                if (!isValidEmailAddress(contactEmailEditText.getText().toString().replaceAll("\\s+",""))) {
                    contactEmailTextInput.setError(getString(R.string.error_email));
                    send = false;
                } else {
                    contactEmailTextInput.setError(null); // Clear the error
                }
                if (subjectEditText.getText().toString().replaceAll("\\s+","").equals("")) {
                    subjectTextInput.setError(getString(R.string.error_subject));
                    send = false;
                } else {
                    subjectTextInput.setError(null); // Clear the error
                }
                if (descriptionEditText.getText().toString().replaceAll("\\s+","").equals("")) {
                    textareaTextInput.setError(getString(R.string.error_description));
                    send = false;
                } else {
                    textareaTextInput.setError(null); // Clear the error
                }

                if (send){
                    Complaint c = new Complaint(contactEmailEditText.getText().toString().replaceAll("\\s+",""),
                            subjectEditText.getText().toString().replaceAll("\\s+",""),
                            descriptionEditText.getText().toString(), currentUser.getUid());
                    db.collection("complaints").add(c);
                    contactEmailEditText.setText("");
                    subjectEditText.setText("");
                    descriptionEditText.setText("");
                    Toast.makeText(getActivity(), "Your message has been received and we will contact you shortly", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public static boolean isValidEmailAddress(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}


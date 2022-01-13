package com.notesdf.notable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private static final String TAG = "Notable:Settings";
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        MaterialButton logoutButton = view.findViewById(R.id.logout_button);
        MaterialButton privacyButton = view.findViewById(R.id.privacy_button);
        MaterialButton termsButton = view.findViewById(R.id.termsConditions_button);
        MaterialButton contactButton = view.findViewById(R.id.contactUs_button);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.Oauth_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "signOut:success");
                mGoogleSignInClient.signOut();
                FirebaseAuth.getInstance().signOut();
                ((NavigationHost) getActivity()).navigateTo(new LoginFragment(), false);
            }
        });

        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pages.flycricket.io/notable/privacy.html"));
                //alleen startActivityForResult is depecrated
                startActivity(browserIntent);
            }
        });

        termsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pages.flycricket.io/notable/terms.html"));
                startActivity(browserIntent);
            }
        });

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationHost) getActivity()).navigateTo(new ContactUsFragment(), true);
            }
        });

        return view;
    }
}

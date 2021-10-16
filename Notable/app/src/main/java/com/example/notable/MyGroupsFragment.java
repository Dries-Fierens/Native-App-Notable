package com.example.notable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MyGroupsFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.groups_fragment, container, false);
        BottomNavigationView BottomNav = view.findViewById(R.id.bottomAppBar);
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
        return view;
    }
}

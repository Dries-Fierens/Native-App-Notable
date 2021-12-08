package com.notesdf.notable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

public class ChatRoomFragment extends Fragment {
    private static final String TAG = "Notable:ChatRoom";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatroom_fragment, container, false);
        MaterialToolbar topBar = view.findViewById(R.id.topAppBar);

        String data = this.getArguments().getString("buttonText");
        topBar.setTitle(data);

        return view;
    }
}

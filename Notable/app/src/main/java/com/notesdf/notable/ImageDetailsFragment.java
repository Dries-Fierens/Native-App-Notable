package com.notesdf.notable;

import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ImageDetailsFragment extends Fragment {

    private static final String TAG = "Notable:ImageDetails";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.imagedetails_fragment, container, false);

        ImageView fullscreenImage = view.findViewById(R.id.fullscreen_image);

        String data = this.getArguments().getString("image");
        Uri imageUri = Uri.parse(data);
        Glide.with(getActivity()).load(imageUri).placeholder(R.drawable.placeholder).diskCacheStrategy(DiskCacheStrategy.DATA).into(fullscreenImage);

        //https://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
        //https://stackoverflow.com/questions/8232608/fit-image-into-imageview-keep-aspect-ratio-and-then-resize-imageview-to-image-d

        return view;
    }
}

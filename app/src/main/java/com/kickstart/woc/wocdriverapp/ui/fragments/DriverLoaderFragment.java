package com.kickstart.woc.wocdriverapp.ui.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.kickstart.woc.wocdriverapp.R;

public class DriverLoaderFragment extends Fragment {

    private static final String TAG = DriverLoaderFragment.class.getSimpleName();

    private ImageView loader;
    private AnimationDrawable animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loader, container, false);
        loader = view.findViewById(R.id.loader);
        loader.setBackgroundResource(R.drawable.loader_animation);
        animation = (AnimationDrawable) loader.getBackground();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        animation.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        animation.stop();
    }
}

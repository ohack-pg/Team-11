package com.kickstart.woc.wocdriverapp.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.maps.MapFragment;
import com.kickstart.woc.wocdriverapp.R;
import com.kickstart.woc.wocdriverapp.model.User;
import com.kickstart.woc.wocdriverapp.utils.FragmentUtils;
import com.kickstart.woc.wocdriverapp.utils.map.MapInputContainerEnum;
import com.kickstart.woc.wocdriverapp.utils.map.UserClient;

import java.io.IOException;

public class DriverHomeFragment extends Fragment {

    private static final String TAG = DriverHomeFragment.class.getSimpleName();
    private FragmentUtils fragmentUtils = new FragmentUtils();
    private UserClient userClient = new UserClient();
    private User driver;
    private MapInputContainerEnum mapInputContainerEnum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driver = userClient.getDriverDetails();

        if (getArguments() != null) {
            String enumKey = (String) getArguments().get(getString(R.string.intent_input_container));
            mapInputContainerEnum = Enum.valueOf(MapInputContainerEnum.class, enumKey);
        }
        if (mapInputContainerEnum.compareTo(MapInputContainerEnum.Unknown) == 0) {
            if (!driver.isVerified()) {
                mapInputContainerEnum = MapInputContainerEnum.DriverVerificationFragment;
            } else {
                mapInputContainerEnum = MapInputContainerEnum.DriverAvailabilityFragment;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_home, null, false);
        renderInputViewContainer(view);
        return view;
    }

    private void renderInputViewContainer(View view) {
        switch (mapInputContainerEnum) {
            case DriverVerificationFragment:
                fragmentUtils.replaceFragment(R.id.map_view_container, TAG, getFragmentManager(), new MapViewFragment());
                fragmentUtils.replaceFragment(R.id.input_view_container, TAG, getChildFragmentManager(), new DriverVerificationFragment());
                break;
            case DriverAvailabilityFragment:
                fragmentUtils.replaceFragment(R.id.map_view_container, TAG, getFragmentManager(), new MapViewFragment());
                fragmentUtils.replaceFragment(R.id.input_view_container, TAG, getFragmentManager(), new DriverAvailabilityFragment());
                break;
            case DriverRideFoundFragment:
                fragmentUtils.replaceFragment(R.id.map_view_container, TAG, getFragmentManager(), new MapViewFragment());
                fragmentUtils.replaceFragment(R.id.input_view_container, TAG, getFragmentManager(), new DriverRideFoundFragment());
                break;
            case DriverEnterRiderOTPFragment:
                fragmentUtils.replaceFragment(R.id.map_view_container, TAG, getFragmentManager(), new MapViewFragment());
                fragmentUtils.replaceFragment(R.id.input_view_container, TAG, getFragmentManager(), new DriverEnterRiderOTPFragment());
                break;
            case DriverOnTripFragment:
                fragmentUtils.replaceFragment(R.id.map_view_container, TAG, getFragmentManager(), new MapViewFragment());
                fragmentUtils.replaceFragment(R.id.input_view_container, TAG, getFragmentManager(), new DriverOnTripFragment());
                break;
            case DriverTripSummaryFragment:
                fragmentUtils.replaceFragment(R.id.riderHomeContainer, TAG, getFragmentManager(), new DriverTripSummaryFragment());
                break;
            case Unknown:
                try {
                    throw new IOException();
                } catch (IOException e) {
                    Log.d(TAG, "IOException: " + e.getMessage());
                }
                break;
        }
    }
}

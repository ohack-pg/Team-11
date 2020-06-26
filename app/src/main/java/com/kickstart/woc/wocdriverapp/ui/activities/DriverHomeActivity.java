package com.kickstart.woc.wocdriverapp.ui.activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.navigation.NavigationView;
import com.kickstart.woc.wocdriverapp.R;
import com.kickstart.woc.wocdriverapp.model.User;
import com.kickstart.woc.wocdriverapp.services.LocationService;
import com.kickstart.woc.wocdriverapp.ui.fragments.DriverAvailabilityFragment;
import com.kickstart.woc.wocdriverapp.ui.fragments.DriverHomeFragment;
import com.kickstart.woc.wocdriverapp.ui.fragments.MapViewFragment;
import com.kickstart.woc.wocdriverapp.ui.listeners.PhoneCallListener;
import com.kickstart.woc.wocdriverapp.ui.listeners.ReplaceInputContainerListener;
import com.kickstart.woc.wocdriverapp.utils.FragmentUtils;
import com.kickstart.woc.wocdriverapp.utils.WocConstants;
import com.kickstart.woc.wocdriverapp.utils.map.ExpandContractMapUtil;
import com.kickstart.woc.wocdriverapp.utils.map.MapInputContainerEnum;
import com.kickstart.woc.wocdriverapp.utils.map.UserClient;

import java.util.Locale;

import static com.kickstart.woc.wocdriverapp.utils.WocConstants.ERROR_DIALOG_REQUEST;
import static com.kickstart.woc.wocdriverapp.utils.WocConstants.MAP_LAYOUT_STATE_CONTRACTED;
import static com.kickstart.woc.wocdriverapp.utils.WocConstants.MAP_LAYOUT_STATE_EXPANDED;
import static com.kickstart.woc.wocdriverapp.utils.WocConstants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.kickstart.woc.wocdriverapp.utils.WocConstants.PERMISSIONS_REQUEST_ENABLE_GPS;
import static com.kickstart.woc.wocdriverapp.utils.WocConstants.REQUEST_CALL;


public class DriverHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapViewFragment.MapViewSizeListener,
        ReplaceInputContainerListener,
        PhoneCallListener {

    private static final String TAG = DriverHomeActivity.class.getSimpleName();

    private ExpandContractMapUtil expandContractMapUtil = new ExpandContractMapUtil();
    private FusedLocationProviderClient mFusedLocationClient;
    private UserClient userClient;
    private FragmentUtils fragmentUtils = new FragmentUtils();
    private MapInputContainerEnum mapInputContainerEnum = MapInputContainerEnum.Unknown;
    private Location mUserLocation;

    private int mMapLayoutState = 0;
    private boolean mLocationPermissionGranted = false;
    private boolean isLocationServicesOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userClient = (UserClient)getApplicationContext();

        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getLastKnownLocation();
            } else {
                getLocationPermission();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.input_view_container);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (f instanceof DriverAvailabilityFragment) {//the fragment on which you want to handle your back press
            Log.i("BACK PRESSED", "BACK PRESSED");
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation_screen, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent i = new Intent(DriverHomeActivity.this,
                    CreateOrEditRiderActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment() {
        onReplaceInputContainer(mapInputContainerEnum);
    }

    @Override
    public void onMapViewSizeChange() {
        int mapSize = 50;
        FrameLayout mMapViewContainer = findViewById(R.id.map_view_container);
        FrameLayout mInputViewContainer = findViewById(R.id.input_view_container);
        if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
            mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED;
            expandContractMapUtil.expandMapAnimation(mMapViewContainer, mapSize, 100,
                    mInputViewContainer, 100 - mapSize, 0);
        } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
            mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED;
            expandContractMapUtil.contractMapAnimation(mMapViewContainer, 100, mapSize,
                    mInputViewContainer, 0, 100 - mapSize);
        }
    }

    /*
       Step 0: Check Map Service
       a. Determine if google services can be used on the device
       b. Determine if GPS is enabled on the device, send to settings screen to enable GPS
       c. If GPS is not enabled, explicitly asking for location permissions
    */
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    // Used to determine if google services can be used by device
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "You can't make map requests");
        }
        return false;
    }

    // Used to determine if maps is enabled on the device
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    // This will open Settings on the device to enable GPS
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    // startActivityForResult is triggered to enable GPS
    // If GPS is enabled, we continue with request
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (mLocationPermissionGranted) {
                    getLastKnownLocation();
                } else {
                    getLocationPermission();
                }
                break;
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    // Run after location permissions are selected
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }

                break;
            case REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapInputContainerEnum = MapInputContainerEnum.valueOf(permissions[2]);
                    onMakePhoneCall(mapInputContainerEnum, permissions[1]);
                }
                break;
        }
    }

    // Step 1: Get User Details
    // Step 2: Get User Location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            if (location != null) {
                                Log.d(TAG, "onComplete: latitude: " + location.getLatitude());
                                Log.d(TAG, "onComplete: longitude: " + location.getLongitude());
                                mUserLocation = new Location("FusedLocationProviderClient");
                                mUserLocation.setLatitude(location.getLatitude());
                                mUserLocation.setLongitude(location.getLongitude());
                                saveUserLocation();
                                showFragment();
                                startLocationService();
                            }
                        }
                    }
                }
        );
    }

    // Step 3: Save user location
    private void saveUserLocation() {
        userClient.saveUserLocation(mUserLocation.getLatitude(), mUserLocation.getLongitude());
    }

    // Step 5: Start Location Service
    private void startLocationService() {
        if (isLocationServicesOn && !isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                this.startForegroundService(serviceIntent);
            } else {
                this.startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) this.getSystemService(this.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.kickstart.woc.wocdriverapp.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    @Override
    public void onReplaceInputContainer(MapInputContainerEnum key) {
        DriverHomeFragment driverHomeFragment = new DriverHomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.intent_input_container), key.name());
        driverHomeFragment.setArguments(bundle);
        fragmentUtils.replaceFragment(R.id.driver_home_fragment, TAG, getSupportFragmentManager(), driverHomeFragment);
    }

    @Override
    public void onMakePhoneCall(MapInputContainerEnum mapInputContainerEnum, String number) {
        if (number.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE, number, mapInputContainerEnum.name()}, WocConstants.REQUEST_CALL);
            } else {
                String dial = "tel: " + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }
    }
}

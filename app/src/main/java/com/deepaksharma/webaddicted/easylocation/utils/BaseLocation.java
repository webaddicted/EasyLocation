package com.deepaksharma.webaddicted.easylocation.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.deepaksharma.webaddicted.easylocation.Permission.EasyPermissions;
import com.deepaksharma.webaddicted.easylocation.Permission.PermissionListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by deepaksharma on 23/8/18.
 */
public abstract class BaseLocation extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, PermissionListener {
    private static String TAG = BaseLocation.class.getSimpleName();
    // The minimum distance to change Updates in meters
    private static long INTERVAL = 1000; // 1 sec
    // The minimum time between updates in milliseconds
    private static long FASTEST_INTERVAL = 1000; // 1 sec
    // The minimum distance to change Updates in meters
    private static long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    private static GoogleApiClient mGoogleApiClient;
    LocationManager locationManager;
    private boolean continueLocation = false;

    private static LocationRequest mLocationRequest;
    private boolean isGPSEnabled, isNetworkEnabled;
    private static android.location.Location mLocation;

    /**
     * provide user current location single time
     */
    public void getLocation() {
        checkPermission();
    }

    /**
     * provide user current location after a perticular time
     * @param timeInterval - location update after time intervel in sec
     * @param fastInterval - fast time interval
     * @param displacement - location update after a perticular distance
     */
    public void getLocation(@NonNull long timeInterval, @NonNull long fastInterval, @NonNull long displacement) {
        this.continueLocation = true;
        INTERVAL = INTERVAL * timeInterval;
        FASTEST_INTERVAL = FASTEST_INTERVAL * fastInterval;
        MIN_DISTANCE_CHANGE_FOR_UPDATES = MIN_DISTANCE_CHANGE_FOR_UPDATES * displacement;
        checkPermission();
    }

    /**
     * check Gps status & location permission
     */
    private void checkPermission() {
        try {
            locationManager = (LocationManager) BaseLocation.this.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            List<String> locationList = new ArrayList<>();
//            locationList.add(Manifest.permission.LOCATION_HARDWARE);
            locationList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            locationList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (EasyPermissions.checkAndRequestPermission(this, locationList, this)) {
                checkGpsLocation();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * check play services & enable gps
     */
    private void checkGpsLocation() {
        if (isGPSEnabled) {
            if (checkPlayServices()) {
                buildGoogleApiClient();
            } else {
                Toast.makeText(this, "Play service not available.", Toast.LENGTH_SHORT).show();
            }
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
//        Criteria criteria = new Criteria();
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(true);
//        criteria.setSpeedRequired(false);
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setCostAllowed(true);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            buildGoogleApiClient();
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "login  Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        mGoogleApiClient.connect();
    }

    /**
     * check Google play services status.
     * @return
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 1000).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here
//                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(BaseLocation.this, 2000);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    //        [Permission Start]
    @Override
    public void onRequestPermissionsResult(@NonNull int requestCode, @NonNull  String permissions[], @NonNull int[] grantResults) {
        EasyPermissions.checkResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(List<String> mCustomPermission) {
        checkGpsLocation();
    }

    @Override
    public void onPermissionDenied(List<String> mCustomPermission) {

    }
//       [Permission Stop]

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    /**
     * gps is not enabled then it gives last location
     * @return
     */
    @SuppressLint("MissingPermission")
    public android.location.Location getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                if (location != null) {
                    mLocation = location;
//                    Log.d(TAG, "onSuccess: not null check");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error trying to get last GPS location");
                e.printStackTrace();
            }
        });
        return mLocation;
    }

    /**
     * location update interval
     */
    private void createLocationRequest() {
        Log.d(TAG, "createLocationRequest: ");
        mLocationRequest = new LocationRequest();
        if (this.continueLocation) {
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
        }
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        mLocation = location;
        getAddress(location);
    }

    /**
     * provide user current address on the bases of lat long
     * @param location
     */
    private void getAddress(@NonNull android.location.Location location) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null) {
                android.location.Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d("My Current address", "" + strReturnedAddress.toString());
            } else {
                Log.d("My Current address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current address", "Can't get Address!");
        }
        getCurrentLocation(location, strAdd);
    }

    public abstract void getCurrentLocation(@NonNull android.location.Location location, @NonNull String address);

}

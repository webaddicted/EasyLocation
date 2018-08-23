package com.deepaksharma.webaddicted.easylocation.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.Context.LOCATION_SERVICE;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by deepak sharma.
 */

public class GPSLocation implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    String TAG = GPSLocation.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation = null;
    LocationManager locationManager;
    // gradle Location add
    private LocationRequest mLocationRequest;
    double latitude = 0.0;
    double longitude = 0.0;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    Context mContext;
    GetLocationListener mGetLocationListener;
    public GPSLocation(Context context) {
        this.mContext = context;
    }

    public void getLocation(GetLocationListener getLocationListener) {
        this.mGetLocationListener = getLocationListener;
        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);


//        locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER,
//                10,
//                10, this);
        if (isGPSEnabled) {
//            // Building the GoogleApi client
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            buildGoogleApiClient();
        } else {
            getLastLocation();
            Log.d(TAG, "onResponse: login==gps not on=>");
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        startLocationUpdates();
    }


    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(mContext);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
//                        Toast.makeText(DeviceDetailServices.this, "check===>"+location.getLatitude()+"\n"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                        if (location != null) {
                            Log.d(TAG, "onResponse: onSuccess: check");
//      onLocationChanged(location);
                            displayLocation(location);
//                                Log.d(TAG, "onSuccess: Not Null");
                        }else{
                            getLocationnn();
                            Log.d(TAG, "onResponse: onSuccess: null check");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onResponse: Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    public Location getLocationnn() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "onResponse: getLocation: isGPSEnabled===>"+isGPSEnabled+"\n isNetworkEnabled===>"+isNetworkEnabled);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                displayLocation(mLastLocation);
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) this);
                    Log.d(TAG, "onResponse: Network");
                    if (locationManager != null) {
                        Log.d(TAG, "onResponse: Network not null  ");
                        mLastLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLastLocation != null) {
                            displayLocation(mLastLocation);
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (mLastLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) this);
                        Log.d(TAG, "onResponse: GPS Enabled");
                        if (locationManager != null) {
                            Log.d(TAG, "onResponse: GPS Enabled not null");
                            mLastLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (mLastLocation != null) {
                                displayLocation(mLastLocation);
                                latitude = mLastLocation.getLatitude();
                                longitude = mLastLocation.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLastLocation;
    }

    @SuppressLint("MissingPermission")
    private void displayLocation(Location mLastLocation) {

        if (mLastLocation != null && mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Log.d(TAG, "onResponse: Current Location latitude===>" + latitude + "\n longitude===>" + longitude);
            if(mGetLocationListener !=null) {
                    mGetLocationListener.getCurrentLocation(mLastLocation);
            }

        } else {
            Log.d(TAG, "onResponse: (Couldn't get the location. Make sure location is enabled on the device)");
//            lblLocation
//                    .setText(
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        Log.d(TAG, "onResponse===>" + String.valueOf(location.getLatitude()) + "\n" + String.valueOf(location.getLongitude()));

        displayLocation(mLastLocation);
    }


    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        Log.d(TAG, "onResponse:  startLocationUpdates: ");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } else {
            buildGoogleApiClient();
        }
    }

    protected void stopLocationUpdates() {
        Log.d(TAG, "onResponse: stopLocationUpdates: ");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, (LocationListener) this);
        } else {
            buildGoogleApiClient();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onResponse:  Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode() + "\n" + result.getErrorMessage());
        mGoogleApiClient.connect();
    }


    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "onResponse: buildGoogleApiClient: ");
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        createLocationRequest();

    }

    protected void createLocationRequest() {
        Log.d(TAG, "onResponse: createLocationRequest: ");
        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000); // 10 sec
//        mLocationRequest.setFastestInterval(5000); // 5 sec
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setSmallestDisplacement(10); // 10 meters
    }


    public interface GetLocationListener{
        void getCurrentLocation(Location location);
    }
}

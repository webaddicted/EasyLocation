package com.deepaksharma.webaddicted.easylocation.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.deepaksharma.webaddicted.easylocation.utils.BaseLocation;
import com.deepaksharma.webaddicted.easylocation.R;
import com.deepaksharma.webaddicted.easylocation.databinding.ActivityMainBinding;
import com.deepaksharma.webaddicted.easylocation.utils.GPSLocation;

public class MainActivity extends BaseLocation implements MainListener, GPSLocation.GetLocationListener {
    ActivityMainBinding activityMainBinding;
    MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        activityMainBinding.setHandler(new MainHandler(this));
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
    }

    @Override
    public void onBaseListener() {
        getLocation();
    }

    @Override
    public void onCustomListener() {
        GPSLocation gpsLocation = new GPSLocation(this);
        gpsLocation.getLocation(this);
    }

    @Override
    public void getCurrentLocation(@NonNull Location location, @NonNull String address) {
        Toast.makeText(this, "getLatitude -> "+location.getLatitude()+"\n getLongitude -> "+location.getLongitude()+"\nAddress -> "+address, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void getCurrentLocation(Location location) {
        Toast.makeText(this, "Custom get Longitude -> "+location.getLongitude()+"\n GetLongitude -> "+location.getLatitude(), Toast.LENGTH_SHORT).show();
    }
}

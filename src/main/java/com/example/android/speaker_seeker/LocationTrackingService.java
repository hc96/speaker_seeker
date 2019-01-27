package com.example.android.speaker_seeker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.android.speaker_seeker.models.UserLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationTrackingService extends Service {

    private static final String TAG = LocationTrackingService.class.getSimpleName();
    private LocationManager mLocationManager;
    private FirebaseAuth mAuthentication;
    private DatabaseReference mDatabaseReference;
    private int mBatteryLevel;
    private BroadcastReceiver mReceiver;
    private int mLocationInterval;

    private static final int LOCATION_INTERVAL = 5000; // in milliseconds
    private static final int LOCATION_INTERVAL_BATTERY_LOW = 10*60*1000; // in milliseconds
    private static final float LOCATION_DISTANCE = 10; // in meters

    public LocationTrackingService() {
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            saveLastLocationDatabase(mLastLocation);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        initializeLocationManager();
        mReceiver = new BatteryBroadcastReceiver();

        IntentFilter filterBatteryLevelChange = new IntentFilter();
        filterBatteryLevelChange.addAction(Intent.ACTION_BATTERY_LOW);
        filterBatteryLevelChange.addAction(Intent.ACTION_BATTERY_OKAY);

        registerReceiver(mReceiver,filterBatteryLevelChange);

        // Check the initial state of battery
        if (isBatteryLow()){
            mLocationInterval = LOCATION_INTERVAL_BATTERY_LOW;
            Log.d(TAG, "Battery level is low, new location updates interval: " + mLocationInterval);
        }
        else {
            mLocationInterval = LOCATION_INTERVAL;
            Log.d(TAG, "Battery level is ok, new location updates interval: " + mLocationInterval);
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    mLocationInterval,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    mLocationInterval,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]
            );
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        Log.d(TAG,"LOCATION_INTERVAL: " + mLocationInterval + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listener, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void saveLastLocationDatabase(Location lastLocation){
        mAuthentication = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        double latitude = lastLocation.getLatitude();
        double longitude = lastLocation.getLongitude();
        UserLocation userLocation = new UserLocation(latitude,longitude);

        try {
            FirebaseUser firebaseUser = mAuthentication.getCurrentUser();
            String uid = firebaseUser.getEmail().replace(".", ",");
            mDatabaseReference.child("locations").child(uid).setValue(userLocation);;
            Log.d(TAG, "User's location has been written to database");
        } catch (NullPointerException e){
            Log.d(TAG, "Error while writing user to database");
        }
    }

    // Check if the initial battery level is low
    private boolean isBatteryLow(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = LocationTrackingService.this.registerReceiver(null, intentFilter);
        int batteryLevel = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        Log.d(TAG,"Battery level: " + batteryLevel);
        return (batteryLevel < 16);
    }

    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean batteryLow = intent.getAction().equals(Intent.ACTION_BATTERY_LOW);

            if (batteryLow) {
                mLocationInterval = LOCATION_INTERVAL_BATTERY_LOW;
                Log.d(TAG, "Battery level is low, new location updates interval: " + mLocationInterval);
            }
            else {
                mLocationInterval = LOCATION_INTERVAL;
                Log.d(TAG, "Battery level is ok, new location updates interval: " + mLocationInterval);
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        mLocationInterval,
                        LOCATION_DISTANCE,
                        mLocationListeners[0]
                );
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        mLocationInterval,
                        LOCATION_DISTANCE,
                        mLocationListeners[1]
                );
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }
        }
    }
}

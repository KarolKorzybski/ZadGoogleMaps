package com.example.karol.zadgooglemaps;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    double lat1 = 0;
    double lng1 = 0;
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    PendingIntent pendingIntent;
    int priority;
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    boolean stan = false;
    Location mLastLocation;
    private Timer mTimer;

    private TimerTask mTimerTask = new TimerTask() {
        public void run() {
            if (!stan) {
                mLocationClient = new GoogleApiClient.Builder(MyService.this)
                        .addConnectionCallbacks(MyService.this)
                        .addOnConnectionFailedListener(MyService.this)
                        .addApi(LocationServices.API)
                        .build();
                mLocationRequest.setInterval(1000);
                mLocationRequest.setFastestInterval(1000);


                priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
                //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes

                mLocationRequest.setPriority(priority);
                mLocationClient.connect();
            }
        }
    };
    public void onCreate() {
        super.onCreate();
        this.mTimer = new Timer();
        this.mTimer.schedule(mTimerTask, 0, 250);  }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onConnected(Bundle dataBundle) {

         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        Intent intent = new Intent(this, MyService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, pendingIntent);
        } catch (IllegalStateException e) {
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        onLocationChanged(mLastLocation);

    }

    @Override
    public void onConnectionSuspended(int i) {
        // Log.d(TAG, "Connection suspended");
    }


    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        //Log.d(TAG, "Location changed");
        try {
            // Log.d("lat", String.valueOf(location.getLatitude()));
            // Log.d("long", String.valueOf(location.getLongitude()));

        } catch (NullPointerException e) {
            // Log.d("NullPointerException", String.valueOf(e));
        }
        if (location != null) {
            // Log.d(TAG, "== location != null");

            //Send result to activities
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            // Log.d("lat", String.valueOf(location.getLatitude()));
            // Log.d("long", String.valueOf(location.getLongitude()));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private void sendMessageToUI(String lat, String lng) {

        // Log.d(TAG, "Sending info...");

        //Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        final Intent intent = new Intent("location_update");
        if (lat1 != Double.parseDouble(lat) || lng1 != Double.parseDouble(lng)) {
            intent.putExtra("Lat", lat);
            intent.putExtra("Long", lng);
            //Log.d("lat", lat);
            //Log.d("long", lng);
            sendBroadcast(intent);
        }
        lat1 = Double.parseDouble(lat);
        lng1 = Double.parseDouble(lng);

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Log.d(TAG, "Failed to connect to Google API");

    }

    public void onDestroy() {
        super.onDestroy();
        mLocationClient.disconnect();
        mTimerTask.cancel();
        pendingIntent = null;
        priority = 0;
        mLocationRequest = null;
        stan = true;
        Log.e("destroy", "!null");

    }
}





package com.example.karol.zadgooglemaps;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MyService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    double lat1 = 0;
    double lng1 = 0;
    private static final String TAG = MyService.class.getSimpleName();
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    public LocationListener listener;
    public LocationManager locationManager;
    PendingIntent pendingIntent;

    public static final String ACTION_LOCATION_BROADCAST = MyService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    boolean stan;
    Location mLastLocation;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);


        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (!stan) {
            Log.d("stan = ", String.valueOf(stan));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                // Log.d(TAG, "== Error On onConnected() Permission not granted");
                //Permission not granted by user so cancel the further execution.

                return;
            }


            Intent intent = new Intent(this, MyService.class);
            pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, pendingIntent);
            } catch (IllegalStateException e) {
                // Log.d("illegal", "illegal");
            }

            //Log.d(TAG, "Connected to Google API");



            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            onLocationChanged(mLastLocation);
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
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
            //  Log.d("long", String.valueOf(location.getLongitude()));

        } catch (NullPointerException e) {
            //  Log.d("NullPointerException", String.valueOf(e));
        }
        if (location != null) {
            // Log.d(TAG, "== location != null");

            //Send result to activities
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            // Log.d("lat", String.valueOf(location.getLatitude()));
            //  Log.d("long", String.valueOf(location.getLongitude()));
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
            Log.d("lat", lat);
            Log.d("long", lng);
            sendBroadcast(intent);
            intent.putExtra(EXTRA_LATITUDE, lat);
            intent.putExtra(EXTRA_LONGITUDE, lng);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
        final Intent intent = new Intent("location_update");
            intent.putExtra("destroy", "true");
            sendBroadcast(intent);
        pendingIntent = null;
        stan = true;
        mLocationClient.disconnect();
        Log.e("destroy", "!null");

    }
}


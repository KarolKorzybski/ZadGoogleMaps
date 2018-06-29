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

/**
 * Klasa MyService, korzystająca z GoogleApiClient pobiera dane z lokalizacji oraz wysyła je
 * do MapsAcitivity
 */
public class MyService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    /**
     * Zmienne odpowiedzialne za obecną pozycję na mapie
     */
    double lat1 = 0;
    double lng1 = 0;

    /**
     * Inicjalizacja elementów niezbędnych do konfiguracji lokalizacji
     */
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    PendingIntent pendingIntent;
    Location mLastLocation;

    int priority;
    boolean stan = false;

    /**
     * Inicjalizacja timera, konfiguracja lokalizacji, nadawanie priorytetu połączenia z lokalizacją,
     *
     */
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

    /**
     * Wywoływanie timera, konfiguracją cyklicznej pracy co 250ms
     */
    public void onCreate() {
        super.onCreate();
        this.mTimer = new Timer();
        this.mTimer.schedule(mTimerTask, 0, 250);
    }

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
    /**
     *Konfiguracja intencji pendingIntent,
     *Odbieranie lokalizacji
     */
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


    /**
     * Sprawdzanie lokalizacji i wywołanie metody odpowiedzialnej za wysyłanie lokalizacji do aktywności
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {

            //Send result to activities
            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

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

    /**
     * Metoda odpowiedzialna za wysyłanie parametrów lokalizacji do aktywności
     * @param lat
     * @param lng
     */
    private void sendMessageToUI(String lat, String lng) {

        // Log.d(TAG, "Sending info...");

        final Intent intent = new Intent("location_update");
        if (lat1 != Double.parseDouble(lat) || lng1 != Double.parseDouble(lng)) {
            intent.putExtra("Lat", lat);
            intent.putExtra("Long", lng);
            sendBroadcast(intent);
        }
        lat1 = Double.parseDouble(lat);
        lng1 = Double.parseDouble(lng);

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Log.d(TAG, "Failed to connect to Google API");

    }

    /**
     * Metoda wywoływana w momencie usuwania serwisu
     */
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


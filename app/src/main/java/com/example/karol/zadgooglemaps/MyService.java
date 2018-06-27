package com.example.karol.zadgooglemaps;

import android.Manifest;
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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    private Toast toast;
    private Timer timer;
    private TimerTask timerTask;
    boolean running = false;
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.d("coordinates", "work");



        }
    }
    public LocationListener listener;
    public LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        timer = new Timer();
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        running = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        clearTimerSchedule();
        final Intent i = new Intent("location_update");
        initTask();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (running) {
                    Log.d("tutaj1","tutaj");
                    LocationListener listener = new LocationListener() {

                        public void onLocationChanged(Location location) {

                            i.putExtra("Lat", location.getLatitude());
                            i.putExtra("Long", location.getLongitude());
                            i.putExtra("Long", location.getLongitude());

                            i.putExtra("coordinates", location.getLongitude() + " " + location.getLatitude());
                            Log.d("coordinates", location.getLongitude() + " " + location.getLatitude());
                            Log.d("coordinates", location.getLongitude() + " " + location.getLatitude());
                            Log.d("Lat", "" + location.getLatitude());
                            Log.d("Long", "" + location.getLongitude());

                            sendBroadcast(i);
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {
                            Log.d("tutaj2","tutaj");
                        }

                        @Override
                        public void onProviderEnabled(String s) {
                            Log.d("tutaj3","tutaj");
                        }

                        @Override
                        public void onProviderDisabled(String s) {
                            Log.d("tutaj4","tutaj");
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);

                        }
                    };

                    LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    Log.d("tutaj5","tutaj");
                    //noinspection MissingPermission
                    if (ActivityCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                       // return super.onStartCommand(intent, flags, startId);
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 900, 0, listener);
                }handler.postDelayed(this,1000);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    private void clearTimerSchedule() {
        if(timerTask != null) {
            timerTask.cancel();
            timer.purge();
        }
    }

    private void initTask() {
        timerTask = new MyTimerTask();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
            Log.e("destroy","!null");
        }
        Log.e("destroy","null");
    }
}

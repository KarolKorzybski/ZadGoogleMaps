package com.example.karol.zadgooglemaps;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Klasa sprawdza połączenie połączenie GPS,
 * odbieranie dane z serwisu o lokalizacji
 * oraz wyświetla marker i rysuje położenie na mapie
 *
 * @author Karol Korzybski
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    /**
     * BroadcastReceiver pozwala odbierać powiadomienia z serwisu
     */
    BroadcastReceiver broadcastReceiver;

    /**
     * Zmienne odpowiedzialne za ustawienie markerów i rysowanie trasy
     */
    Marker base_marker, currentMarker;
    MarkerOptions option;
    CameraPosition cameraPosition;
    PolylineOptions polylineOptions;
    LatLng latLng;
    ArrayList<LatLng> points = new ArrayList<LatLng>();
    public GoogleMap myMap;
    /**
     * Zmienne niezbędne do wyliczania dystansu i odbieranie punktów lokalizacyjnych
     */
    double v = 0.0, v1 = 0.0, distance, Lat = 0, Long = 0, base_lat = 0, base_lng = 0, total_distance = 0;

    /**
     * Zmienne odpowiedzialne za stoper
     */
    int hours, minutes, secs, seconds = 0;
    String time;

    /**
     * Zmienne do odbierania czasu systemowego
     */
    long timestamp, timestamp2;


    boolean alarm = false, running, connected, stan = false;

    /**
     * Inicjalizacja przycisków
     */
    @BindView(R.id.start)
    Button start;
    @BindView(R.id.stop)
    Button stop;


    /**
     * Stała niezbędna do wyświetlania dialogów
     */
    static final private int ALERT_GPS = 1;

    /**
     * Dialog oczekujący na załadowanie się mapy
     */
    ProgressDialog myProgress;

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;


    LocationManager locationManager;

    /**
     * @param savedInstanceState
     * Metoda określa, który układ należy użyć, sprawdza czy mamy załączoną łączność sieciową,
     * wyświetla progresdialog, tworzy fragment mapy oraz uruchamia runTimer
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        // Display Progress Bar.
        myProgress.show();
        TextView distance_text;
        distance_text = (TextView) findViewById(R.id.textView);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            connected = true;
            myProgress.dismiss();
        } else
            connected = false;
        if (!connected) {
            connected = true;
            distance_text.setText("Network disable - please turn on");

        }
        if (connected) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //you will call this activity later


            SupportMapFragment mapFragment
                    = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Set callback listener, on Google Map ready.
            mapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googleMap) {
                    onMyMapReady(googleMap);
                }
            });
            if (!runtime_permissions())
                enable_buttons();
        }

        runTimer();
    }

    private void onMyMapReady(GoogleMap googleMap) {
        // Get Google Map from Fragment.
        myMap = googleMap;

        // Sét OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // Map loaded. Dismiss this dialog, removing it from the screen.
                myProgress.dismiss();

                askPermissions();
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //myMap.setMyLocationEnabled(true);

    }

    /**
     *Metoda odpowiedzialna za pytanie o uprawnienia do lokalizacji
     */
    private void askPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(this, permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }
        }
    }

    private void enable_buttons() {

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("start", "start");
                if (!running) {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        alarm = true;
                        if (alarm)
                            showDialog(ALERT_GPS);

                    }
                    Intent i = new Intent(getApplicationContext(), MyService.class);
                    startService(i);


                    myMap.clear();
                    total_distance = 0.0;
                    // Empty the array list
                    points.clear();
                    seconds = 0;
                    stan = false;
                }
                running = true;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                running = false;
                Intent i = new Intent(getApplicationContext(), MyService.class);
                stopService(i);

            }
        });

    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist;

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case ALERT_GPS:
                return createAlertDialogGps();
            default:
                return null;

        }
    }

    private Dialog createAlertDialogGps() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("GPS disable");
        dialogBuilder.setMessage("Turn on?");
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        dialogBuilder.setNegativeButton("No", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                //showToast("You picked negative button");
            }
        });
        return dialogBuilder.create();
    }

    public void runTimer() {
        final NumberFormat formatter = new DecimalFormat("#0.00");
        final TextView timeView = (TextView) findViewById(R.id.textView2);
        final TextView textView = (TextView) findViewById(R.id.textView);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                hours = seconds / 3600;
                minutes = (seconds % 3600) / 60;
                secs = seconds % 60;
                time = String.format("%02d:%02d:%02d", hours, minutes, secs);
                Log.d("running", String.valueOf(running));
                timeView.setText(time);
                if (running) {
                    timestamp2 = System.currentTimeMillis();
                    Log.d("service", String.valueOf(isMyServiceRunning(MyService.class)));
                    try {
                        //Log.d("Work!", "Work!");
                        if (broadcastReceiver == null) {
                            broadcastReceiver = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    try {
                                        Lat = Double.parseDouble("" + intent.getExtras().get("Lat"));
                                        Long = Double.parseDouble("" + intent.getExtras().get("Long"));
                                    } catch (NullPointerException e) {
                                        //Log.e("NullPointerException ", String.valueOf(e));
                                    } catch (NumberFormatException e) {
                                        //Log.e("NullPointerException ", String.valueOf(e));
                                    }
                                }
                            };
                        }
                        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

                        latLng = new LatLng(Lat, Long);
                        if (!stan & Lat != 0 & Long != 0) {
                            timestamp = System.currentTimeMillis();
                            //if (!stan) {
                            option = new MarkerOptions();
                            option.title("START");
                            option.position(latLng);
                            base_marker = myMap.addMarker(option);
                            cameraPosition = new CameraPosition.Builder()
                                    .target(latLng)             // Sets the center of the map to location user
                                    .zoom(17)                   // Sets the zoom
                                    .bearing(90)                // Sets the orientation of the camera to east
                                    .tilt(40)                   // Sets the tilt of the camera to 40 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            base_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ico));
                            base_marker.showInfoWindow();
                            base_lat = Lat;
                            base_lng = Long;
                            stan = true;
                        }
                        if (stan) {
                            seconds = (int) ((timestamp2 - timestamp) / 1000);
                        }
                        if (v != Lat & v1 != Long) {
                            if (Lat != 0 | Long != 0) {
                                distance = distFrom(v, v1, Lat, Long);
                            }

                            if (!(v == 0.0 & v1 == 0.0)) {
                                textView.setText("Position start");
                            }

                            if (distance > 0) {
                                if (!(v == 0 & v1 == 0)) {
                                    total_distance += distance;
                                    textView.setText("Current distance = " + formatter.format(distance) + "m\n" +
                                            " Total distance = " + formatter.format(total_distance) + "m");
                                }
                                myMap.clear();

                                option = new MarkerOptions();
                                option.position(new LatLng(base_lat, base_lng));
                                currentMarker = myMap.addMarker(option);
                                option.title("START");
                                currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ico));
                                currentMarker.showInfoWindow();
                                v = Lat;
                                v1 = Long;
                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.RED);
                                polylineOptions.width(6);
                                points.add(latLng);
                                polylineOptions.addAll(points);
                                myMap.addPolyline(polylineOptions);
                                cameraPosition = new CameraPosition.Builder()
                                        .target(latLng)             // Sets the center of the map to location user
                                        .zoom(17)                   // Sets the zoom
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(40)                   // Sets the tilt of the camera to 40 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                                myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                option = new MarkerOptions();
                                option.title("I'm here");
                                option.position(latLng);

                                currentMarker = myMap.addMarker(option);

                                currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.guy));
                                currentMarker.showInfoWindow();

                            }
                        }

                    } catch (NullPointerException e) {
                        textView.setText("Check GPS, click again and wait...");
                    }

                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    protected void onDestroy() {
        Intent i = new Intent(this, MyService.class);
        stopService(i);
        Log.e("destroy", "destroy");
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }
}
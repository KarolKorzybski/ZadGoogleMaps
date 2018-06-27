package com.example.karol.zadgooglemaps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    long timestamp;
    long timestamp2;
    public BroadcastReceiver broadcastReceiver;
    boolean zmienna = false;
    Marker currentMarker2;
    MarkerOptions option;
    Marker currentMarker;
    String x;
    int licznik = 0;
    CameraPosition cameraPosition;
    PolylineOptions polylineOptions;
    double v = 0.0;
    double v1 = 0.0;
    LatLng latLng;
    boolean stan = false;
    ArrayList<LatLng> points = new ArrayList<LatLng>();
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.textView2)
    TextView textView2;
    private GoogleMap mMap;
    double liczba;
    String time;
    int hours;
    int minutes;
    int secs;
    double Lat = 0;
    double Long = 0;
    double Lat2 = 0;
    double Long2 = 0;
    private boolean running;
    private boolean wasRunning;
    private int seconds = 0;
    static final private int ALERT_GPS = 1;
    private GoogleMap myMap;
    private ProgressDialog myProgress;
    TextView odleglosc;
    double base_lat = 0;
    double base_lng = 0;
    double dystans = 0;
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;
    LocationManager locationManager;

    public void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        //Log.d("Lat", "" + intent.getExtras().get("Lat"));
                        //Log.d("Long", "" + intent.getExtras().get("Long"));
                        Lat = Double.parseDouble("" + intent.getExtras().get("Lat"));
                        Long = Double.parseDouble("" + intent.getExtras().get("Long"));
                        //Log.d("Przerwa", "przerwa");
                        //Log.d("Lat", "" + Lat);
                        //Log.d("Lat", "" + Long);
                        //i.putExtra("Long",location.getLongitude());
                        //textView.append("\n" +intent.getExtras().get("coordinates"));
                        //Log.d("coordinates", "" +intent.getExtras().get("coordinates"));
                    } catch (NullPointerException e) {
                        //Log.e("NullPointerException ", String.valueOf(e));
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    public void onStop() {
        super.onStop();


        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {

                        //Log.d("Lat", "" + intent.getExtras().get("Lat"));
                        //Log.d("Long", "" + intent.getExtras().get("Long"));
                        Lat = Double.parseDouble("" + intent.getExtras().get("Lat"));
                        Long = Double.parseDouble("" + intent.getExtras().get("Long"));
                        //Log.d("Przerwa", "przerwa");
                        //Log.d("Lat", "" + Lat);
                        //Log.d("Lat", "" + Long);
                        //i.putExtra("Long",location.getLongitude());
                        //textView.append("\n" +intent.getExtras().get("coordinates"));
                        //Log.d("coordinates", "" + intent.getExtras().get("coordinates"));
                    } catch (NullPointerException e) {
                        //Log.e("NullPointerException ", String.valueOf(e));
                    }
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        odleglosc = (TextView) findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //you will call this activity later
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
/*            showDialog(ALERT_DIALOG_BUTTONS);
            stan=true;
            textView.setText("if");*/
            showDialog(ALERT_GPS);

        }
        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);
        // Display Progress Bar.
        myProgress.show();


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

    private Dialog createAlertDialogNetwork() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Connection network disable");
        dialogBuilder.setMessage("Turn on?");
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                Intent intent = new Intent(Settings.ACTION_SETTINGS);
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

    private void onMyMapReady(GoogleMap googleMap) {
        // Get Google Map from Fragment.
        myMap = googleMap;

        // Sét OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // Map loaded. Dismiss this dialog, removing it from the screen.
                myProgress.dismiss();

                askPermissionsAndShowMyLocation();
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);

    }


    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
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

        // Show current location on Map.
        //this.showMyLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

    public void onStart() {

/*        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);*/
        super.onStart();
        if (!zmienna) {
            zmienna = true;
            runTimer();
        }
    }

    /*    @OnClick({R.id.button, R.id.button2})
        public void onViewClicked(View view) {


            switch (view.getId()) {
                case R.id.button:
                    if (!running) {
                        Intent i = new Intent(getApplicationContext(), MyService.class);
                        startService(i);
                        myMap.clear();
                        dystans = 0.0;
                        // Empty the array list
                        points.clear();
                        seconds = 0;
                        stan = false;
                    }
                    running = true;
                    break;
                case R.id.button2:
                    running = false;


                    break;
            }

        }*/
    private void enable_buttons() {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!running) {
                    Intent i = new Intent(getApplicationContext(), MyService.class);
                    startService(i);
                    timestamp = System.currentTimeMillis();

                    myMap.clear();
                    dystans = 0.0;
                    // Empty the array list
                    points.clear();
                    seconds = 0;
                    stan = false;
                }
                running = true;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                running = false;
                Intent i = new Intent(getApplicationContext(), MyService.class);
                stopService(i);

            }
        });

    }

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
/*        Log.d("lat1", String.valueOf(lat1));
        Log.d("lng1", String.valueOf(lng1));
        Log.d("distance", String.valueOf(dist + "m"));*/
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

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;


    private void turnOnScreen() {
        PowerManager.WakeLock screenLock = null;
        if ((getSystemService(POWER_SERVICE)) != null) {
            screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire(10*60*1000L /*10 minutes*/);


            screenLock.release();
        }
    }
    public void runTimer() {
        //      createLocationRequest();

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
                //Log.d("running", String.valueOf(running));
                timeView.setText(time);
                if (running) {
                    //++seconds;
                    timestamp2 = System.currentTimeMillis();
                    seconds = (int)((timestamp2 - timestamp) / 1000);

                    //Log.d("seconds", String.valueOf(seconds));
                    Log.d("time", String.valueOf(time));


                   // Log.d("service", String.valueOf(isMyServiceRunning(MyService.class)));

                    try {
                        //Log.d("Work!", "Work!");

                        latLng = new LatLng(Lat, Long);
                        if (!stan & Lat != 0 & Long != 0) {
                            //if (!stan) {
                            option = new MarkerOptions();
                            option.title("START");
                            option.position(latLng);
                            currentMarker2 = myMap.addMarker(option);
                            cameraPosition = new CameraPosition.Builder()
                                    .target(latLng)             // Sets the center of the map to location user
                                    .zoom(20)                   // Sets the zoom
                                    .bearing(90)                // Sets the orientation of the camera to east
                                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            currentMarker2.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ico));
                            currentMarker2.showInfoWindow();
                            base_lat = Lat;
                            base_lng = Long;
                            stan = true;
                        }
                        if (v != Lat & v1 != Long) {
                            if (Lat != 0 | Long != 0) {
                                liczba = distFrom(v, v1, Lat, Long);


                                Log.d("liczba", String.valueOf(liczba + "m"));
//                                Log.d("v", String.valueOf(v));
//                                Log.d("Lat", String.valueOf(Lat));
//                                Log.d("v1", String.valueOf(v1));
//                                Log.d("Long", String.valueOf(Long));
                            }
                            x = formatter.format(liczba);
                            if (!(v == 0.0 & v1 == 0.0)) {
                                textView.setText("Position start");
                            }

                            if (liczba > 0) {
                                if (!(v == 0 & v1 == 0)) {
                                    dystans += liczba;
                                    textView.setText("Current distance = " + x + "m\n Total distance = " + formatter.format(dystans) + "m");
                                }
                                myMap.clear();

                                option = new MarkerOptions();
                                option.position(new LatLng(base_lat, base_lng));
                                currentMarker = myMap.addMarker(option);
                                option.title("START");
                                currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ico));
                                currentMarker.showInfoWindow();
                                //Log.d("MYTAG", "I'm here");
/*                                    Log.d("v", String.valueOf(v));
                                    Log.d("Lat", String.valueOf(Lat));
                                    Log.d("v1", String.valueOf(v1));
                                    Log.d("Long", String.valueOf(Long));*/
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
                                        .zoom(20)                   // Sets the zoom
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                                myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                option = new MarkerOptions();
                                option.title("I'm here");
                                option.position(latLng);

                                currentMarker = myMap.addMarker(option);

                                currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.guy));
                                currentMarker.showInfoWindow();

                            }
                        } else {
                            //turnOnScreen();
                            //Log.e("turnOnScreen","turnOnScreen");
/*                            Log.d("v", String.valueOf(v));
                            Log.d("Lat", String.valueOf(Lat));
                            Log.d("v1", String.valueOf(v1));
                            Log.d("Long", String.valueOf(Long));*/
/*                            if(licznik == 0)
                            {
                                Log.d("service1", String.valueOf(isMyServiceRunning(MyService.class)));

                                Intent i = new Intent(getApplicationContext(), MyService.class);
                                stopService(i);
                                licznik=2;

                            }

                            if(licznik ==2)
                            {
                                Log.d("service2", String.valueOf(isMyServiceRunning(MyService.class)));
                                Intent i = new Intent(getApplicationContext(), MyService.class);
                                startService(i);
                                licznik=3;

                            }
                            if(licznik>2)
                            {
                                licznik++;
                            }
                            if(licznik==10)
                            {
                                licznik=0;
                            }
                            Log.d("service3", String.valueOf(isMyServiceRunning(MyService.class)));*/
                            //.d("Not location!", "Not location!");
                            //textView.setText("identical\nTotal distance = " + formatter.format(dystans) + "m");
                            //askPermissionsAndShowMyLocation();
/*                            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                            final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
                            kl.disableKeyguard();

                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
                            wakeLock.acquire();
                            wakeLock.release();*/
                        }

                    } catch (NullPointerException e) {
                        textView.setText("Check GPS, click again and wait...");
                        //Log.e("MYTAG", "Check GPS, click again and wait...");
                    }

                }
                handler.postDelayed(this, 900);
            }
        });
    }
}
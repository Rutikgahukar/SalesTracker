package com.example.locationdetect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LocationUpdateService extends Service {
    private static final String CHANNEL_ID = "LocationUpdateServiceChannel";
    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private int uniqueIdCounter = 1;

    private PowerManager.WakeLock wakeLock;

    private final Handler handler = new Handler();

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();


        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("locations");
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }
    private void createNotificationChannel() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Update Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "LocationUpdateService::WakeLock");
            wakeLock.acquire();
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (checkLocationPermission()) {
            Notification notification = createNotification();
            if (notification != null) {
                startForeground(1, notification);
                acquireWakeLock(); // Acquire WakeLock
                if (locationCallback == null) {
                    startLocationUpdates();
                }
            } else {
                Log.e("LocationUpdateService", "Notification is null");
            }
        } else {
            // Dynamically request location permission
        }
        return START_STICKY;
    }

    private boolean checkLocationPermission() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }



    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, SalesExecutive_Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Update Service")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Set high priority
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    Log.d("LocationUpdateService", "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    saveLocationToFirebase(location.getLatitude(), location.getLongitude());

                    sendBroadcast(new Intent("location_update")
                            .putExtra("latitude", location.getLatitude())
                            .putExtra("longitude", location.getLongitude()));
                }
            }
        };

        if (fusedLocationClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } else {
            Log.e("Permission", "Location permission not granted");
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }


    private void saveLocationToFirebase(double latitude, double longitude) {
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String uniqueId = preferences.getString("uniqueId", "");



        DatabaseReference locationsRef = databaseReference.child(uniqueId);

        // Create a unique key for each location update
        String locationKey = locationsRef.push().getKey();

        // Create a new child node for each location update
        DatabaseReference locationUpdateRef = locationsRef.child(locationKey);

        locationUpdateRef.child("latitude").setValue(latitude);
        locationUpdateRef.child("longitude").setValue(longitude);

        // Add other location details if needed
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(Calendar.getInstance().getTime());
        String date = timestamp.split(" ")[0];
        String time = timestamp.split(" ")[1];

        locationUpdateRef.child("date").setValue(date);
        locationUpdateRef.child("time").setValue(time);

    }


    @Override
    public void onDestroy() {
        releaseWakeLock();
        // Remove any pending callbacks to avoid memory leaks
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

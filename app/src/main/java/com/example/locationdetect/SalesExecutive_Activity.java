package com.example.locationdetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

public class SalesExecutive_Activity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE = 1003;
    private static final long LOCATION_UPDATE_INTERVAL = 1000;
    private Switch aSwitch;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Timer locationUpdateTimer;
    private static final String SWITCH_STATE_KEY = "switch_state";
    private static final String USER_ID_KEY = "user_id";
   public  Button LogOut ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_executive);

        Toast.makeText(this, "Please turn on your mobile location", Toast.LENGTH_LONG).show();

        aSwitch = findViewById(R.id.switch1);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    requestLocationPermission();
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
                saveSwitchState(isChecked);
            }
        });

        boolean savedSwitchState = getSwitchState();
        aSwitch.setChecked(savedSwitchState);
        requestLocationPermission();
        requestForegroundServicePermission();

        LogOut = findViewById(R.id.SalesExecativeLogout);
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("userLoggedIn", true);
        editor.apply();
        LogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear SharedPreferences to log out user
                SharedPreferences preferences = getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().remove("userLoggedIn").apply();

                // Navigate back to LoginActivity
                Intent intent = new Intent(SalesExecutive_Activity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optional: Close this activity to prevent going back to it using the back button
            }
        });

        ImageView backArrowImage = findViewById(R.id.backarrowimage);

        backArrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Please logout first", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveSwitchState(boolean isChecked) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SWITCH_STATE_KEY, isChecked);
        editor.apply();
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void requestForegroundServicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE);
            }
        }
    }
    private boolean getSwitchState() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(SWITCH_STATE_KEY, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Log.e("Permission", "Location permission denied");
                }
                break;

            case FOREGROUND_SERVICE_PERMISSION_REQUEST_CODE:
                break;
        }
    }

    private void startLocationUpdates() {
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        startService(serviceIntent);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                }
            }
        };

        locationUpdateTimer = new Timer();
        locationUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkLocationPermission()) {
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(5000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                } else {
                    Log.e("Permission", "Location permission not granted or switch is off");
                }
            }
        }, 0, LOCATION_UPDATE_INTERVAL);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (locationUpdateTimer != null) {
            locationUpdateTimer.cancel();
        }
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        stopService(serviceIntent);
    }
}

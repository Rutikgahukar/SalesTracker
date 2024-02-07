package com.example.locationdetect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000; // Splash screen duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_acitivty);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the MainActivity after the splash duration
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close the splash activity to prevent going back to it
            }
        }, SPLASH_TIME_OUT);
    }
}

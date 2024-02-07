package com.example.locationdetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    public static final String PREF_NAME = "LoginPrefs";
//        private static final String DEFAULT_LOGIN_ID = "Admin@123";
//    private static final String DEFAULT_PASSWORD = "Admin@123";
    private static final String KEY_LOGIN_ID = "loginId";
    private static final String KEY_PASSWORD = "password";

    private EditText userEditText, passEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userEditText = findViewById(R.id.user);
        passEditText = findViewById(R.id.pass);

        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isAdminLoggedIn = preferences.getBoolean("adminLoggedIn", false);
        boolean isUserLoggedIn = preferences.getBoolean("userLoggedIn", false);

        if (isAdminLoggedIn) {
            startActivity(new Intent(LoginActivity.this, Admin_Activity.class));
            finish();
        } else if (isUserLoggedIn) {
            startActivity(new Intent(LoginActivity.this, SalesExecutive_Activity.class));
            finish();
        }
    }

    private void login() {
        String enteredLoginId = userEditText.getText().toString().trim();
        String enteredPassword = passEditText.getText().toString().trim();

        // Check if the entered login information matches the default values in SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String storedLoginId = preferences.getString(KEY_LOGIN_ID, "Admin@123");
        String storedPassword = preferences.getString(KEY_PASSWORD, "Admin@123");

        if (enteredLoginId.equals(storedLoginId) && enteredPassword.equals(storedPassword)) {
            // The entered login information is found in SharedPreferences
            // Show a toast message indicating successful login
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

            // Set the flag indicating admin is logged in
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("adminLoggedIn", true);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, Admin_Activity.class);
            startActivity(intent);
            finish();
        } else {
            // If the entered information does not match the values in SharedPreferences,
            // check in Firebase Realtime Database
            checkLoginInFirebase(enteredLoginId, enteredPassword);
        }
    }

    private void checkLoginInFirebase(final String enteredLoginId, final String enteredPassword) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        databaseReference.orderByChild("username").equalTo(enteredLoginId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null && user.getPassword().equals(enteredPassword)) {
                                    // Password matches, login successful
                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                                    // Save the unique ID to SharedPreferences
                                    saveUniqueIdToSharedPreferences(userSnapshot.getKey());

                                    // Save the user role to SharedPreferences
                                    saveUserRoleToSharedPreferences(user.getRole());

                                    startLocationUpdateService();
                                    Intent intent = new Intent(LoginActivity.this, SalesExecutive_Activity.class);
                                    startActivity(intent);
                                    return;
                                }
                            }
                        }

                        // No matching user or password found in Firebase
                        Toast.makeText(LoginActivity.this, "Incorrect login information", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void startLocationUpdateService() {
        Intent serviceIntent = new Intent(this, LocationUpdateService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void saveUniqueIdToSharedPreferences(String uniqueId) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("uniqueId", uniqueId);
        editor.apply();
    }
    // After successful login in checkLoginInFirebase method
    private void saveUserRoleToSharedPreferences(String userRole) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userRole", userRole);
        editor.apply();
    }


}

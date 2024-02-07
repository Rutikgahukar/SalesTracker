package com.example.locationdetect;

import static com.example.locationdetect.LoginActivity.PREF_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class Admin_Activity extends AppCompatActivity {
    Button add,track,Logout;
    TextView registeredUsersCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        add = findViewById(R.id.add);
        track = findViewById(R.id.track);
        Logout = findViewById(R.id.adminLogout);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear SharedPreferences to log out admin
                SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().remove("adminLoggedIn").apply();

                // Navigate back to LoginActivity
                Intent intent = new Intent(Admin_Activity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Optional: Close this activity to prevent going back to it using the back button
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Admin_Activity.this, AddSalesExecutive.class);
                startActivity(intent);
            }
        });

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Admin_Activity.this, TrackSalesExecutive.class);
                startActivity(intent);
            }
        });

        registeredUsersCount = findViewById(R.id.registeredUsersCount);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                registeredUsersCount.setText("Registered Users: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Admin_Activity.this, "Error fetching user count", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
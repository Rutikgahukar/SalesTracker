package com.example.locationdetect;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private DatabaseReference databaseReference;

    private TextView userNameTextView;
    private String selectedUid;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        userNameTextView = findViewById(R.id.userNameTextView);
        // Retrieve selected UID from Intent extras using consistent key
        selectedUid = getIntent().getStringExtra("selectedUid");

        if (selectedUid != null) {
            displayUserLocations(selectedUid);
            fetchUserName(selectedUid);
        }

        ImageView imageBackArrow = findViewById(R.id.Imagebackarrow);
        imageBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Or use Intent to navigate back to the desired activity
            }
        });


        ImageView imageDatePicker = findViewById(R.id.Imagedatepicker);
        imageDatePicker.setOnClickListener(v -> {
            // Show the DatePickerFragment
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.show(getSupportFragmentManager(), datePickerFragment.getTag());
            Toast.makeText(this, "Apply Filter", Toast.LENGTH_SHORT).show();
        });

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recyclerViewLocationUpdates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //spinner
        Spinner locationSpinner = findViewById(R.id.locationSpinner);
        // Define the spinner items
        String[] spinnerItems = {"Historical Locations", "Recent Location"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        locationSpinner.setAdapter(spinnerAdapter);

        // Inside your Spinner's OnItemSelectedListener
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = spinnerItems[position];
                if (selectedItem.equals("Historical Locations")) {
                    String fromDate = "2023-01-01"; // Your start date
                    String toDate = "2023-12-31";   // Your end date
                    filterLocationsByDates(fromDate, toDate);
                } else if (selectedItem.equals("Recent Location")) {
                    Toast.makeText(MapsActivity.this, "Displaying today's location entries only.", Toast.LENGTH_LONG).show();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDate = sdf.format(new Date());

                    filterLocationsByDates(currentDate, currentDate);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing, if required
            }
        });

    }



    public void filterLocationsByDates(String fromDate, String toDate) {
        if (selectedUid != null) {
            DatabaseReference userLocationsRef = databaseReference.child(selectedUid);

            userLocationsRef.orderByChild("date")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            List<LocationUpdateModel> filteredLocations = new ArrayList<>();
                            googleMap.clear();

                            List<DataSnapshot> snapshots = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                snapshots.add(snapshot);
                            }

                            for (int i = snapshots.size() - 1; i >= 0; i--) {
                                DataSnapshot snapshot = snapshots.get(i);
                                String date = snapshot.child("date").getValue(String.class);
                                String time = snapshot.child("time").getValue(String.class);
                                Double latitudeDouble = snapshot.child("latitude").getValue(Double.class);
                                Double longitudeDouble = snapshot.child("longitude").getValue(Double.class);

                                if (date != null && time != null && latitudeDouble != null && longitudeDouble != null) {
                                    String dateTime = date + " " + time; // Combine date and time
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    try {
                                        Date locationDate = sdf.parse(dateTime);
                                        long locationTimestamp = locationDate.getTime(); // Get timestamp

                                        SimpleDateFormat sdfFormatted = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
                                        Date fromDateParsed = sdfFormatted.parse(fromDate + " 12:00:00 AM");
                                        Date toDateParsed = sdfFormatted.parse(toDate + " 11:59:59 PM");

                                        // Compare if the locationTimestamp is within the specified range
                                        if (locationTimestamp >= fromDateParsed.getTime() && locationTimestamp <= toDateParsed.getTime()) {
                                            double latitude = latitudeDouble;
                                            double longitude = longitudeDouble;

                                            LatLng location = new LatLng(latitude, longitude);

                                            // Converting coordinates to human-readable address
                                            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                                            try {
                                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                                if (addresses != null && addresses.size() > 0) {
                                                    String addressText = addresses.get(0).getAddressLine(0);
                                                    LocationUpdateModel locationhistrory = new LocationUpdateModel(
                                                            addressText,
                                                            date,
                                                            time,
                                                            latitude,
                                                            longitude
                                                    );
                                                    filteredLocations.add(locationhistrory);
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            // Add marker for this location
                                            googleMap.addMarker(new MarkerOptions().position(location));
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // Set the filtered locations to the RecyclerView adapter
                            LocationUpdatesAdapter locationUpdatesAdapter = new LocationUpdatesAdapter(filteredLocations);
                            recyclerView.setAdapter(locationUpdatesAdapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MapsActivity.this, "Error fetching locations", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("MapsActivity", "Selected UID is null. Cannot filter locations.");
            // You can display a toast or a message to the user
            Toast.makeText(MapsActivity.this, "Unable to filter locations. Please select a user.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserName(String selectedUid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(selectedUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("name").getValue(String.class);
                    if (userName != null) {
                        userNameTextView.setText(userName); // Set only the user's name
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displayUserLocations(String selectedUid) {
        databaseReference = FirebaseDatabase.getInstance().getReference("locations");
        DatabaseReference userLocationsRef = databaseReference.child(selectedUid);

        userLocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                googleMap.clear();
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.BLUE);

                List<LatLng> coordinates = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double latitudeDouble = snapshot.child("latitude").getValue(Double.class);
                    Double longitudeDouble = snapshot.child("longitude").getValue(Double.class);

                    if (latitudeDouble != null && longitudeDouble != null) {
                        double latitude = latitudeDouble;
                        double longitude = longitudeDouble;

                        LatLng location = new LatLng(latitude, longitude);
                        coordinates.add(location);

                        googleMap.addMarker(new MarkerOptions().position(location));
                    }
                }

                polylineOptions.addAll(coordinates);
                googleMap.addPolyline(polylineOptions);

                if (!coordinates.isEmpty()) {
                    LatLng lastLatLng = coordinates.get(coordinates.size() - 1);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 15));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Error fetching locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
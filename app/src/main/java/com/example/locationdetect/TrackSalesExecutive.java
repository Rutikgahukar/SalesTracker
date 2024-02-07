package com.example.locationdetect;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TrackSalesExecutive extends AppCompatActivity {

    private ListView userListView;
    private CustomAdapter listAdapter;
    private List<String> userList;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_sales_executive);

        // Initialize ListView and populate user list
        userListView = findViewById(R.id.list);
        searchBar = findViewById(R.id.searchBar);


        userList = new ArrayList<>();
        listAdapter = new CustomAdapter(this, userList); // Use CustomAdapter
        userListView.setAdapter(listAdapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = userList.get(position);
                String selectedUid = extractUidFromSelectedItem(selectedItem);
                if (selectedUid != null) {
                    navigateToMapActivity(selectedUid);
                }
            }
        });

        // Populate user list from Firebase
        fetchUserList();


        // Implement search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                listAdapter.getFilter().filter(charSequence); // Apply filter to adapter
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void fetchUserList() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    String userName = snapshot.child("username").getValue(String.class);
                    if (uid != null && userName != null) {
                        userList.add(userName + " (UID: " + uid + ")");
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TrackSalesExecutive.this, "Error fetching user list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractUidFromSelectedItem(String selectedItem) {
        int start = selectedItem.lastIndexOf("(") + 5;
        int end = selectedItem.lastIndexOf(")");
        return selectedItem.substring(start, end).trim();
    }

    private void navigateToMapActivity(String selectedUid) {
        Intent intent = new Intent(TrackSalesExecutive.this, MapsActivity.class);
        intent.putExtra("selectedUid", selectedUid);
        startActivity(intent);
    }
}

package com.example.locationdetect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class AddSalesExecutive extends AppCompatActivity {

    static final String PREF_NAME = "LoginPrefs";

    private EditText nameEditText, surnameEditText, emailEditText, passEditText,userEditText;
    private Spinner genderSpinner;
    private Button loginButton , logOutButton;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sales_executive);

        // Find the UI elements by their IDs
        nameEditText = findViewById(R.id.name);
        surnameEditText = findViewById(R.id.surname);
        emailEditText = findViewById(R.id.email);
        passEditText = findViewById(R.id.pass);
        userEditText = findViewById(R.id.username);


        genderSpinner = findViewById(R.id.genderSpinner);
        setupGenderSpinner();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddSalesExecutive.this, "User Added Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddSalesExecutive.this,Admin_Activity.class);
                startActivity(intent);
                saveUserToFirebase();

            }
        });

        logOutButton = findViewById(R.id.BtnlogOut);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().clear().apply();

                // Navigate back to LoginActivity
                Intent intent = new Intent(AddSalesExecutive.this, Admin_Activity.class);
                startActivity(intent);
                finish(); //
            }
        });

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userRole = preferences.getString("userRole", "");
        if (userRole.equals("sales_executive")) {
            // User is a sales executive, proceed accordingly
            // For example:
            startActivity(new Intent(this, SalesExecutive_Activity.class));

        } else if (userRole.equals("admin")) {
            // User is an admin, proceed accordingly
            startActivity(new Intent(this, Admin_Activity.class));
        }
//        else {
//            // No role found, navigate to LoginActivity
//            startActivity(new Intent(this, SalesExecutive_Activity.class));
//            finish(); // Optional: Close this activity to prevent going back to it using the back button
//        }
    }

    private void saveUserToFirebase() {
        String name = nameEditText.getText().toString();
        String surname = surnameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passEditText.getText().toString();
        String username = userEditText.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();
         String role = logOutButton.getText().toString();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(username)) {
            String userId = mDatabase.push().getKey();
            User user = new User(name, surname, email, password, username, gender,role );
            mDatabase.child("users").child(userId).setValue(user);

            Toast.makeText(AddSalesExecutive.this, "User Added Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddSalesExecutive.this, Admin_Activity.class);
            startActivity(intent);
        } else {
            Toast.makeText(AddSalesExecutive.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGenderSpinner() {
        // Define an array adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        genderSpinner.setAdapter(adapter);

        // Set a listener to handle item selections in the spinner
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected gender
                String selectedGender = parentView.getItemAtPosition(position).toString();
                Toast.makeText(AddSalesExecutive.this, "Selected Gender: " + selectedGender, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });


    }

}

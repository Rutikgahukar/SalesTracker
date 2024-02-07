package com.example.locationdetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<String> {

    public CustomAdapter(Context context, List<String> userList) {
        super(context, R.layout.list_item, userList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        String currentUser = getItem(position);

        // Extract username and UID from the user string
        String username = extractUsernameFromUserString(currentUser);
        String uid = extractUidFromUserString(currentUser);

        // Set the extracted data to the TextViews and ImageView in the list item layout
        TextView usernameTextView = listItemView.findViewById(R.id.usernameTextView);
        TextView uidTextView = listItemView.findViewById(R.id.uniqueIdTextView);
        ImageView contactIcon = listItemView.findViewById(R.id.contactIcon);

        // Set the background of the ImageView with the custom drawable
        Drawable customBackground = generateRoundBackground(username);
        contactIcon.setImageDrawable(customBackground);

        usernameTextView.setText(username);
//    uidTextView.setText("UID: " + uid);

        // Set default background for the contact icon (you can set your own drawable)
        contactIcon.setBackgroundResource(R.drawable.round_background);

        if (!uid.isEmpty()) {
            uidTextView.setText("UID: " + uid);
            uidTextView.setVisibility(View.INVISIBLE);
        } else {
            uidTextView.setVisibility(View.GONE);
        }

        return listItemView;
    }

    // Method to create a custom rounded background drawable with the first letter
    private Drawable generateRoundBackground(String username) {
        // Get the first character of the username
        char firstLetter = username.charAt(0);

        // Create a ShapeDrawable for the background
        ShapeDrawable background = new ShapeDrawable(new OvalShape());
        background.getPaint().setColor(Color.parseColor("#DE670C"));

        // Create a Bitmap to hold the text (first letter)
        Bitmap bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888); // Set your desired dimensions
        Canvas canvas = new Canvas(bitmap);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE); // Set text color
        textPaint.setTextSize(24); // Set text size
        textPaint.setAntiAlias(true);

        // Draw the first letter in the center of the bitmap
        canvas.drawCircle(20, 20, 20, background.getPaint());
        canvas.drawText(String.valueOf(firstLetter).toUpperCase(), 12, 28, textPaint); // Adjust text position

        // Convert the Bitmap to a Drawable
        return new BitmapDrawable(getContext().getResources(), bitmap);
    }

    private String extractUsernameFromUserString(String userString) {
        // Implement logic to extract the username from the userString
        // Example: "John Doe (UID: 123)" -> "John Doe"
        // Replace this with your actual logic
        return userString.split("\\(")[0].trim();
    }

    private String extractUidFromUserString(String userString) {
        // Implement logic to extract the UID from the userString
        // Example: "John Doe (UID: 123)" -> "123"
        // Replace this with your actual logic
        return userString.split("UID:")[1].replaceAll("[^0-9]", "").trim();
    }
}
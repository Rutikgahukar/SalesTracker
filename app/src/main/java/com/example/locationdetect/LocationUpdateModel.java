package com.example.locationdetect;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationUpdateModel {
    private String address;
    private String date;
    private String time;
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Constructor
    public LocationUpdateModel(String address, String date, String time , double latitude, double longitude) {
        this.address = address;
        this.date = date;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime12hr() {
        try {
            // Parse the existing time in 24-hour format
            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date timeDate = timeFormat24.parse(time);

            // Convert it to 12-hour format
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return timeFormat12.format(timeDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return ""; // Handle the exception as per your requirement
        }
    }
}

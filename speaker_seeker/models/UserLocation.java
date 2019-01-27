package com.example.android.speaker_seeker.models;

public class UserLocation {

    private double latitude;
    private double longitude;

    public UserLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(UserLocation.class)
    }

    public UserLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {

        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

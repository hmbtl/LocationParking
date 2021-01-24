package com.hmbtl.locationparking.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by anar on 11/17/17.
 */

public class ParkingLocation {
    private double longitude, latitude, bearing;

    public ParkingLocation(double latitude, double longitude, double bearing){
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getBearing() {
        return bearing;
    }

    public LatLng toLatLng(){
        return new LatLng(this.latitude, this.longitude);
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    @Override
    public String toString() {
        return "Longitude: " + this.longitude + " | Latitude: " + this.latitude + " | Bearing: " + this.bearing;
    }
}

package com.hmbtl.locationparking.models;

import com.google.android.gms.maps.model.Marker;

import java.util.Date;

/**
 * Created by anar on 11/22/17.
 */

public class ParkingShare {

    private int shareId, duration, distance;
    private Date shareDate;
    private ParkingLocation parkingLocation;
    private User user;
    private Marker marker;
    private int requestId;

    public ParkingShare(User user, int shareId, int duration, ParkingLocation parkingLocation, int distance, Date shareDate){
        this.user = user;
        this.shareId = shareId;
        this.duration = duration;
        this.parkingLocation = parkingLocation;
        this.shareDate = shareDate;
        this.distance = distance;
        this.requestId = 0;
    }


    public int getDistance() {
        return distance;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }


    public int getShareId() {
        return shareId;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getShareDate() {
        return shareDate;
    }

    public void setShareDate(Date shareDate) {
        this.shareDate = shareDate;
    }

    public ParkingLocation getParkingLocation() {
        return parkingLocation;
    }

    public void setParkingLocation(ParkingLocation parkingLocation) {
        this.parkingLocation = parkingLocation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && this.shareId == ((ParkingShare) obj).getShareId();
    }
}

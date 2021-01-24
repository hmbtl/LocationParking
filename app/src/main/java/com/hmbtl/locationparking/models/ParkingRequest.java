package com.hmbtl.locationparking.models;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by anar on 11/15/17.
 */

public class ParkingRequest {

    private User user;
    private int distance;
    private int status;
    private int requestId;
    private Marker marker;

    public ParkingRequest(int requestId, User user, int distance, int status){
        this.user = user;
        this.distance = distance;
        this.status = status;
        this.requestId = requestId;
    }

    public ParkingRequest(int requestId, User user, int distance){
        this(requestId, user, distance, 0);
    }


    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getStatus() {
        return status;
    }

    public int getDistance() {
        return distance;
    }

    public User getUser() {
        return user;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

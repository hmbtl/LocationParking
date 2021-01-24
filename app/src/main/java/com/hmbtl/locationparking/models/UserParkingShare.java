package com.hmbtl.locationparking.models;

import android.os.CountDownTimer;

import com.google.android.gms.maps.model.Marker;

import java.util.Date;

/**
 * Created by anar on 11/20/17.
 */

public class UserParkingShare {
    private Marker marker;
    private User user;
    private int distance, shareId, duration;
    private Date requestDate;
    private CountDownTimer countDown;

    public UserParkingShare(int shareId, User user, int duration){
        this(null, shareId, user, duration, 0, null);
    }

    public UserParkingShare(Marker marker, int shareId, User user, int duration, int distance, Date requestDate){
        this.shareId = shareId;
        this.marker = marker;
        this.user = user;
        this.duration = duration;
        this.distance = distance;
        this.requestDate = requestDate;
    }

    public UserParkingShare(Marker marker, int shareId, User user, int duration, int distance) {
        this(marker, shareId, user, duration, distance, null);
    }

    public UserParkingShare(Marker marker, int shareId, User user, int duration) {
        this(marker, shareId, user, duration, 0, null);
    }

    public UserParkingShare(int shareId, User user, int duration, int distance, Date requestDate){
        this(null, shareId, user, duration, distance, requestDate);
    }

    public UserParkingShare(int shareId, User user, int duration, Date requestDate){
        this(null, shareId, user, duration, 0, requestDate);
    }

    public void setCountDown(CountDownTimer countDown) {
        this.countDown = countDown;
    }

    public CountDownTimer getCountDown() {
        return countDown;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getDistance() {
        return distance;
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

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
}

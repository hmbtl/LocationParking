package com.hmbtl.locationparking.models;

/**
 * Created by anar on 11/24/17.
 */

public class MyParkingShare {
    private int shareId,  duration;

    public MyParkingShare(int shareId, int duration){
        this.shareId = shareId;
        this.duration = duration;
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
}

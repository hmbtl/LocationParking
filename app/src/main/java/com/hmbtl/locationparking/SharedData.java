package com.hmbtl.locationparking;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hmbtl.locationparking.models.ParkingLocation;
import com.hmbtl.locationparking.models.ParkingShare;
import com.hmbtl.locationparking.models.Profile;

/**
 * Created by anar on 10/30/17.
 */

public class SharedData {

    private static SharedData sInstance;
    private static SharedPreferences prefs;
    protected Context mContext;
    private SharedPreferences.Editor editor;

    private static final String PREFS_FILE_NAME  = "com.hmbtl.locationparking";
    private static final String PREFS_AUTH_HEADER  = "com.hmbtl.locationparking.auth_header";
    private static final String PREFS_PROFILE  = "com.hmbtl.locationparking.profile";
    private static final String PREFS_IS_LOGGED_IN  = "com.hmbtl.locationparking.is_logged_in";
    private static final String PREFS_FIREBASE_TOKEN = "com.hmbtl.locationparking.firebase_token";
    private static final String PREFS_IS_ACCEPTED = "com.hmbtl.locationparking.is_accepted";
    private static final String PREFS_ACCEPTED_SHARE = "com.hmbtl.locationparking.accepted_share";
    private static final String PREFS_ACCEPTED_DESTINATION = "com.hmbtl.locationparking.destination";
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    private static final String PREFS_IS_REACHED_SHOWN = "requesting_locaction_updates";
    private static final String PREFS_ACCEPTED_REQUEST_ID = "com.hmbtl.locationparking.request_id";



    private SharedData(Context context){
        mContext = context;
        prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SharedData getInstance(Context context){
        if(sInstance == null){
            sInstance = new SharedData(context.getApplicationContext());
        }
        return sInstance;
    }


    public void setAuthHeader(String header){
        editor.putString(PREFS_AUTH_HEADER, header);
        editor.apply();
    }

    public  String getAuthHeader(){
        return prefs.getString(PREFS_AUTH_HEADER, "");
    }


    public void setProfile(Profile profile){
        Gson gson = new Gson();
        String profileString = gson.toJson(profile, Profile.class);

        
        editor.putString(PREFS_PROFILE, profileString);
        editor.apply();
    }

    public  void setLoggedIn(boolean isLoggedIn){
        
        editor.putBoolean(PREFS_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn(){
        return prefs.getBoolean(PREFS_IS_LOGGED_IN, false);
    }

    public Profile getProfile(){
        Gson gson = new Gson();
        return gson.fromJson(prefs.getString(PREFS_PROFILE,""), Profile.class);
    }


    public void setFirebaseToken(String token){
        editor.putString(PREFS_FIREBASE_TOKEN,token);
        editor.apply();
    }

    public String getFirebaseToken(){
        return prefs.getString(PREFS_FIREBASE_TOKEN,null);
    }


    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     */
    public boolean requestingLocationUpdates() {
        return prefs
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public void setRequestingLocationUpdates(boolean requestingLocationUpdates) {
        editor.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }


    public void setAccepted(boolean isAccepted){
        editor.putBoolean(PREFS_IS_ACCEPTED, isAccepted).apply();
    }

    public boolean isAccepted(){
        return prefs.getBoolean(PREFS_IS_ACCEPTED, false);
    }


    public void setAcceptedShare(ParkingShare parkingShare){
        Gson gson = new GsonBuilder().serializeNulls().create();
        String valuToSave = gson.toJson(parkingShare, ParkingShare.class);
        editor.putString(PREFS_ACCEPTED_SHARE, valuToSave);
    }

    public ParkingShare getAcceptedShare(){
        Gson gson = new Gson();
        String fromValue = prefs.getString(PREFS_ACCEPTED_SHARE, null);
        return gson.fromJson(fromValue, ParkingShare.class);
    }

    public void setDestination(ParkingLocation parkingLocation){
        Gson gson = new GsonBuilder().serializeNulls().create();
        String valuToSave = gson.toJson(parkingLocation, ParkingLocation.class);
        editor.putString(PREFS_ACCEPTED_DESTINATION, valuToSave).apply();
    }

    public int getAcceptedRequestId(){
        return prefs.getInt(PREFS_ACCEPTED_REQUEST_ID, 0);
    }

    public void setAcceptedRequestId(int requestId){
        editor.putInt(PREFS_ACCEPTED_REQUEST_ID, requestId).apply();
    }

    public boolean isReached(){
        return prefs.getBoolean(PREFS_IS_REACHED_SHOWN, false);
    }

    public void setReached(boolean isReachedShown){
        editor.putBoolean(PREFS_IS_REACHED_SHOWN, isReachedShown).apply();
    }

    public ParkingLocation getDestination(){
        Gson gson = new Gson();
        String fromValue = prefs.getString(PREFS_ACCEPTED_DESTINATION, "");
        return gson.fromJson(fromValue, ParkingLocation.class);
    }
}

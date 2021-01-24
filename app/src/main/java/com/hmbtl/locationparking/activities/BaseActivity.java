package com.hmbtl.locationparking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.hmbtl.locationparking.Constants;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.SharedData;
import com.hmbtl.locationparking.api.ApiListener;
import com.hmbtl.locationparking.api.BasicApi;
import com.hmbtl.locationparking.fragments.MessagesFragment;
import com.hmbtl.locationparking.fragments.ParkingShareFragment;
import com.hmbtl.locationparking.fragments.ProfileFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by anar on 11/7/17.
 */

public class BaseActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment messagesFragment, shareFragment, profileFragment;
    private static final String FRAGMENT_SHARE = "FRAGMENT_SHARE";
    private static final String FRAGMENT_PROFILE = "FRAGMENT_PROFILE";
    private static final String FRAGMENT_MESSAGES = "FRAGMENT_MESSAGES";
    private static final String FRAGMENT_HISTORY = "FRAGMENT_HISTORY";
    private static final String FRAGMENT_FEED = "FRAGMENT_FEED";

    private final String TAG = "BaseActivity";
    private String  savedFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        registerFirebaseToken();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.action_inbox:
                        showFragment(new MessagesFragment());
                        break;

                    case R.id.action_feed:

                        showFragment(new MessagesFragment());
                        break;
                    case R.id.action_history:

                        showFragment(new MessagesFragment());
                        break;

                    case R.id.action_profile:

                        showFragment(new ProfileFragment());
                        break;

                    case R.id.action_share:
                        showFragment(new ParkingShareFragment());
                        break;

                }


                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_share);

    }


    private void showFragment (Fragment fragment){
        String backStackName =  fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();


        Log.e(TAG, "NEW: " + backStackName);
        Log.e(TAG, "PRE: " + savedFragment);

        if(!backStackName.equals(savedFragment)){
            if(manager.findFragmentByTag(backStackName) == null){
                FragmentTransaction ft = manager.beginTransaction();
                ft.add(R.id.frame_layout, fragment, backStackName);
                ft.setTransition(FragmentTransaction.TRANSIT_NONE);

                if(savedFragment != null)
                    ft.hide(manager.findFragmentByTag(savedFragment));

                ft.commit();
            } else {
                FragmentTransaction ft = manager.beginTransaction();
                ft.show(manager.findFragmentByTag(backStackName)).hide(manager.findFragmentByTag(savedFragment))
                .commit();
            }
        }

        savedFragment = backStackName;
    }

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() != R.id.action_share){
            bottomNavigationView.setSelectedItemId(R.id.action_share);
        } else {
            finish();
        }
    }

    private void registerFirebaseToken(){
        try {
            JSONObject json = new JSONObject();
            json.put("firebase_token", SharedData.getInstance(this).getFirebaseToken());
            BasicApi.getInstance(this).
                    sendRequest("notification/token", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                            Log.e("Token","Registered");
                        }

                        @Override
                        public void onFailure() {

                        }

                        @Override
                        public void onError(int code) {

                        }

                        @Override
                        public void onData(Object dataJSON) {

                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ParkingShareFragment.REQUEST_CODE_FINE_LOCATION){
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(ParkingShareFragment.class.getName());
            if(fragment != null){
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ParkingShareFragment.REQUEST_CHECK_SETTINGS){
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(ParkingShareFragment.class.getName());
            if(fragment != null){
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}

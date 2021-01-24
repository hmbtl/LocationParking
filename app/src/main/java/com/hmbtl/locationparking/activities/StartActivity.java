package com.hmbtl.locationparking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hmbtl.locationparking.SharedData;

/**
 * Created by anar on 11/8/17.
 */

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(SharedData.getInstance(this).isLoggedIn() && SharedData.getInstance(this).getProfile() != null){
            Intent intent = new Intent(this, BaseActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }


}

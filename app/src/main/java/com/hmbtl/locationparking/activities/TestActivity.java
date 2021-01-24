package com.hmbtl.locationparking.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.views.ProgressButton;

/**
 * Created by anar on 11/27/17.
 */

public class TestActivity extends AppCompatActivity {
    ProgressButton progressButton;
    Button stopButton;
    TextView text;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        progressButton = (ProgressButton) findViewById(R.id.itemShareSendRequestButton);
        text = (TextView) findViewById(R.id.itemShareTimer);
        progressButton.setOnClickListener(onClickListener);
        text.setOnClickListener(onClickListener);
        //stopButton.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view == text){
                progressButton.stopAnimation();
            } else if (view == progressButton){
                progressButton.startAnimation();

            }
        }
    };
}

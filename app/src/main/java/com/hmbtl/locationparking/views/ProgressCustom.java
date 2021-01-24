package com.hmbtl.locationparking.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.hmbtl.locationparking.R;

/**
 * Created by anar on 10/30/17.
 */

public class ProgressCustom extends ProgressDialog {

    public ProgressCustom(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        setCancelable(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }
}

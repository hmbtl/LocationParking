package com.hmbtl.locationparking.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hmbtl.locationparking.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by anar on 11/6/17.
 */

public class RegisterActivity extends AppCompatActivity {


    private TextView pageTitle, pageSubtitle;
    private EditText firstNameEdit, lastNameEdit, emailEdit, passwordEdit;
    private ImageButton nextButton, prevButton;

    private LinearLayout nameLayout, emailLayout, passwordLayout;

    private List<PageView> pageList = new LinkedList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        initViews();

        initPages();
    }



    private void setPage(int position){
       // PageView pageView = pageList.get(position);


    }


    private void initViews(){

        nameLayout = (LinearLayout) findViewById(R.id.name_layout) ;
        emailLayout = (LinearLayout) findViewById(R.id.layout_email) ;
        passwordLayout = (LinearLayout) findViewById(R.id.layout_password) ;

        pageTitle = (TextView) findViewById(R.id.page_title);
        pageSubtitle = (TextView) findViewById(R.id.page_sub_title);

        firstNameEdit = (EditText) findViewById(R.id.input_name);
        lastNameEdit = (EditText) findViewById(R.id.input_surname);
        emailEdit = (EditText) findViewById(R.id.input_email);
        passwordEdit = (EditText) findViewById(R.id.input_password);

        nextButton = (ImageButton) findViewById(R.id.button_next);

    }

    private void initPages(){
        pageList.add(new PageView(nameLayout, getString(R.string.registration_name_title)));
        pageList.add(new PageView(emailLayout, getString(R.string.registration_email_title), getString(R.string.registration_email_sub_title)));
        pageList.add(new PageView(passwordLayout, getString(R.string.registration_password_title), getString(R.string.registration_password_sub_title)));
    }


    class PageView {

        String title, subtitle;
        View view;

        PageView(View view,String title, String subtitle){
            this.view = view;
            this.title = title;
            this.subtitle = subtitle;
        }

        PageView(View view,String title){
            this.view = view;
            this.title = title;
            this.subtitle = null;
        }
    }


}

package com.hmbtl.locationparking.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.hmbtl.locationparking.Constants;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.SharedData;
import com.hmbtl.locationparking.api.ApiListener;
import com.hmbtl.locationparking.api.BasicApi;
import com.hmbtl.locationparking.models.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private Button registerButton, siginButton, facebookButton,loginButton;
    private EditText inputPassword, inputUsername;
    private TextInputLayout inputPasswordLabel, inputUsernameLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_facebook);

        initFacebook();
        initViews();


    }


    private void initFacebook(){
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.e("Facebook",loginResult.toString());

                /*
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra(Constants.INTENT_AUTH_TOKEN, loginResult.getAccessToken().getToken());
                intent.putExtra(Constants.INTENT_USER_ID, loginResult.getAccessToken().getUserId());
                startActivity(intent);
                finish();
                */

                authFacebook(loginResult.getAccessToken().getUserId(),loginResult.getAccessToken().getToken());

            }

            @Override
            public void onCancel() {
            }


            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookError",error.toString());
            }
        });

    }


    private void initViews(){
        facebookButton = (Button) findViewById(R.id.button_connect_facebook);
        loginButton = (Button) findViewById(R.id.button_login);
        registerButton = (Button) findViewById(R.id.button_register);

        inputPassword = (EditText) findViewById(R.id.input_password);
        inputUsername = (EditText) findViewById(R.id.input_username);

        inputPasswordLabel = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputUsernameLabel = (TextInputLayout) findViewById(R.id.input_layout_username);

        loginButton.setOnClickListener(onClick);
        facebookButton.setOnClickListener(onClick);
        registerButton.setOnClickListener(onClick);


        inputPassword.setTypeface(Typeface.DEFAULT);
        inputPassword.setTransformationMethod(new PasswordTransformationMethod());

    }





    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view == facebookButton){
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this,Arrays.asList("email", "public_profile", "user_birthday", "user_friends",
                        "user_photos") );
            } else if(view == loginButton){
                if(isFieldsCorrect()){
                    authLogin();
                }
            } else if(view == registerButton){
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        }
    };



    private boolean isFieldsCorrect(){
        boolean finalRes = true;
        String username = inputUsername.getText().toString().trim();

        if(username.isEmpty()){
            inputUsernameLabel.setError(getString(R.string.error_username));
            requestFocus(inputUsername);
            finalRes =  false;
        } else {
            inputUsernameLabel.setErrorEnabled(false);
        }


        String password = inputPassword.getText().toString().trim();
        if(password.isEmpty()){
            inputPasswordLabel.setError(getString(R.string.error_password));
            requestFocus(inputPassword);
            finalRes =  false;
        } else {
            inputPasswordLabel.setErrorEnabled(false);
        }

        return finalRes;
    }


    private void authLogin()  {
        try {
            String username = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            JSONObject json = new JSONObject();

                json.put("username",username);

            json.put("password",password);
            BasicApi.getInstance(this)
                    .sendRequest("auth/login", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure() {

                        }

                        @Override
                        public void onError(int code) {

                        }

                        @Override
                        public void onData(Object jsonObject) {

                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }






    private void authFacebook(String facebookId, String facebookToken){
       try {
            JSONObject json = new JSONObject();
            json.put("facebook_id",facebookId);
            json.put("facebook_token",facebookToken);
            BasicApi.getInstance(this)
                    .sendRequest("auth/facebook", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure() {

                        }

                        @Override
                        public void onError(int code) {

                        }

                        @Override
                        public void onData(Object data) {

                            try {
                                JSONObject dataJSON = (JSONObject) data;

                                int userId = dataJSON.getInt("user_id");
                                String token = dataJSON.getString("session_token");
                                String email = dataJSON.getString("email");
                                String firstName = dataJSON.getString("first_name");
                                String lastName = dataJSON.getString("last_name");
                                String picture = dataJSON.getString("profile_picture");

                                SharedData.getInstance(LoginActivity.this).setProfile(new Profile(userId, email, firstName, lastName, picture));
                                SharedData.getInstance(LoginActivity.this).setLoggedIn(true);
                                SharedData.getInstance(LoginActivity.this).setAuthHeader(token);

                                Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
                                startActivity(intent);
                                finish();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    private void checkActiveToken(){
        if(AccessToken.getCurrentAccessToken() != null){
            Log.e("Login","Is not expired yet");
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            intent.putExtra(Constants.INTENT_AUTH_TOKEN, AccessToken.getCurrentAccessToken().getToken());
            intent.putExtra(Constants.INTENT_USER_ID, AccessToken.getCurrentAccessToken().getUserId());
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}

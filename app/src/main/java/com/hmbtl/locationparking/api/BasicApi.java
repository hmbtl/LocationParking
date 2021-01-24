package com.hmbtl.locationparking.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.hmbtl.locationparking.Constants;
import com.hmbtl.locationparking.SharedData;
import com.hmbtl.locationparking.views.ProgressCustom;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by anar on 10/30/17.
 */

public class BasicApi {

    private final String SERVER_URL = "http://api.hmbtl.com";
    private ProgressCustom progressDialog;
    private Context context;

    private final String TAG = "BasicApi";

    private OkHttpClient client;
    private Gson gson = new Gson();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String authHeader = "";
    private static BasicApi sInstance;

    private BasicApi(Context context){
        this.context = context;
        this.client = new OkHttpClient();
    }

    public static synchronized BasicApi getInstance(Context context){
        if(sInstance == null){
            sInstance = new BasicApi(context.getApplicationContext());
        }
        return sInstance;
    }

    public void sendExternal(String url, int method, JSONObject params, final ApiListener apiListener){
        try {

            // Set Auth Header for the request
            authHeader = SharedData.getInstance(context).getAuthHeader();

            Request.Builder builder = new Request.Builder();

            if(params != null)
                Log.v(TAG, "Params to send: " + params.toString());

            if(!authHeader.equals(""))
                builder.header("Authorization", authHeader);


            if(method == Constants.HTTP_POST || method == Constants.HTTP_PUT){
                builder.url(url);
                String json = getParams(method,params);
                RequestBody body = RequestBody.create(JSON, json);

                if(method == Constants.HTTP_POST){
                    builder.post(body);
                } else {
                    builder.put(body);
                }
            } else {
                String query = getParams(method, params);
                builder.url(url + query);

                if(method == Constants.HTTP_GET){
                    builder.get();
                } else {
                    builder.delete();
                }
            }

            Request request  = builder.build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failure in OkHTTP with: " + e.toString());
                    apiListener.onFailure();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();

                    if(responseData == null || responseData.equals("") ){
                        Log.e(TAG, "Response is null");
                        apiListener.onFailure();
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String status = jsonObject.getString("status");

                            if(status.equalsIgnoreCase("ok")){
                                Log.v(TAG, "Result is okay");
                                //apiListener.onSuccess();
                                apiListener.onData(jsonObject);

                            } else if (status.equalsIgnoreCase("error")) {
                                JSONObject errorJson = jsonObject.getJSONObject("error");
                                int errorCode = errorJson.getInt("code");
                                String errorMessage = errorJson.getString("message");
                                Log.e(TAG, "Error received with code: " + errorCode + " | " + errorMessage);
                                apiListener.onError(errorCode);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON exception with: " + e.toString());
                            apiListener.onFailure();
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding exception with: " + e.toString());
            apiListener.onFailure();
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception with: " + e.toString());
            apiListener.onFailure();
            e.printStackTrace();
        }
    }

    public void sendRequest(String url, int method, JSONObject params, final ApiListener apiListener){
        try {

            // Set Auth Header for the request
            authHeader = SharedData.getInstance(context).getAuthHeader();

            Request.Builder builder = new Request.Builder();

            if(params != null)
                Log.v(TAG, "Params to send: " + params.toString());

            if(!authHeader.equals(""))
                builder.header("Authorization", authHeader);


            if(method == Constants.HTTP_POST || method == Constants.HTTP_PUT){
                builder.url(SERVER_URL + "/" + url);
                String json = getParams(method,params);
                RequestBody body = RequestBody.create(JSON, json);

                if(method == Constants.HTTP_POST){
                    builder.post(body);
                } else {
                    builder.put(body);
                }
            } else {
                String query = getParams(method, params);
                builder.url(SERVER_URL + "/" + url + "?" + query);

                if(method == Constants.HTTP_GET){
                    builder.get();
                } else {
                    builder.delete();
                }
            }

            Request request  = builder.build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failure in OkHTTP with: " + e.toString());
                    apiListener.onFailure();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();

                    if(responseData == null || responseData.equals("") ){
                        Log.e(TAG, "Response is null");
                        apiListener.onFailure();
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String status = jsonObject.getString("status");

                            if(status.equals("ok")){
                                Log.v(TAG, "Result is okay");
                                apiListener.onSuccess();

                                if(jsonObject.has("data")){
                                    Object data = jsonObject.get("data");
                                    Log.v(TAG, "Data received from server: " + data.toString());
                                    apiListener.onData(data);
                                }

                            } else if (status.equals("error")) {
                                JSONObject errorJson = jsonObject.getJSONObject("error");
                                int errorCode = errorJson.getInt("code");
                                String errorMessage = errorJson.getString("message");
                                Log.e(TAG, "Error received with code: " + errorCode + " | " + errorMessage);
                                apiListener.onError(errorCode);
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON exception with: " + e.toString());
                            apiListener.onFailure();
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Encoding exception with: " + e.toString());
            apiListener.onFailure();
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception with: " + e.toString());
            apiListener.onFailure();
            e.printStackTrace();
        }
    }

    public void sendRequest(String url, int method, ApiListener apiListener){
        sendRequest(url, method, null, apiListener);
    }

    public void sendRequest(String url,ApiListener apiListener){
        sendRequest(url, Constants.HTTP_GET, apiListener);
    }

    private String getParams(int method, JSONObject parameters) throws UnsupportedEncodingException, JSONException {
        String valueToSend = "";

        if(method == Constants.HTTP_POST || method == Constants.HTTP_PUT){
            valueToSend = parameters.toString();
        } else {
            if(parameters != null){
                StringBuilder result = new StringBuilder();
                Iterator<String> keys = parameters.keys();
                boolean isFirstValue  = true;
                while(keys.hasNext()){
                    String key = keys.next();
                    String value = parameters.getString(key);

                    if(isFirstValue)
                        isFirstValue = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(key,"UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(value, "UTF-8"));
                }

                valueToSend = result.toString();
            }

        }
        return valueToSend;
    }



}

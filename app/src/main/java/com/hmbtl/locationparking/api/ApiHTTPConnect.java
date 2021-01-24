package com.hmbtl.locationparking.api;

import android.content.Context;

import com.google.gson.Gson;
import com.hmbtl.locationparking.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

/**
 * Created by anar on 10/30/17.
 */

public class ApiHTTPConnect {

    final int TIMEOUT_READ = 5000;
    final int TIMEOUT_CONN = 10000;
    final String USER_AGENT = "Mozilla/5.0";
    final String ACCEPT_LANGUAGE = "en-US,en;q=0.5";
    private Context context;

    public ApiHTTPConnect(Context context){
        this.context = context;
    }

    public void sendRequest(String method, String url, JSONObject params){
        try {
            URL obj = new URL(url);


            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setReadTimeout(TIMEOUT_READ);
            connection.setConnectTimeout(TIMEOUT_CONN);
            connection.setRequestMethod(method);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept-Language",ACCEPT_LANGUAGE);

            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(getParams(method, params));
            dataOutputStream.flush();
            dataOutputStream.close();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }





    private String getParams(String method, JSONObject parameters) throws UnsupportedEncodingException, JSONException {
        String valueToSend = "";

        if(method.equals(Constants.HTTP_POST) || method.equals(Constants.HTTP_PUT)){
            Gson gson = new Gson();
            valueToSend = gson.toJson(parameters);
        } else {
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
        }
        return valueToSend;
    }

    private String getJSON(List<ApiParameter> parameters){
        String json = "";

        for(ApiParameter parameter: parameters){

        }

        return json;
    }


}

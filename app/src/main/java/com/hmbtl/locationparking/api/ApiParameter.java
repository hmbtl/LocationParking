package com.hmbtl.locationparking.api;

/**
 * Created by anar on 10/30/17.
 */

public class ApiParameter {

    private String key;
    private String value;

    public ApiParameter(String key, Object value){
        this.key = key;
        this.value = String.valueOf(value);
    }




    @Override
    public String toString() {
        return this.key + " : " + this.value;
    }
}

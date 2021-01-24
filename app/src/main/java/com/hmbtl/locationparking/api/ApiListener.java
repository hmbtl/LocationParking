package com.hmbtl.locationparking.api;

/**
 * Created by anar on 10/30/17.
 */

public interface ApiListener {

    void onSuccess();

    void onFailure();

    void onError(int code);

    void onData(Object jsonObject);
}

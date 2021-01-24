package com.hmbtl.locationparking.services;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by anar on 11/30/17.
 */

public class ActivityLifecycleCallbacksService implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i(activity.getClass().getSimpleName(), "onCreate(Bundle)");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onStart()");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onResume()");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onPause()");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i(activity.getClass().getSimpleName(), "onSaveInstanceState(Bundle)");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onStop()");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.i(activity.getClass().getSimpleName(), "onDestroy()");
    }
}

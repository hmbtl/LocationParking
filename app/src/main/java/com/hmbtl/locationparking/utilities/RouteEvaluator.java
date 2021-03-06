package com.hmbtl.locationparking.utilities;

import android.animation.TypeEvaluator;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by anar on 11/24/17.
 */

public class RouteEvaluator implements TypeEvaluator<LatLng> {
    @Override
    public LatLng evaluate(float t, LatLng startPoint, LatLng endPoint) {
        double lat = startPoint.latitude + t * (endPoint.latitude - startPoint.latitude);
        double lng = startPoint.longitude + t * (endPoint.longitude - startPoint.longitude);
        return new LatLng(lat,lng);
    }
}
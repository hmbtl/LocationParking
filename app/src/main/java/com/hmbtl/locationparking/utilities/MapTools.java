package com.hmbtl.locationparking.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.api.BasicApi;
import com.hmbtl.locationparking.api.LatLngInterpolator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by anar on 11/24/17.
 */


public class MapTools {

    private static int POLYGON_STROKE_WIDTH_PX = 20;
    private static final float ALPHA = 0.15f;
    private static String GOOGLE_API_KEY = "AIzaSyBrn-hx840Fm-x3KOkwdWNezPbnchMXohA";

    public static void drawPolyline(GoogleMap map, Marker startMarker, Marker endMarker){
        Polyline line = map.addPolyline(new PolylineOptions()
                .add(startMarker.getPosition(), endMarker.getPosition())
                .width(25)
                .color(Color.BLACK)
                .zIndex(10)
                .geodesic(true)
        );
    }



    private static float bearingBetweenLocations(LatLng latLng1,LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return (float) brng;
    }

    public static void animateMarker(double latitude, double longitude, float rotation, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();

            final LatLng endPosition = new LatLng(latitude, longitude);

            final float startRotation = marker.getRotation();

            final float toRotation = MapTools.bearingBetweenLocations(startPosition, endPosition);


            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator markerMoveAnimation = ValueAnimator.ofFloat(0, 1);
            markerMoveAnimation.setInterpolator(new LinearInterpolator());
            markerMoveAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });


            ValueAnimator rotationAnimation = ValueAnimator.ofFloat(0,1);
            rotationAnimation.setInterpolator(new LinearInterpolator());
            rotationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    try {
                        float v = valueAnimator.getAnimatedFraction();

                        float rot = v * toRotation + (1 - v) * startRotation;

                        marker.setRotation(-rot > 180 ? rot / 2 : rot);


                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });


            List<Animator> animators = new ArrayList<>();
            animators.add(markerMoveAnimation);
            animators.add(rotationAnimation);

            AnimatorSet animoSet = new AnimatorSet();
            animoSet.setDuration(1000);
            animoSet.playTogether(animators);
            animoSet.start();

        }
    }



    public static void drawCurvedLine(GoogleMap googleMap, Marker startMarker, Marker endMarker) {
        double lineLength = SphericalUtil.computeDistanceBetween(startMarker.getPosition(), endMarker.getPosition());
        double lineHeading = SphericalUtil.computeHeading(startMarker.getPosition(), endMarker.getPosition());

        double lineHeadingFirst = 0;
        double lineHeadingSecond = 0;

        if (lineHeading < 0) {
            lineHeadingFirst = lineHeading + 45;
            lineHeadingSecond = lineHeading + 135;
        } else {
            lineHeadingFirst = lineHeading - 45;
            lineHeadingSecond = lineHeadingSecond - 135;
        }

        LatLng latLngA = SphericalUtil.computeOffset(startMarker.getPosition(), lineLength / 3, lineHeadingFirst);
        LatLng latLngB = SphericalUtil.computeOffset(endMarker.getPosition(), lineLength / 3, lineHeadingSecond);

        PolylineOptions polylineOptions = CubicBezier(startMarker.getPosition(), latLngA, latLngB, endMarker.getPosition(), 0.0001, googleMap);
        googleMap.addPolyline(polylineOptions);
    }


    public static PolylineOptions CubicBezier(LatLng startPos, LatLng aPos, LatLng bPos, LatLng endPos, double resolution, GoogleMap map){

        double lat1 = startPos.latitude;
        double long1 = startPos.longitude;

        double lat2 = aPos.latitude;
        double lon2 = aPos.longitude;

        double lat3 = bPos.latitude;
        double long3 = bPos.longitude ;

        double lat4 = endPos.latitude;
        double long4 = endPos.longitude ;

        LinkedList<LatLng> points = new LinkedList<>();


        for (double i = 0; i <= 1; i+= resolution){
            points.add(getBezier(startPos, aPos, bPos, endPos, i));
        }

        LinkedList<LatLng> path = new LinkedList<>();

        for (int i = 0; i < points.size() - 1; i++){
            path.push(new LatLng(points.get(i).latitude, points.get(i).longitude));
            path.push(new LatLng(points.get(i + 1).latitude, points.get(i + 1).longitude));
        }

        PolylineOptions line = new PolylineOptions();
        line.width(POLYGON_STROKE_WIDTH_PX);
        line.color(Color.BLACK);
        line.addAll(path);
        line.geodesic(true);

        return line;
    }


    private static LatLng getBezier(LatLng startPos, LatLng aPos, LatLng bPos, LatLng endPos, double t){

        double b1 = t * t * t;
        double b2 = 3 * t * t * (1 - t);
        double b3 = 3 * t * (1 - t) * (1 - t);
        double b4 = (1 - t) * (1 - t) * (1 - t);

        double posX = startPos.latitude * b1 + aPos.latitude * b2 + bPos.latitude * b3 + endPos.latitude * b4;
        double posY = startPos.longitude * b1 + aPos.longitude * b2 + bPos.longitude * b3 + endPos.longitude * b4;

        return new LatLng(posX, posY);
    }


    public static String createDirectionsUrl(LatLng origin, LatLng dest){
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Set mode to driving
        String mode = "mode=driving";

        String key = "key=" + MapTools.GOOGLE_API_KEY;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&" + key;

        return url;
    }


    public static String millisecondsToClock(long milliseconds){

        int time = (int) (milliseconds/1000);

        int minute = time / 60;
        int seconds = time - minute * 60;

        return String.format("%02d",minute) + ":" + String.format("%02d",seconds);
    }

    public static String millisecondsToClockWithHour(long milliseconds){

        int time = (int) (milliseconds/1000);

        int seconds = (int) (milliseconds / 1000) % 60 ;
        int minutes = (int) ((milliseconds / (1000*60)) % 60);
        int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

        return String.format("%02d",hours) + ":" + String.format("%02d",minutes) + ":" + String.format("%02d",seconds);
    }

    public static float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    /*

    public static void showNavigationDialog(final Context context, final LatLng location){
        final AppCompatDialog dialog = new AppCompatDialog(context);
        dialog.setContentView(R.layout.dialog_navigate);
        dialog.setTitle("Navigation");

        final Button googleButton = (Button) dialog.findViewById(R.id.button_google_maps);
        final Button wazeButton = (Button) dialog.findViewById(R.id.button_waze);
        final Button navigateButton = (Button) dialog.findViewById(R.id.button_navigate);
        final Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == googleButton){
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+location.latitude +"," + location.longitude + "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);

                } else if(view == wazeButton){
                    String uri = "waze://?ll=" + location.latitude +", " + location.longitude +"&navigate=yes";
                    context.startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(uri)));
                } else if (view == navigateButton){
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+location.latitude +"," + location.longitude + "&mode=d");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);

                } else if (view == cancelButton){
                    dialog.dismiss();
                }
                dialog.dismiss();
            }
        };

        googleButton.setOnClickListener(onClickListener);
        wazeButton.setOnClickListener(onClickListener);
        navigateButton.setOnClickListener(onClickListener);
        cancelButton.setOnClickListener(onClickListener);


        dialog.show();

    }

*/

    public static void showNotification(Context context){
        Intent notificationIntent = new Intent(context, BasicApi.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.parked)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle("Navigation")
                .setContentIntent(resultPendingIntent)
                .setContentText("Person have stopped sharing his spot.")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH);

        builder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }
}
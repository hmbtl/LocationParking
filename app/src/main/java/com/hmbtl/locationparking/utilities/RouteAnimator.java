package com.hmbtl.locationparking.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by anar on 11/24/17.
 */

public class RouteAnimator {

    private GoogleMap googleMap;
    private List<LatLng> points;
    private Polyline backgroundPolyline;
    private Polyline foregroundPolyline;
    private PolylineOptions optionsForeground;
    private AnimatorSet firstRunAnimSet;
    private AnimatorSet secondLoopRunAnimSet;
    private final int GREY = Color.parseColor("#FFA7A6A6");

    private boolean isAnimating = false;

    public RouteAnimator(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public RouteAnimator(GoogleMap googleMap, List<LatLng> points) {
        this.googleMap = googleMap;
        this.points = points;
    }


    public void removeRoute() {
        //Reset the polylines
        if (foregroundPolyline != null) foregroundPolyline.remove();
        if (backgroundPolyline != null) backgroundPolyline.remove();
        isAnimating = false;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void animate() {
        if (this.points.size() != 0 && this.points != null) {
            isAnimating = true;

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            // Add all points to camera
            for (LatLng point : points) {
                builder.include(point);
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
            googleMap.animateCamera(cu);


            if (firstRunAnimSet == null) {
                firstRunAnimSet = new AnimatorSet();
            } else {
                firstRunAnimSet.removeAllListeners();
                firstRunAnimSet.end();
                firstRunAnimSet.cancel();

                firstRunAnimSet = new AnimatorSet();
            }
            if (secondLoopRunAnimSet == null) {
                secondLoopRunAnimSet = new AnimatorSet();
            } else {
                secondLoopRunAnimSet.removeAllListeners();
                secondLoopRunAnimSet.end();
                secondLoopRunAnimSet.cancel();

                secondLoopRunAnimSet = new AnimatorSet();
            }
            //Reset the polylines
            if (foregroundPolyline != null) foregroundPolyline.remove();
            if (backgroundPolyline != null) backgroundPolyline.remove();


            PolylineOptions optionsBackground = new PolylineOptions().add(points.get(0)).color(GREY).width(15);
            backgroundPolyline = googleMap.addPolyline(optionsBackground);

            optionsForeground = new PolylineOptions().add(points.get(0)).color(Color.BLACK).width(15);
            foregroundPolyline = googleMap.addPolyline(optionsForeground);

            final ValueAnimator percentageCompletion = ValueAnimator.ofInt(0, 100);
            percentageCompletion.setDuration(2000);
            percentageCompletion.setInterpolator(new DecelerateInterpolator());
            percentageCompletion.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    List<LatLng> foregroundPoints = backgroundPolyline.getPoints();

                    int percentageValue = (int) animation.getAnimatedValue();
                    int pointcount = foregroundPoints.size();
                    int countTobeRemoved = (int) (pointcount * (percentageValue / 100.0f));
                    List<LatLng> subListTobeRemoved = foregroundPoints.subList(0, countTobeRemoved);
                    subListTobeRemoved.clear();

                    foregroundPolyline.setPoints(foregroundPoints);
                }
            });
            percentageCompletion.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    foregroundPolyline.setColor(GREY);
                    foregroundPolyline.setPoints(backgroundPolyline.getPoints());
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });


            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), GREY, Color.BLACK);
            colorAnimation.setInterpolator(new AccelerateInterpolator());
            colorAnimation.setDuration(1200); // milliseconds

            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    foregroundPolyline.setColor((int) animator.getAnimatedValue());
                }

            });

            ObjectAnimator foregroundRouteAnimator = ObjectAnimator.ofObject(this, "routeIncreaseForward", new RouteEvaluator(), points.toArray());
            foregroundRouteAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            foregroundRouteAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    backgroundPolyline.setPoints(foregroundPolyline.getPoints());
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            foregroundRouteAnimator.setDuration(1600);
            //        foregroundRouteAnimator.start();

            firstRunAnimSet.playSequentially(foregroundRouteAnimator,
                    percentageCompletion);
            firstRunAnimSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    secondLoopRunAnimSet.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            secondLoopRunAnimSet.playSequentially(colorAnimation,
                    percentageCompletion);
            secondLoopRunAnimSet.setStartDelay(200);

            secondLoopRunAnimSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    secondLoopRunAnimSet.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            firstRunAnimSet.start();
        }
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    /**
     * This will be invoked by the ObjectAnimator multiple times. Mostly every 16ms.
     **/
    public void setRouteIncreaseForward(LatLng endLatLng) {
        List<LatLng> foregroundPoints = foregroundPolyline.getPoints();
        foregroundPoints.add(endLatLng);
        foregroundPolyline.setPoints(foregroundPoints);
    }
}
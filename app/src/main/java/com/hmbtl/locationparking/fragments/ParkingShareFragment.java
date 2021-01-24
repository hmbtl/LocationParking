package com.hmbtl.locationparking.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.hmbtl.locationparking.Constants;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.SharedData;
import com.hmbtl.locationparking.api.ParkingApi;
import com.hmbtl.locationparking.models.ParkingLocation;
import com.hmbtl.locationparking.models.ParkingRequest;
import com.hmbtl.locationparking.models.ParkingShare;
import com.hmbtl.locationparking.models.User;
import com.hmbtl.locationparking.sensors.Compass;
import com.hmbtl.locationparking.services.LocationUpdatesService;
import com.hmbtl.locationparking.utilities.MapTools;
import com.hmbtl.locationparking.utilities.PermissionUtils;
import com.hmbtl.locationparking.utilities.RouteAnimator;
import com.hmbtl.locationparking.views.CenterRequestView;
import com.hmbtl.locationparking.views.ProgressButton;
import com.hmbtl.locationparking.views.TimerView;
import com.hmbtl.locationparking.views.TopPanelShareView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by anar on 11/7/17.
 */

public class ParkingShareFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener
{

    private GoogleMap mMap;
    private AppCompatActivity mainActivity;

    private ImageButton myLocationButton;
    private ProgressButton parkYourCarButton;
    private ImageView parkingLocationSelectPin;
    private RelativeLayout mainLayout;
    private CenterRequestView requestView;
    private TopPanelShareView topPanelShareView;


    private ParkingShare acceptedParkingShare;
    private ProgressButton acceptedCancel;
    private CircularImageView acceptedProfile;
    private ProgressButton acceptedNavigate;
    private TextView acceptedName;
    private RelativeLayout acceptedPanel;


    private ParkingRequest handshakeParkingRequest;
    private ProgressButton handshakeConfirmButton, handshakeCancelButton;
    private CircularImageView handshakeProfile;
    private TextView handshakeName, handshakeDistance;
    private RelativeLayout handshakePanel;

    private Compass compass;

    private Marker myLocationMarker;
    private Marker myParkingMarker;

    private boolean isAccepted = false;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    private boolean isSharing = false;
    private int parkButtonState = 0;
    private int shareId = -1;

    private final int STATE_ADD_PARKING = 0;
    private final int STATE_REMOVE_PARKING = 1;
    private final int STATE_PARKING_DONE = 2;
    private final int STATE_SHARE_PARKING = 3;
    private final int STATE_STOP_SHARE = 4;

    public static final int REQUEST_CODE_FINE_LOCATION = 1;
    public static final int REQUEST_CHECK_SETTINGS = 2;
    static int UPDATE_INTERVAL = 10000; // SEC
    static int FATEST_INTERVAL = 5000; // SEC
    static int DISPLACEMENT = 10; // METERS
    static final float ALPHA = 0.15f;

    private boolean mPermissionDenied = false;
    private boolean mRequestingLocationUpdates = false;
    private View rootView;

    private RouteAnimator routeAnimator;
    private List<LatLng> path = new LinkedList<>();


    private ParkingApi parkingApi;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // The BroadcastReceiver used to listen from broadcasts from the service.

    private MyReceiver myReceiver;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // Ask for location updates
            askForLocationAccess();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_map, null, false);

        // Load Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Init all views
        initViews();

        // set Accepted false
        SharedData.getInstance(mainActivity).setAccepted(false);


                // Set Appcompat Activity
        mainActivity = (AppCompatActivity) getActivity();

        // Create receiver
        myReceiver = new MyReceiver();

        // Init general api
        parkingApi = new ParkingApi(mainActivity);

        // Init compass
        compass = new Compass(mainActivity);
        compass.setCompassChangeListener(onCompassChangeListener);
        Log.e("PP", "onCreateView");

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();

        filter.addAction(Constants.INTENT_FILTER_ADD_REQUEST);
        filter.addAction(Constants.INTENT_FILTER_CANCEL_REQUEST);
        filter.addAction(Constants.INTENT_FILTER_UPDATE_REQUEST);
        filter.addAction(Constants.INTENT_FILTER_USER_LOCATION);
        filter.addAction(Constants.INTENT_FILTER_STOP_SHARING);
        filter.addAction(Constants.INTENT_FILTER_DESTINATION_REACHED);


        LocalBroadcastManager.getInstance(mainActivity).registerReceiver((mMessageReceiver),
                filter
        );
    }

    @Override
    public void onResume() {
        super.onResume();



        LocalBroadcastManager.getInstance(mainActivity).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
        compass.start();
        Log.e("PP", "onResume");
    }


    @Override
    public void onPause() {
        super.onPause();

        compass.stop();
    }


    @Override
    public void onStart() {
        super.onStart();
        mainActivity.bindService(new Intent(mainActivity, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
        Log.e("PP", "onCreateView");
    }

    @Override
    public void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            mainActivity.unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mainActivity).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(mainActivity).unregisterReceiver(myReceiver);
        mService.stopSelf();
        super.onDestroy();

    }




    private void initViews(){
        myLocationButton = (ImageButton) rootView.findViewById(R.id.button_my_location);
        parkYourCarButton = (ProgressButton) rootView.findViewById(R.id.button_park_car);
        parkingLocationSelectPin = (ImageView) rootView.findViewById(R.id.parking_location_pin);
        requestView = (CenterRequestView) rootView.findViewById(R.id.request_view);
        topPanelShareView = (TopPanelShareView) rootView.findViewById(R.id.top_panel_share_view);

        // Add accepted views
        acceptedPanel = (RelativeLayout) rootView.findViewById(R.id.acceptedPanel);
        acceptedCancel = (ProgressButton) rootView.findViewById(R.id.acceptedCancel);
        acceptedName = (TextView) rootView.findViewById(R.id.acceptedName);
        acceptedNavigate = (ProgressButton) rootView.findViewById(R.id.acceptedNavigate);
        acceptedProfile = (CircularImageView) rootView.findViewById(R.id.acceptedProfile);

        acceptedCancel.setOnClickListener(onClick);
        acceptedNavigate.setOnClickListener(onClick);


        // Add handshake views
        handshakePanel = (RelativeLayout) rootView.findViewById(R.id.handshakePanel);
        handshakeCancelButton = (ProgressButton) rootView.findViewById(R.id.handshakeCancel);
        handshakeConfirmButton = (ProgressButton) rootView.findViewById(R.id.handshakeConfirm);
        handshakeName = (TextView) rootView.findViewById(R.id.handshakeName);
        handshakeDistance = (TextView) rootView.findViewById(R.id.handshakeDistance);
        handshakeProfile = (CircularImageView) rootView.findViewById(R.id.handshakeProfile);

        handshakeConfirmButton.setOnClickListener(onClick);
        handshakeCancelButton.setOnClickListener(onClick);


        // Hide accepted panel from map
        acceptedPanel.setVisibility(View.GONE);

        topPanelShareView.show();
        requestView.hide();

        topPanelShareView.setOnShareSendRequestClicked(onShareSendRequestClicked);
        requestView.setOnRequestClickListener(onRequestClickListener);

        myLocationButton.setOnClickListener(onClick);
        parkYourCarButton.setOnClickListener(onClick);

        myLocationButton.setVisibility(View.GONE);
        parkingLocationSelectPin.setVisibility(View.GONE);

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraIdleListener(onCameraIdleListener);
        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        // Set googleMap to show markers
        topPanelShareView.setMap(mMap);


        // Get last known parking
        getLastKnownParking();

        // Init route animator
        routeAnimator = new RouteAnimator(mMap);

    }


    private void getLastKnownParking(){
        parkingApi.getParking(new ParkingApi.ParkingApiResultListener() {
            @Override
            public void onSuccess(Object data) {
                ParkingLocation parkingLocation = (ParkingLocation) data;
                myParkingMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(parkingLocation.getLatitude(), parkingLocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.parked))
                        .anchor(0.5f, 0.5f)
                        .title("Your Parking")
                        .flat(false));

                setParkYourCarButton(STATE_SHARE_PARKING);
            }

            @Override
            public void onError(String message) {
                setParkYourCarButton(STATE_ADD_PARKING);
            }
        });
    }


    private void updateMyLocationMarker(Location location){
        if(myLocationMarker == null){
            myLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gps_location))
                    .anchor(0.5f, 0.5f)
                    .zIndex(0)
                    .flat(true));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16));

        } else {
            myLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }


    private void askForLocationAccess(){
        if(ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            PermissionUtils.requestPermission(mainActivity, REQUEST_CODE_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,true);
        } else {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mService.getLocationRequest());

            SettingsClient client = LocationServices.getSettingsClient(mainActivity);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(mainActivity, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    startLocationUpdates();
                }
            });

            task.addOnFailureListener(mainActivity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(mainActivity,
                                        REQUEST_CHECK_SETTINGS);

                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(myLocationMarker)){
            return true;
        } else if (marker.equals(myParkingMarker)) {
            if(!isSharing){
                setParkYourCarButton(STATE_REMOVE_PARKING);
            }
        } else {
            if(topPanelShareView != null){
                topPanelShareView.OnMarkerClick(marker);
            }
        }

        return false;
    }


    GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {

        }
    };


    GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = new GoogleMap.OnCameraMoveStartedListener() {
        @Override
        public void onCameraMoveStarted(int i) {
           /*
            if(myLocationMarker != null){
                LatLngBounds cameraBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                if(!cameraBounds.contains(myLocationMarker.getPosition())){
                    myLocationButton.setVisibility(View.VISIBLE);

                }
            }

            */
            myLocationButton.setVisibility(View.VISIBLE);
        }
    };

    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if(myParkingMarker != null && !isSharing) {
                setParkYourCarButton(STATE_SHARE_PARKING);
            }
        }
    };




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//        Log.e("Result",requestCode + " " + resultCode + " " + data.toString());

        if(requestCode == REQUEST_CHECK_SETTINGS){
            if(resultCode == RESULT_OK){
                startLocationUpdates();
            } else {
                askForLocationAccess();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        Log.e("Code",requestCode + " | " + permissions.toString());
        if (requestCode != REQUEST_CODE_FINE_LOCATION) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            if(mMap != null){
                askForLocationAccess();
            }

        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    private void startLocationUpdates() {
        //mMap.setMyLocationEnabled(true);
        if(!mRequestingLocationUpdates){
            mService.requestLocationUpdates();
            mRequestingLocationUpdates = true;
        }
    }



    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view == parkYourCarButton){

                if(myParkingMarker == null){

                    if(parkButtonState == STATE_ADD_PARKING){
                        parkingLocationSelectPin.setVisibility(View.VISIBLE);
                        setParkYourCarButton(STATE_PARKING_DONE);

                        topPanelShareView.hide();

                    } else if (parkButtonState == STATE_PARKING_DONE){
                        double latitude = mMap.getCameraPosition().target.latitude;
                        double longitude = mMap.getCameraPosition().target.longitude;
                        double bearing = mMap.getCameraPosition().bearing;

                        topPanelShareView.show();

                        final ParkingLocation parkingLocation = new ParkingLocation(latitude, longitude, bearing);

                        parkYourCarButton.startAnimation();

                        parkingApi.addParking(parkingLocation, new ParkingApi.ParkingApiResultListener() {

                            @Override
                            public void onSuccess(Object data) {
                                myParkingMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(parkingLocation.getLatitude(), parkingLocation.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.parked))
                                        .anchor(0.5f, 0.8f)
                                        .title("Your Parking")
                                        .flat(false));

                                parkingLocationSelectPin.setVisibility(View.GONE);

                                setParkYourCarButton(STATE_SHARE_PARKING);

                                parkYourCarButton.stopAnimation();
                            }

                            @Override
                            public void onError(String message) {
                                Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();

                                parkingLocationSelectPin.setVisibility(View.GONE);

                                setParkYourCarButton(STATE_ADD_PARKING);
                                parkYourCarButton.stopAnimation();
                            }
                        });
                    }

                } else {

                    if(parkButtonState == STATE_SHARE_PARKING){

                        // Show dialog
                        shareParking();


                    } else if (parkButtonState == STATE_REMOVE_PARKING){
                        parkYourCarButton.startAnimation();
                        parkingApi.removeParking(new ParkingApi.ParkingApiResultListener() {
                            @Override
                            public void onSuccess(Object data) {
                                myParkingMarker.remove();
                                myParkingMarker = null;

                                setParkYourCarButton(STATE_ADD_PARKING);
                                parkYourCarButton.stopAnimation();
                            }

                            @Override
                            public void onError(String message) {
                                setParkYourCarButton(STATE_REMOVE_PARKING);
                                parkYourCarButton.stopAnimation();
                            }
                        });
                    } else if (parkButtonState == STATE_STOP_SHARE){

                        parkYourCarButton.startAnimation();

                        parkingApi.stopSharing(shareId, new ParkingApi.ParkingApiResultListener() {
                            @Override
                            public void onSuccess(Object data) {
                                isSharing = false;

                                requestView.stop();
                                requestView.hide();
                                requestView.removeAll();

                                topPanelShareView.show();

                                setParkYourCarButton(STATE_SHARE_PARKING);
                                parkYourCarButton.stopAnimation();

                                hideHandshakePanel();

                            }

                            @Override
                            public void onError(String message) {
                                parkYourCarButton.stopAnimation();
                            }
                        });

                    }


                }

            } else if (view == myLocationButton){
                if(myLocationMarker != null){
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(myLocationMarker.getPosition())
                            .zoom(15)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 300, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            myLocationButton.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }

            } else if (view == acceptedCancel) {
                acceptedCancel.startAnimation();
                parkingApi.cancelRequest(acceptedParkingShare.getRequestId(), new ParkingApi.ParkingApiResultListener() {
                    @Override
                    public void onSuccess(Object data) {
                        acceptedCancel.stopAnimation(new ProgressButton.OnAnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                routeAnimator.removeRoute();
                                topPanelShareView.removeAll();

                                hideAcceptedPanel();
                            }
                        });

                    }

                    @Override
                    public void onError(String message) {
                        acceptedCancel.stopAnimation(new ProgressButton.OnAnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                routeAnimator.removeRoute();
                                topPanelShareView.removeAll();

                                hideAcceptedPanel();
                            }
                        });
                    }
                });
            } else if (view == acceptedNavigate) {

                if(acceptedNavigate.getText().toString().equalsIgnoreCase(getString(R.string.navigate))){
                    String uri = "waze://?ll=" + acceptedParkingShare.getParkingLocation().getLatitude() +", " + acceptedParkingShare.getParkingLocation().getLongitude() +"&navigate=yes";
                    mainActivity.startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(uri)));
                } else {
                    //Confirm Handshake
                }

            } else if (view == handshakeCancelButton){

            } else if (view == handshakeConfirmButton){

            }
        }
    };


    Compass.OnCompassChangeListener onCompassChangeListener = new Compass.OnCompassChangeListener() {
        @Override
        public void onChange(float bearing) {
            if(myLocationMarker != null){
                myLocationMarker.setRotation(bearing);
            }
        }
    } ;


    private void shareParking(){
        final AppCompatDialog dialog = new AppCompatDialog(mainActivity);
        dialog.setContentView(R.layout.dialog_share_time_picker);
        dialog.setTitle(R.string.share_duration);

        final Button dialogButton = (Button) dialog.findViewById(R.id.set_timer_button);
        final TimerView timerView = (TimerView) dialog.findViewById(R.id.share_timer_view);

        parkYourCarButton.startAnimation();

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int duration = timerView.getCurrentSeconds();
                requestView.setTimer(duration);
                parkingApi.shareParking(duration, onShareParkingResult);
                dialog.dismiss();
            }
        });


        dialog.show();
    }




    private ParkingApi.ParkingApiResultListener onShareParkingResult = new ParkingApi.ParkingApiResultListener() {
        @Override
        public void onSuccess(Object data) {

            topPanelShareView.removeAll();
            topPanelShareView.hide();

            shareId = (int) data;
            requestView.show();
            requestView.start();

            // Share your parking
            isSharing = true;

            setParkYourCarButton(STATE_STOP_SHARE);
            parkYourCarButton.stopAnimation();
        }

        @Override
        public void onError(String message) {

        }
    };

    private void setParkYourCarButton(int state){
        switch(state){
            case STATE_ADD_PARKING:
                parkYourCarButton.setText(R.string.park_your_car);
                parkYourCarButton.setBackgroundResource(R.drawable.btn_green);
                break;
            case STATE_PARKING_DONE:
                parkYourCarButton.setText(R.string.done);
                parkYourCarButton.setBackgroundResource(R.drawable.btn_green);
                break;
            case STATE_REMOVE_PARKING:
                parkYourCarButton.setText(R.string.remove_parking);
                parkYourCarButton.setBackgroundResource(R.drawable.btn_red);
                break;
            case STATE_SHARE_PARKING:
                parkYourCarButton.setText(R.string.share_parking);
                parkYourCarButton.setBackgroundResource(R.drawable.btn_blue);
                break;
            case STATE_STOP_SHARE:
                parkYourCarButton.setText(R.string.stop_sharing);
                parkYourCarButton.setBackgroundResource(R.drawable.btn_blue);
                break;
        }

        parkButtonState = state;
    }


    CenterRequestView.OnRequestClickListener onRequestClickListener = new CenterRequestView.OnRequestClickListener() {
        @Override
        public void onRequestClick(final View view, int result, final int position) {
            Log.e("RequestList", "Clicked: " + position + " | " + result);
            final ParkingRequest p = requestView.getRequest(position);

            ((ProgressButton) view).startAnimation();


            if(result == CenterRequestView.REQUEST_ON_REJECT){
                parkingApi.setParkingResponse(shareId, p.getRequestId(), 2, new ParkingApi.ParkingApiResultListener() {
                    @Override
                    public void onSuccess(Object data) {
                        ((ProgressButton) view).stopAnimation(new ProgressButton.OnAnimationEndListener() {
                            @Override
                            public void onAnimationEnd() {
                                requestView.removeRequest(position);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        ((ProgressButton) view).stopAnimation();
                    }
                });
            } else {
                parkingApi.setParkingResponse(shareId, p.getRequestId(), 1, new ParkingApi.ParkingApiResultListener() {
                    @Override
                    public void onSuccess(Object data) {
                        ((ProgressButton) view).stopAnimation();

                        handshakeParkingRequest = p;

                        showHandshakePanel();

                        requestView.removeAll();
                        requestView.hide();
                    }

                    @Override
                    public void onError(String message) {
                        ((ProgressButton) view).stopAnimation();
                    }
                });
            }
        }
    };

    TopPanelShareView.OnShareSendRequestClicked onShareSendRequestClicked = new TopPanelShareView.OnShareSendRequestClicked() {
        @Override
        public void onSendRequest(final View view, int action, final int position) {
            final ParkingShare p = topPanelShareView.getParkingShare(position);

            ((ProgressButton) view).startAnimation();

            if(action == TopPanelShareView.SHARE_ON_SEND){
                Log.e("RequestClicked:",position + "");


                parkingApi.sendRequest(p, new ParkingApi.ParkingApiResultListener() {
                    @Override
                    public void onSuccess(Object data) {
                        int requestId = (int) data;

                        p.setRequestId(requestId);
                        ((ProgressButton) view).stopAnimation();
                    }

                    @Override
                    public void onError(String message) {
                        ((ProgressButton) view).stopAnimation();
                    }
                });
            } else if (action == TopPanelShareView.SHARE_ON_CANCEL){
                Log.e("RequestClicked:",position + "");

                parkingApi.cancelRequest(p.getRequestId(), new ParkingApi.ParkingApiResultListener() {
                    @Override
                    public void onSuccess(Object data) {
                        if(acceptedParkingShare != null){
                            if(p.getRequestId() == acceptedParkingShare.getRequestId()){
                                routeAnimator.removeRoute();
                            }
                        }
                        topPanelShareView.getParkingShare(position).setRequestId(0);
                        ((ProgressButton) view).stopAnimation();
                    }

                    @Override
                    public void onError(String message) {
                        ((ProgressButton) view).stopAnimation();
                    }
                });
            }

        }
    };


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("json");
            Log.e("Broadcast", data);
            if(intent.getAction() != null){
                switch (intent.getAction()){
                    case Constants.INTENT_FILTER_ADD_REQUEST:
                        try {
                            JSONObject jsonObject = new JSONObject(data);

                            JSONObject userJSON = jsonObject.getJSONObject("user");
                            JSONObject requestJSON = jsonObject.getJSONObject("request");

                            int userId = userJSON.getInt("user_id");
                            String firstName = userJSON.getString("first_name");
                            String lastName = userJSON.getString("last_name");
                            String profilePicture = userJSON.getString("profile_picture");

                            // Create user
                            User user = new User(userId, firstName, lastName, profilePicture);


                            int requestId = requestJSON.getInt("request_id");
                            int distance = requestJSON.getInt("distance");

                            ParkingRequest parkingRequest = new ParkingRequest(requestId, user, distance);

                            if(requestView != null){
                                requestView.addRequest(parkingRequest);
                                requestView.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constants.INTENT_FILTER_CANCEL_REQUEST:
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONObject requestJSON = jsonObject.getJSONObject("request");
                            int requestId = requestJSON.getInt("request_id");

                            if(handshakeParkingRequest != null && handshakeParkingRequest.getRequestId() == requestId){
                               hideHandshakePanel();
                            }

                            for(int i = 0; i < requestView.getRequests().size(); i++){
                                ParkingRequest p = requestView.getRequests().get(i);
                                if(p.getRequestId() == requestId){
                                    requestView.removeRequest(i);
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constants.INTENT_FILTER_UPDATE_REQUEST:
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONObject requestJSON = jsonObject.getJSONObject("request");
                            int requestId = requestJSON.getInt("request_id");
                            int status = requestJSON.getInt("status");

                            Log.e("request_id", requestId + "");

                            for(int i = 0; i < topPanelShareView.getParkingShares().size(); i++){
                                final ParkingShare p = topPanelShareView.getParkingShare(i);
                                if(p.getRequestId() == requestId){
                                    if(status == 2){
                                        topPanelShareView.getParkingShare(i).setRequestId(0);
                                        routeAnimator.removeRoute();
                                        hideAcceptedPanel();
                                    } else {
                                        acceptedParkingShare = topPanelShareView.getParkingShare(i);

                                        parkingApi.getRoute(myLocationMarker.getPosition(), p.getMarker().getPosition(), new ParkingApi.ParkingApiResultListener() {
                                            @Override
                                            public void onSuccess(Object jsonObject) {
                                                path.clear();
                                                path.add(myLocationMarker.getPosition());

                                                try {
                                                    JSONArray routes = ((JSONObject)jsonObject).getJSONArray("routes");
                                                    for(int i= 0; i < routes.length(); i++){
                                                        JSONObject route = routes.getJSONObject(i);
                                                        JSONObject polyline = route.getJSONObject("overview_polyline");
                                                        String points  = polyline.getString("points");

                                                        path.addAll(PolyUtil.decode(points));
                                                    }
                                                    path.add(p.getMarker().getPosition());

                                                    // Start animation of polylines
                                                    routeAnimator.setPoints(path);
                                                    routeAnimator.animate();

                                                    showAcceptedPanel();

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                                    }
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                    case Constants.INTENT_FILTER_USER_LOCATION:
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONObject requestJSON = jsonObject.getJSONObject("request");
                            JSONObject locationJSON = jsonObject.getJSONObject("location");
                            int requestId = requestJSON.getInt("request_id");


                            double latitude = locationJSON.getDouble("latitude");
                            double longitude = locationJSON.getDouble("longitude");
                            double bearing = locationJSON.getDouble("bearing");

                            Log.e("Location",jsonObject.toString());
                            Log.e("RequestId", " " + requestId);

                            if(handshakeParkingRequest != null && handshakeParkingRequest.getRequestId() == requestId){


                                LatLng userLocation = new LatLng(latitude, longitude);
                                LatLng myLocation  = myParkingMarker.getPosition();

                                long distance = Math.round(SphericalUtil.computeDistanceBetween(userLocation, myLocation));

                                handshakeDistance.setText(getString(R.string.distance_info, distance));

                                if(handshakeParkingRequest.getMarker() == null){
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                                            .position(new LatLng(latitude, longitude))
                                            .rotation((float)bearing)
                                            .anchor(0.5f, 0.5f)
                                            .flat(true);
                                    handshakeParkingRequest.setMarker(mMap.addMarker(markerOptions));
                                } else {
                                    MapTools.animateMarker(latitude, longitude, (float) bearing, handshakeParkingRequest.getMarker());
                                }

                            }

                        } catch (JSONException e){
                            e.printStackTrace();
                        }

                        break;
                    case Constants.INTENT_FILTER_STOP_SHARING:
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONObject requestJSON = jsonObject.getJSONObject("share");
                            int shareId = requestJSON.getInt("share_id");


                            if(acceptedParkingShare != null && acceptedParkingShare.getShareId() == shareId){
                                hideAcceptedPanel();
                                topPanelShareView.removeAll();
                            }

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                        break;
                    case Constants.INTENT_FILTER_DESTINATION_REACHED:

                        // Change text of handshake
                        acceptedNavigate.setText(R.string.confirm_handshake);

                        break;
                }
            }
        }
    };




    private void showHandshakePanel(){
        handshakePanel.setVisibility(View.VISIBLE);
        handshakeName.setText(handshakeParkingRequest.getUser().getFullName());
        Picasso.with(mainActivity).load(handshakeParkingRequest.getUser().getPicture()).into(handshakeProfile);
        handshakeDistance.setText(R.string.distance_no_info);
    }


    private void hideHandshakePanel(){
        handshakePanel.setVisibility(View.GONE);
        if(handshakeParkingRequest!= null && handshakeParkingRequest.getMarker() != null){
            handshakeParkingRequest.getMarker().remove();
        }
        handshakeParkingRequest = null;
    }

    private void showAcceptedPanel(){
        SharedData.getInstance(mainActivity).setAccepted(true);
        SharedData.getInstance(mainActivity).setDestination(acceptedParkingShare.getParkingLocation());
        Log.e("ParkingLocationReal", acceptedParkingShare.getParkingLocation().toString());
        Log.e("ParkingLocationSet", SharedData.getInstance(mainActivity).getDestination().toString());
        SharedData.getInstance(mainActivity).setAcceptedRequestId(acceptedParkingShare.getRequestId());
        SharedData.getInstance(mainActivity).setReached(false);
        topPanelShareView.hide();
        acceptedPanel.setVisibility(View.VISIBLE);
        Picasso.with(mainActivity).load(acceptedParkingShare.getUser().getPicture()).into(acceptedProfile);
        acceptedName.setText(acceptedParkingShare.getUser().getFullName());


        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                .position(new LatLng(acceptedParkingShare.getParkingLocation().getLatitude(), acceptedParkingShare.getParkingLocation().getLongitude()))
                .rotation((float)acceptedParkingShare.getParkingLocation().getBearing())
                .anchor(0.5f, 0.5f)
                .flat(true);

        acceptedParkingShare.setMarker(mMap.addMarker(markerOptions));
    }

    private void hideAcceptedPanel(){
        SharedData.getInstance(mainActivity).setAccepted(false);
        SharedData.getInstance(mainActivity).setDestination(null);
        SharedData.getInstance(mainActivity).setAcceptedRequestId(0);


        acceptedPanel.setVisibility(View.GONE);
        topPanelShareView.show();

        if(acceptedParkingShare != null && acceptedParkingShare.getMarker() != null){
            acceptedParkingShare.getMarker().remove();
        }

        if(routeAnimator!=null){
            routeAnimator.removeRoute();
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                updateMyLocationMarker(location);
                if(!isSharing){
                    if(!SharedData.getInstance(mainActivity).isAccepted()){
                        ParkingLocation parkingLocation = new ParkingLocation(location.getLatitude(), location.getLongitude(), myLocationMarker.getRotation());

                        parkingApi.getNearByShares(parkingLocation, new ParkingApi.ParkingApiResultListener() {
                            @Override
                            public void onSuccess(Object data) {
                                List<ParkingShare> parkingShares = (ArrayList<ParkingShare>) data;

                                topPanelShareView.setParkingShares(parkingShares);
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }

                }
            }
        }
    }

}

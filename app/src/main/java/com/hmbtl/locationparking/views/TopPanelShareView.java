package com.hmbtl.locationparking.views;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.SharedData;
import com.hmbtl.locationparking.models.ParkingShare;
import com.hmbtl.locationparking.models.Profile;
import com.hmbtl.locationparking.views.adapters.ShareAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anar on 11/14/17.
 */

public class TopPanelShareView extends RelativeLayout{

    private RecyclerView shareListView;
    private GoogleMap googleMap;
    private ShareAdapter shareAdapter;
    private List<ParkingShare> userParkingShares = new ArrayList<>();
    private SnapHelper helper;
    private RecyclerView.LayoutManager layoutManager;
    private int previousSelectedMarker = 0;


    public static final int SHARE_ON_SEND = 1;
    public static final int SHARE_ON_CANCEL = 2;

    private Profile profile;
    public interface OnShareSendRequestClicked{
        void onSendRequest(View view, int action, int position);
    }

    public TopPanelShareView(Context context){
        super(context);
        initView(context);
    }

    public TopPanelShareView(Context context, AttributeSet attr, int defStyle){
        super(context, attr, defStyle);
        initView(context);
    }

    public TopPanelShareView(Context context, AttributeSet attr){
        super(context, attr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.top_panel_share_view, this, true);

        profile = SharedData.getInstance(context).getProfile();

        shareListView = (RecyclerView) findViewById(R.id.shareList);

        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);

        shareAdapter = new ShareAdapter(context, userParkingShares);
        shareListView.setLayoutManager(layoutManager);
        shareListView.setItemAnimator(new DefaultItemAnimator());
        shareListView.addOnScrollListener(onScrollListener);

        helper = new LinearSnapHelper();
        helper.attachToRecyclerView(shareListView);



        shareListView.setAdapter(shareAdapter);

    }

    public void setOnShareSendRequestClicked(OnShareSendRequestClicked onShareSendRequestClicked){
        this.shareAdapter.setOnShareSendRequestClicked(onShareSendRequestClicked);
    }

    public void setMap(GoogleMap googleMap){
        this.googleMap = googleMap;
        populateData(googleMap);

    }

    public void hide(){
        this.setVisibility(View.GONE);
        hideMarkers();
    }

    public void removeAll(){
        for(ParkingShare p: this.userParkingShares){
            p.getMarker().remove();
        }

        this.userParkingShares.clear();
    }

    public void show(){
        this.setVisibility(View.VISIBLE);
        showMarkers();
    }


    private void populateData(GoogleMap googleMap){

        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                .flat(true);



        /*
        userParkingShares.add(new UserParkingShare(
                googleMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                .flat(true)
                .position(new LatLng(40.39251417,49.87067331))),
                1, profile, 130));



        userParkingShares.add(new UserParkingShare(
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                        .flat(true)
                        .position(new LatLng(40.39994788,49.86337201))),
                1, profile, 200));


        userParkingShares.add(new UserParkingShare(
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                        .flat(true)
                        .position(new LatLng(40.39960552,49.86086477))),
                1, profile, 303));

        userParkingShares.add(new UserParkingShare(
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                        .flat(true)
                        .position(new LatLng(40.40238464,49.87816111))),
                1, profile, 450));

        userParkingShares.add(new UserParkingShare(
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                        .flat(true)
                        .position(new LatLng(40.39569807,49.86794329))),
                1, profile, 65));
    */


        shareAdapter.notifyDataSetChanged();
    }


    public ParkingShare getParkingShare(int position){
        return this.userParkingShares.get(position);
    }

    public List<ParkingShare> getParkingShares(){
        return this.userParkingShares;
    }

    public void hideMarkers(){
        for(ParkingShare p: userParkingShares){
            p.getMarker().setVisible(false);
        }
    }

    public void hideMarker(int position){
        userParkingShares.get(position).getMarker().setVisible(false);
    }

    public void showMarkers(){
        for(ParkingShare p: userParkingShares){
            p.getMarker().setVisible(true);
        }

    }

    public void showMarker(int position){
        userParkingShares.get(position).getMarker().setVisible(true);
    }

    public void removeParkingShare(int position){
        if(this.userParkingShares.get(position).getMarker() != null){
            this.userParkingShares.get(position).getMarker().remove();
        }
        this.userParkingShares.remove(position);
        this.shareAdapter.notifyTimer();
        this.shareAdapter.notifyItemRemoved(position);
    }

    public void addParkingShare(final ParkingShare userParkingShare){

        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_red))
                .position(new LatLng(userParkingShare.getParkingLocation().getLatitude(), userParkingShare.getParkingLocation().getLongitude()))
                .rotation((float)userParkingShare.getParkingLocation().getBearing())
                .flat(true);
        userParkingShare.setMarker(this.googleMap.addMarker(markerOptions));

        /*
        userParkingShare.setCountDownTimer(new CountDownTimer(userParkingShare.getDuration() * 1000, 1000) {
            @Override
            public void onTick(long l) {
                userParkingShare.setDuration(userParkingShare.getDuration() - 1);
                shareAdapter.notifyItemChanged(0);
            }

            @Override
            public void onFinish() {

            }
        });
        */

        this.userParkingShares.add(0, userParkingShare);
        this.shareAdapter.notifyTimer();
        this.shareAdapter.notifyItemInserted(0);
        this.shareListView.scrollToPosition(0);
    }

    public void setParkingShares(List<ParkingShare> userParkingShares){

        List<ParkingShare> temporaryParkingShare = new ArrayList<ParkingShare>(this.userParkingShares);

        boolean isRetain;

        // Remove all markers in the map
        for (int i = 0; i < temporaryParkingShare.size(); i++){
            ParkingShare p = temporaryParkingShare.get(i);

            // Set isRetain value to false in each loop
            isRetain = false;

            for(int k = 0; k < userParkingShares.size(); k++){
                ParkingShare pCompare = userParkingShares.get(k);

                // If value is found in this array then retain value
                // and break the loop
                if(pCompare.equals(p)){
                    isRetain = true;
                    break;
                }
            }

            if(!isRetain){
              removeParkingShare(i);
            }

        }


        boolean isAdd;

        for (int i = 0; i < userParkingShares.size(); i++){
            ParkingShare p = userParkingShares.get(i);
            isAdd = true;
            for(int k = 0; k < this.userParkingShares.size(); k++){
                ParkingShare pCompare = this.userParkingShares.get(k);
                if(pCompare.equals(p)){
                    isAdd = false;
                }
            }
            if(isAdd){
                addParkingShare(p);
            }
        }

    }


    public void OnMarkerClick(Marker marker){
        int position = getParkingShareByMarker(marker);

        if(position != -1){
            shareListView.smoothScrollToPosition(position);
        }
    }



    public int getParkingShareByMarker(Marker marker){

        for(ParkingShare p: userParkingShares){
            if(marker.equals(p.getMarker())){
                return userParkingShares.indexOf(p);
            }
        }

        return -1;
    }


    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

        private int prevPos = 0;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                View centerView = helper.findSnapView(layoutManager);
                int position = layoutManager.getPosition(centerView);
                focusMarkerInPosition(position);
            }
        }

    };


    private void focusMarkerInPosition(int position){
        if(previousSelectedMarker != position){
            userParkingShares.get(position).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car_yellow));
            //userParkingShares.get(previousSelectedMarker).getMarker().setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car_red));
            previousSelectedMarker = position;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(userParkingShares.get(position).getMarker().getPosition())
                .zoom(15)
                .build();
        this.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),300,null);
    }

}

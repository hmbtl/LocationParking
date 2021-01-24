package com.hmbtl.locationparking.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.models.ParkingRequest;
import com.hmbtl.locationparking.views.adapters.RequestAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by anar on 11/14/17.
 */

public class CenterRequestView extends RelativeLayout implements View.OnClickListener{


    private ImageButton closeButton;
    private RecyclerView requestListView;
    private List<ParkingRequest> requestList = new ArrayList<>();
    private RequestAdapter requestAdapter;
    private TextView noRequestText;
    private TextView requestCount;
    private TextView requestTimer;
    private RelativeLayout requestContainer;
    private int duration = 0;
    private int remainingDuration = 0;

    private CountDownTimer countDownTimer;
    private OnRequestTimerFinished onRequestTimerFinished;

    public static final int REQUEST_ON_ACCEPT = 1;
    public static final int REQUEST_ON_REJECT = 2;

    public interface OnRequestClickListener {
        void onRequestClick(View view, int result, int position);
    }

    public interface OnRequestTimerFinished {
        void onRequestTimerFinished();
    }

    public CenterRequestView(Context context){
        super(context);
        initView(context);
    }

    public CenterRequestView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public CenterRequestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.center_request_view, this, true);


        requestListView = (RecyclerView) findViewById(R.id.requestsList);
        requestTimer = (TextView) findViewById(R.id.requestTimer);
        requestCount = (TextView) findViewById(R.id.requestCount);
        noRequestText = (TextView) findViewById(R.id.noRequestText);
        closeButton = (ImageButton) findViewById(R.id.closeButton);
        requestContainer = (RelativeLayout) findViewById(R.id.requestContainer);


        closeButton.setOnClickListener(this);
        requestTimer.setOnClickListener(this);

        requestAdapter = new RequestAdapter(context, requestList);
        requestListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        requestListView.setItemAnimator(new DefaultItemAnimator());
        requestListView.setAdapter(requestAdapter);

        this.setClickable(true);
    }


    private void setAlphaAnimation(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha",  1f, .3f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", .3f, 1f);
        fadeIn.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });
        mAnimationSet.start();
    }


    public void setOnRequestClickListener(OnRequestClickListener onRequestClickListener){
        this.requestAdapter.setOnRequestClickListener(onRequestClickListener);
    }

    public void setOnRequestTimerFinished(OnRequestTimerFinished onRequestTimerFinished){
        this.onRequestTimerFinished = onRequestTimerFinished;
    }

    public void setTimer(int duration){
        this.requestTimer.setBackgroundResource(R.drawable.btn_circle_requests);
        this.duration = duration;
        this.countDownTimer = new CountDownTimer(this.duration * 1000, 1000) {

            boolean isTenSecondsLeft = false;

            @Override
            public void onTick(long millisUntilFinished) {
                remainingDuration = (int) TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);

                if(millisUntilFinished <= 11000 && !isTenSecondsLeft) {
                    isTenSecondsLeft = true;
                    requestTimer.setBackgroundResource(R.drawable.btn_circle_requests_red);
                }
                requestTimer.setText(millisecondsToTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                if(onRequestTimerFinished != null){
                    onRequestTimerFinished.onRequestTimerFinished();
                }
            }
        };
    }



    private String millisecondsToTime(long milliseconds){
        String timeToReturn = "00:00";

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((milliseconds - TimeUnit.MINUTES.toMillis(minutes)));

        timeToReturn = String.format(Locale.getDefault(),"%02d",minutes) + ":" + String.format(Locale.getDefault(),"%02d",seconds);

        return timeToReturn;
    }

    public void start(){
        if(this.countDownTimer != null)
            this.countDownTimer.start();
    }


    public void stop(){
        if(this.countDownTimer != null)
            this.countDownTimer.cancel();
    }

    public void removeAll(){
        this.requestList.clear();
        changeRequestVisibility(false);
        setRequestCount();
    }

    public void setRequests(List<ParkingRequest> parkingRequests){
        this.requestList.clear();
        this.requestList.addAll(parkingRequests);
        this.requestAdapter.notifyDataSetChanged();

        if(this.requestList.size() > 0){
            changeRequestVisibility(true);
        } else {
            changeRequestVisibility(false);
        }

        setRequestCount();
    }

    public List<ParkingRequest> getRequests(){
        return this.requestList;
    }

    public int getCount(){
        return this.requestList.size();
    }

    public void addRequest(ParkingRequest parkingRequest){

        this.requestList.add(0,parkingRequest);
        this.requestAdapter.notifyItemInserted(0);
        this.requestListView.scrollToPosition(0);

        if(this.requestList.size() > 0){
            changeRequestVisibility(true);
        }
        setRequestCount();
    }

    public void removeRequest(int position){
        if(this.requestList.size() > position - 1 && position >= 0){
            this.requestList.remove(position);
        }
        this.requestAdapter.notifyItemRemoved(position);

        if(this.requestList.size() == 0){
            changeRequestVisibility(false);
        }

        setRequestCount();
    }

    public ParkingRequest getRequest(int position){
        return this.requestList.get(position);
    }


    private void setRequestCount(){
        this.requestCount.setText(String.valueOf(this.requestList.size()));
    }

    public void hide(){
        this.setVisibility(View.GONE);
    }

    public void show(){
        this.setVisibility(View.VISIBLE);
    }


    public void close(){
        this.requestContainer.setVisibility(View.GONE);
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void open(){
        this.requestContainer.setVisibility(View.VISIBLE);
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    private void changeRequestVisibility(boolean isVisible){
        if(isVisible){
            this.requestListView.setVisibility(View.VISIBLE);
            this.noRequestText.setVisibility(View.GONE);
        } else {
            this.requestListView.setVisibility(View.INVISIBLE);
            this.noRequestText.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View view) {
        if(view == closeButton){
            close();
        } else if(view == requestTimer){
            open();
        }
    }
}

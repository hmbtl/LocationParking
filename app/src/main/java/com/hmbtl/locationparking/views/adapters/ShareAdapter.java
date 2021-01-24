package com.hmbtl.locationparking.views.adapters;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.models.ParkingShare;
import com.hmbtl.locationparking.views.ProgressButton;
import com.hmbtl.locationparking.views.TopPanelShareView;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by anar on 11/20/17.
 */

public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.MyViewHolder> {

    private TopPanelShareView.OnShareSendRequestClicked onShareSendRequestClicked;
    private List<ParkingShare> userParkingShares;
    private Context context;
    private CountDownTimer countDownTimer;

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ProgressButton sendRequestButton;
        TextView nameText, distanceText, timerText;
        CircularImageView profileImage;

        public MyViewHolder(View view){
            super(view);
            this.sendRequestButton = (ProgressButton) view.findViewById(R.id.itemShareSendRequestButton);
            this.nameText = (TextView) view.findViewById(R.id.itemShareName);
            this.distanceText = (TextView) view.findViewById(R.id.itemShareDistance);
            this.profileImage = (CircularImageView) view.findViewById(R.id.itemShareProfile);
            this.timerText = (TextView) view.findViewById(R.id.itemShareTimer);
            this.sendRequestButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view == sendRequestButton){
                if(userParkingShares.get(getAdapterPosition()).getDuration() != 0){
                    if (onShareSendRequestClicked != null) {
                        ParkingShare p = userParkingShares.get(getAdapterPosition());
                        if(p.getRequestId() == 0){
                            onShareSendRequestClicked.onSendRequest(view, TopPanelShareView.SHARE_ON_SEND, getAdapterPosition());
                        } else {
                            onShareSendRequestClicked.onSendRequest(view, TopPanelShareView.SHARE_ON_CANCEL, getAdapterPosition());
                        }

                    }
                }
            }
        }

    }


    public ShareAdapter(Context context, List<ParkingShare> userParkingShares){
        this.context = context;
        this.userParkingShares = userParkingShares;
        notifyTimer();
    }


    public void setOnShareSendRequestClicked(TopPanelShareView.OnShareSendRequestClicked onShareSendRequestClicked){
        this.onShareSendRequestClicked = onShareSendRequestClicked;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.top_panel_share_view_adapter_item, parent, false);


        return new ShareAdapter.MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ParkingShare userParkingShare = userParkingShares.get(position);
        holder.nameText.setText(userParkingShare.getUser().getFullName());
        holder.distanceText.setText(context.getString(R.string.distance_info, userParkingShare.getDistance()));
        holder.timerText.setText(millisecondsToTime(userParkingShare.getDuration() * 1000));

        if(userParkingShare.getDuration() < 11){
            holder.timerText.setBackgroundResource(R.drawable.bg_rounded_share_timer_red);
        } else {
            holder.timerText.setBackgroundResource(R.drawable.bg_rounded_share_timer);
        }

        if(userParkingShare.getRequestId() == 0){
            holder.sendRequestButton.setText(R.string.send_request);
        } else {
            holder.sendRequestButton.setText(R.string.cancel_request);
        }

        Picasso.with(context).load(userParkingShare.getUser().getPicture()).into(holder.profileImage);
    }

    private String millisecondsToTime(long milliseconds){
        String timeToReturn = "00:00";

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds((milliseconds - TimeUnit.MINUTES.toMillis(minutes)));

        timeToReturn = String.format(Locale.getDefault(),"%02d",minutes) + ":" + String.format(Locale.getDefault(),"%02d",seconds);

        return timeToReturn;
    }


    public void notifyTimer(){
        int maxDuration = findMaxDuration();
        addTimer(maxDuration);
    }

    private void addTimer(int maxDuration){

        if(countDownTimer != null){
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(maxDuration * 1000, 1000) {
            @Override
            public void onTick(long l) {
                for(int i = 0; i <userParkingShares.size(); i++){
                    if(userParkingShares.get(i).getDuration() > 0){
                        userParkingShares.get(i).setDuration(userParkingShares.get(i).getDuration() - 1);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
            }
        };

        countDownTimer.start();
    }

    private int findMaxDuration(){
        int maxDuration = 0;

        for(ParkingShare p: userParkingShares){
            maxDuration = Math.max(maxDuration, p.getDuration());
        }

        return maxDuration;
    }

    @Override
    public int getItemCount() {
        return userParkingShares.size();
    }
}

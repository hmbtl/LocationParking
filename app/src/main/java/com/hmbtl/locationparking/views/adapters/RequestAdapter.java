package com.hmbtl.locationparking.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.models.ParkingRequest;
import com.hmbtl.locationparking.views.CenterRequestView;
import com.hmbtl.locationparking.views.ProgressButton;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by anar on 11/15/17.
 */


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.MyViewHolder>{
    private List<ParkingRequest> parkingRequests;
    private Context context;
    private CenterRequestView.OnRequestClickListener onRequestClickListener;

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ProgressButton acceptButton, rejectButton;
        public TextView nameText, distanceText;
        public CircularImageView profileImage;

        private MyViewHolder(View view){
            super(view);
            this.acceptButton = (ProgressButton) view.findViewById(R.id.itemRequestAcceptButton);
            this.rejectButton = (ProgressButton) view.findViewById(R.id.itemRequestRejectButton);
            this.nameText = (TextView) view.findViewById(R.id.itemRequestName);
            this.distanceText = (TextView) view.findViewById(R.id.itemRequestDistance);
            this.profileImage = (CircularImageView) view.findViewById(R.id.itemRequestProfile);
            this.acceptButton.setOnClickListener(this);
            this.rejectButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view == acceptButton){
                if(onRequestClickListener != null){
                    onRequestClickListener.onRequestClick(view, CenterRequestView.REQUEST_ON_ACCEPT, getAdapterPosition());
                }
            } else if (view == rejectButton){
                if(onRequestClickListener != null){
                    onRequestClickListener.onRequestClick(view, CenterRequestView.REQUEST_ON_REJECT, getAdapterPosition());
                }
            }
        }
    }

    public RequestAdapter(Context context, List<ParkingRequest> parkingRequests){
        this.parkingRequests = parkingRequests;
        this.context = context;
    }


    public void setOnRequestClickListener(CenterRequestView.OnRequestClickListener onRequestClickListener){
        this.onRequestClickListener = onRequestClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.center_requests_view_adapter_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ParkingRequest parkingRequest = parkingRequests.get(position);
        holder.nameText.setText(parkingRequest.getUser().getFullName());
        holder.distanceText.setText(String.valueOf(parkingRequest.getDistance()) + " meters away");
        Picasso.with(context).load(parkingRequest.getUser().getPicture()).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return parkingRequests.size();
    }


}
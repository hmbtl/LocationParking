package com.hmbtl.locationparking.api;

import android.app.Activity;

import com.google.android.gms.maps.model.LatLng;
import com.hmbtl.locationparking.Constants;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.models.ParkingLocation;
import com.hmbtl.locationparking.models.ParkingShare;
import com.hmbtl.locationparking.models.User;
import com.hmbtl.locationparking.utilities.MapTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anar on 11/16/17.
 */

public class ParkingApi {
    private Activity context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");

    public interface ParkingApiResultListener {
        void onSuccess(Object data);
        void onError(String message);
    }

    public ParkingApi(Activity context){
        this.context = context;
    }


    public void addParking(ParkingLocation parkingLocation,  final ParkingApiResultListener callback){
        try {
            JSONObject json = new JSONObject();
            json.put("latitude",parkingLocation.getLatitude());
            json.put("longitude",parkingLocation.getLongitude());
            json.put("bearing",parkingLocation.getLongitude());

            BasicApi.getInstance(context)
                    .sendRequest("parking", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(null);
                                }
                            });
                        }

                        @Override
                        public void onFailure() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onData(Object dataJSON) {
                        }
                    });

        } catch (JSONException e) {
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }

    public void removeParking(final ParkingApiResultListener callback){

        BasicApi.getInstance(context)
                .sendRequest("parking", Constants.HTTP_DELETE, new ApiListener() {
                    @Override
                    public void onSuccess() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(null);
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onError(int code) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onData(Object dataJSON) {
                    }
                });


    }

    public void getParking(final ParkingApiResultListener callback){

        BasicApi.getInstance(context)
                .sendRequest("parking", Constants.HTTP_GET, new ApiListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onError(int code) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onData(final Object data) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    JSONObject dataJSON = (JSONObject) data;

                                    double latitude = dataJSON.getDouble("latitude");
                                    double longitude = dataJSON.getDouble("longitude");
                                    double bearing = dataJSON.getDouble("bearing");

                                    callback.onSuccess(new ParkingLocation(latitude, longitude, bearing));

                                } catch (JSONException e) {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            }
                        });
                    }
                });


    }



    public void shareParking(int duration, final ParkingApiResultListener callback){
        try{
            final JSONObject json = new JSONObject();
            json.put("duration", duration);

            BasicApi.getInstance(context)
                    .sendRequest("parking/share", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onData(final Object object) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = (JSONObject) object;
                                        int shareId = jsonObject.getInt("share_id");

                                        callback.onSuccess(shareId);

                                    } catch (JSONException e) {
                                        callback.onError(context.getString(R.string.api_failure_message));
                                    }
                                }
                            });
                        }
                    });
        } catch (JSONException e){
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }

    public void stopSharing(int shareId, final ParkingApiResultListener callback){
        BasicApi.getInstance(context)
                .sendRequest("parking/share/" + shareId, Constants.HTTP_DELETE, new ApiListener() {
                    @Override
                    public void onSuccess() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(null);
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onError(int code) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onData(final Object object) {

                    }
                });
    }


    public void sendRequest(final ParkingShare parkingShare, final ParkingApiResultListener callback){
        try{
            final JSONObject json = new JSONObject();
            json.put("share_id", parkingShare.getShareId());
            json.put("distance", parkingShare.getDistance());

            BasicApi.getInstance(context)
                    .sendRequest("parking/request", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onData(final Object object) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = (JSONObject) object;
                                        int requestId = jsonObject.getInt("request_id");
                                        callback.onSuccess(requestId);

                                    } catch (JSONException e) {
                                        callback.onError(context.getString(R.string.api_failure_message));
                                    }
                                }
                            });
                        }
                    });
        } catch (JSONException e){
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }


    public void cancelRequest(int requestId, final ParkingApiResultListener callback){
        BasicApi.getInstance(context)
                .sendRequest("parking/request/" + requestId, Constants.HTTP_DELETE, new ApiListener() {
                    @Override
                    public void onSuccess() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(null);
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onError(int code) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onData(final Object object) {

                    }
                });
    }

    public void setParkingResponse(int shareId, int requestId, int response, final ParkingApiResultListener callback){
        try {
            JSONObject json = new JSONObject();
            json.put("share_id",shareId);
            json.put("request_id",requestId);
            json.put("response",response);

            BasicApi.getInstance(context)
                    .sendRequest("parking/response", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(null);
                                }
                            });

                        }

                        @Override
                        public void onFailure() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onData(final Object dataJSON) {

                        }
                    });

        } catch (JSONException e) {
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }

    public void getNearByShares(ParkingLocation parkingLocation, final ParkingApiResultListener callback){
        try {
            JSONObject json = new JSONObject();
            json.put("latitude",parkingLocation.getLatitude());
            json.put("longitude",parkingLocation.getLongitude());
            json.put("bearing",parkingLocation.getLongitude());

            BasicApi.getInstance(context)
                    .sendRequest("parking/nearby", Constants.HTTP_GET, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure() {
                        }

                        @Override
                        public void onError(int code) {

                        }

                        @Override
                        public void onData(final Object dataJSON) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONArray jsonArray = (JSONArray) dataJSON;

                                        List<ParkingShare> parkingShares = new ArrayList<>();

                                        for(int i = 0; i < jsonArray.length(); i++){

                                            JSONObject userJSON = jsonArray.getJSONObject(i).getJSONObject("user");
                                            JSONObject parkingJSON = jsonArray.getJSONObject(i).getJSONObject("parking");

                                            int userId = userJSON.getInt("user_id");
                                            String firstName = userJSON.getString("first_name");
                                            String lastName = userJSON.getString("last_name");
                                            String profilePicture = userJSON.getString("profile_picture");

                                            // Create user
                                            User user = new User(userId, firstName, lastName, profilePicture);

                                            int shareId = parkingJSON.getInt("share_id");
                                            int parkingId = parkingJSON.getInt("parking_id");
                                            int duration = parkingJSON.getInt("duration");
                                            int distance = parkingJSON.getInt("distance");
                                            Date date = dateFormat.parse(parkingJSON.getString("share_date"));
                                            double latitude = parkingJSON.getDouble("latitude");
                                            double longitude = parkingJSON.getDouble("longitude");
                                            double bearing = parkingJSON.getDouble("bearing");

                                            // Create parking location
                                            ParkingLocation  location = new ParkingLocation(latitude, longitude, bearing);
                                            // Create parking share
                                            ParkingShare parkingShare = new ParkingShare(user, shareId, duration, location, distance, date);

                                            // Add to list
                                            parkingShares.add(parkingShare);
                                        }

                                        callback.onSuccess(parkingShares);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });

        } catch (JSONException e) {
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }


    public void getRoute(LatLng origin, LatLng destination, final ParkingApiResultListener callback){
        String requestURL = MapTools.createDirectionsUrl(origin, destination);
        BasicApi.getInstance(context)
                .sendExternal(requestURL, Constants.HTTP_GET, null, new ApiListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onError(int code) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(context.getString(R.string.api_failure_message));
                            }
                        });
                    }

                    @Override
                    public void onData(final Object jsonObject) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(jsonObject);
                            }
                        });
                    }
                });
    }


    public void updateSharing(int shareId, final ParkingApiResultListener callback){
        try {
            JSONObject json = new JSONObject();
            json.put("share_id", shareId);
            BasicApi.getInstance(context)
                    .sendRequest("parking/share/" + shareId, Constants.HTTP_PUT, new ApiListener() {
                        @Override
                        public void onSuccess() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(null);
                                }
                            });
                        }

                        @Override
                        public void onFailure() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onData(final Object object) {

                        }
                    });
        } catch (JSONException e){
            e.printStackTrace();
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }

    public void sendLocation(int requestId, ParkingLocation location, final ParkingApiResultListener callback){
        try {
            JSONObject json = new JSONObject();
            json.put("request_id", requestId);
            json.put("latitude",location.getLatitude());
            json.put("longitude",location.getLongitude());
            json.put("bearing",location.getLongitude());
            BasicApi.getInstance(context)
                    .sendRequest("location", Constants.HTTP_POST, json, new ApiListener() {
                        @Override
                        public void onSuccess() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(null);
                                }
                            });
                        }

                        @Override
                        public void onFailure() {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError(context.getString(R.string.api_failure_message));
                                }
                            });
                        }

                        @Override
                        public void onData(final Object object) {

                        }
                    });
        } catch (JSONException e){
            e.printStackTrace();
            callback.onError(context.getString(R.string.api_failure_message));
        }
    }

}

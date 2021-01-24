package com.hmbtl.locationparking.services;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hmbtl.locationparking.Constants;
import com.hmbtl.locationparking.R;
import com.hmbtl.locationparking.activities.BaseActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by anar on 11/13/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private LocalBroadcastManager broadcastManager;
    private static final String TAG = "FCM Service";
    private static final String INTENT_CANCEL_REQUEST = "INTENT_CANCEL_REQUEST";



    @Override
    public void onCreate() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            String json = data.get("json");
            String action = data.get("action");

            if(action != null){



                Intent intent;
                switch (action) {
                        case "add_request":
                            intent = new Intent(Constants.INTENT_FILTER_ADD_REQUEST);
                            intent.putExtra("json", json);
                            broadcastManager.sendBroadcast(intent);
                            break;
                        case "update_request":
                            intent = new Intent(Constants.INTENT_FILTER_UPDATE_REQUEST);
                            intent.putExtra("json", json);
                            broadcastManager.sendBroadcast(intent);

                            break;
                        case "cancel_request":

                            intent = new Intent(Constants.INTENT_FILTER_CANCEL_REQUEST);
                            intent.putExtra("json", json);
                            broadcastManager.sendBroadcast(intent);

                            break;
                        case "user_location":

                            intent = new Intent(Constants.INTENT_FILTER_USER_LOCATION);
                            intent.putExtra("json", json);
                            broadcastManager.sendBroadcast(intent);

                            break;
                        case "stop_sharing":
                            if(isAppIsInBackground()) {
                                sendNotification("The user cancelled parking spot");
                            }
                            intent = new Intent(Constants.INTENT_FILTER_STOP_SHARING);
                            intent.putExtra("json", json);
                            broadcastManager.sendBroadcast(intent);
                            break;
                    }
            }


        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "unbind");
        return super.onUnbind(intent);
    }

    private boolean isAppIsInBackground() {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }


    private void sendNotification(String message) {
        Intent notificationIntent = new Intent(this, BaseActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );



        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_share)
                        .setContentTitle("Location Update")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}

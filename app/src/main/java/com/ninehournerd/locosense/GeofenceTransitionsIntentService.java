package com.ninehournerd.locosense;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by katelyn on 4/27/15.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String LOG_TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                /*String errorMessage = GeofenceErrorMessages.getErrorString(this,
                        geofencingEvent.getErrorCode());
                Log.e(LOG_TAG, errorMessage);*/
                return;
            }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            //TODO: add settings to decide if user wants notifications
            sendNotification(geofenceTransitionDetails);
            Log.i(LOG_TAG, geofenceTransitionDetails);

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                enableWiFi();
            }
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                disableWifi();
            }
        } else {
            // Log the error.
                /*Log.e(LOG_TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));*/
        }
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        Log.e("DEBUG", "sendNotification");
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
            /*builder.setSmallIcon(R.drawable.ic_launcher)
                    // In a real app, you may want to use a library like Volley
                    // to decode the Bitmap.
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_launcher))
                    .setColor(Color.RED)
                    .setContentTitle(notificationDetails)
                    .setContentText(getString(R.string.geofence_transition_notification_text))
                    .setContentIntent(notificationPendingIntent);*/

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context             The app context.
     * @param geofenceTransition  The ID of the geofence transition.
     * @param triggeringGeofences The geofence(s) triggered.
     * @return The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {
        Log.e("DEBUG", "getGeofenceTransitionDetails");
        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        Log.e("DEBUG", "getTransitionString");
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Geofence transition entered";
            //return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                //return getString(R.string.geofence_transition_exited);
                return "Geofence transition exited";
            default:
                //return getString(R.string.unknown_geofence_transition);
                return "unknown transition entered";
        }
    }

    public void enableWiFi() {
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Log.i(LOG_TAG, "Wifi enabled.");
        }
    }

    public void disableWifi() {
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            Log.i(LOG_TAG, "Wifi disabled.");
        }
    }
}

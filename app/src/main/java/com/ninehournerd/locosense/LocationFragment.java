package com.ninehournerd.locosense;

/**
 * Created by katelyn on 4/26/15.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private ArrayAdapter<Comment> mLocationAdapter;
    ArrayList<String> locations = new ArrayList<String>();
    List<Comment> comments = new ArrayList<Comment>();
    private CommentsDataSource datasource;

    private PendingIntent mGeofencePendingIntent;

    public LocationFragment() {
    }

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Log.d("***********", "ON CREATEE");

        /** Reference to the button of the layout main.xml */
        Button btn = (Button) rootView.findViewById(R.id.btnAdd);

        datasource = new CommentsDataSource(getActivity());
        datasource.open();

        comments = datasource.getAllComments();


        mLocationAdapter = new ArrayAdapter<Comment>(
                // the current context which is this fragment's parent activity
                getActivity(),
                //ID of list item layout
                R.layout.list_item_location,
                //ID of the text view to populate
                R.id.list_item_location_textview,
                //forecast Data
                comments);

        final EditText edit = (EditText) rootView.findViewById(R.id.txtItem);

        /** Defining a click event listener for the button "Add" */
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EditText edit = (EditText) findViewById(R.id.txtItem);
                //locations.add(edit.getText().toString());

                Comment comment = null;
                comment = datasource.createComment(edit.getText().toString());
                mLocationAdapter.add(comment);

                createGeoFence(edit.getText().toString());


                edit.setText("");


                mLocationAdapter.notifyDataSetChanged();
            }
        };

        /** Setting the event listener for the add button */
        btn.setOnClickListener(listener);

        ListView listView = (ListView) rootView.findViewById(R.id.listView_locations);
        listView.setAdapter(mLocationAdapter);

        return rootView;
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }



    private void createGeoFence(String loc) {

        Geocoder geocoder = new Geocoder(getActivity());


        try {
            List<Address> addressList = geocoder.getFromLocationName(loc, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();


                Geofence geofence = new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(loc)

                        .setCircularRegion(
                                address.getLatitude(),
                                address.getLongitude(),
                                100
                        )
                        .setExpirationDuration(10000000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();

                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        getGeofencingRequest(),
                        getGeofencePendingIntent()
                ).setResultCallback(getActivity());

                Context context = getActivity();
                CharSequence text = "Geofence for " +address.getLatitude() + " " + address.getLongitude() + " created!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                /*sb.append(address.getLatitude()).append("\n");
                sb.append(address.getLongitude()).append("\n");
                String result = sb.toString();*/
            }
        }
        catch (Exception e) {

        }

    }

    public void onResult() {}

    @Override
    public void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        datasource.close();
        super.onPause();
    }

    public void enableWiFi() {
        WifiManager wifiManager = (WifiManager) getActivity()
                .getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public void disableWifi() {
        WifiManager wifiManager = (WifiManager) getActivity()
                .getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    public class GeofenceTransitionsIntentService extends IntentService {

        public GeofenceTransitionsIntentService() {
            super("GeofenceTransitionsIntentService");
        }

        protected void onHandleIntent(Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            /*if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(this,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }*/

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
                sendNotification(geofenceTransitionDetails);
                //Log.i(TAG, geofenceTransitionDetails);

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    enableWiFi();
                }
                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    disableWifi();
                }
            } else {
                // Log the error.
                /*Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
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
                    return "unkknown transition entered";
            }
        }
    }


}

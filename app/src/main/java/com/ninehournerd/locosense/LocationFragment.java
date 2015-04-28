package com.ninehournerd.locosense;

/**
 * Created by katelyn on 4/26/15.
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
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
    private static final String LOG_TAG = LocationFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        buildGoogleApiClient();
        mGoogleApiClient.connect();



    }

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

                Geofence geofence = createGeoFence(edit.getText().toString());
                if (geofence != null) {
                    mGeofencePendingIntent = getGeofencePendingIntent();
                    LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                            getGeofencingRequest(geofence),
                            mGeofencePendingIntent);
                    showGeofenceCreatedToast(geofence);

                }

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

    private Geofence createGeoFence(String location) {
        Geocoder geocoder = new Geocoder(getActivity());

        try {
            //TODO: getFromLocationName is blocking and should be moved to separate thread
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);

                Geofence geofence = new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(location)
                        .setCircularRegion(
                                address.getLatitude(),
                                address.getLongitude(),
                                100
                        )
                        .setExpirationDuration(1000*60*10) //TODO: change to NEVER_EXPIRE
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();
                return geofence;
            }
        }
        catch (IOException e) {
            //TODO: IOException	if the network is unavailable or any other I/O problem occurs
        }
        return null;
    }

    private void showGeofenceCreatedToast(Geofence geofence) {
        Context context = getActivity();
        CharSequence text = "Geofence for " + geofence.getRequestId() + " created!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        //TODO: check if you can continuously add single geofence
        ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();
        mGeofenceList.add(geofence);

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER & GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeofenceList);
        return builder.build();
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

}

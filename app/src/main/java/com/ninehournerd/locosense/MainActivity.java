package com.ninehournerd.locosense;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LocationFragment())
                    .commit();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void enableWiFi() {
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    public void disableWifi() {
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        private ArrayAdapter<String> mLocationAdapter;
        ArrayList<String> locations = new ArrayList<String>();

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            /*String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
                    "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                    "Linux", "OS/2" };

            mLocationAdapter = new ArrayAdapter<String>(
                    // the current context which is this fragment's parent activity
                    getActivity(),
                    //ID of list item layout
                    R.layout.list_item_location,
                    //ID of the text view to populate
                    R.id.list_item_location_textview,
                    //forecast Data
                    values);

            ListView listView = (ListView) rootView.findViewById(R.id.listView_locations);
            listView.setAdapter(mLocationAdapter);

            return rootView;

            */

            /** Reference to the button of the layout main.xml */
            Button btn = (Button) findViewById(R.id.btnAdd);



            mLocationAdapter = new ArrayAdapter<String>(
                    // the current context which is this fragment's parent activity
                    getActivity(),
                    //ID of list item layout
                    R.layout.list_item_location,
                    //ID of the text view to populate
                    R.id.list_item_location_textview,
                    //forecast Data
                    locations);


            /** Defining a click event listener for the button "Add" */
            OnClickListener listener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText edit = (EditText) findViewById(R.id.txtItem);
                    locations.add(edit.getText().toString());
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
    }
}

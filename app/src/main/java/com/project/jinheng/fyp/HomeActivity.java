package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.Lot;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JinHeng on 11/3/2014.
 */
public class HomeActivity extends BaseActivity {

    private final String TAG = "HomeActivity";
    private static List<Lot> parkingLots = new ArrayList<>();
    private static List<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        new MarkerAsyncTask().execute(); //todo the api url for the server here

        Fragment fragment = HomeFragment.newInstance(R.layout.fragment_home);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();

    }

    public static class HomeFragment extends Fragment implements LocationListener {

        private static final String TAG = "HomeFragment";
        private GoogleMap map;
        private Location location;
        private LocationManager locationManager;
        private String locationProvider;
        private AlertDialog errorDialog;

        public static HomeFragment newInstance(int layout) {
            HomeFragment classInstance = new HomeFragment();
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            classInstance.setArguments(args);

            return classInstance;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(getArguments().getInt("layout"), container, false);

            if (map == null) {
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                if (map == null) {
                    Log.d(TAG, "Map not loaded");
                }
            }

            initializeMap();
            initializeLocationManager();

            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.button_locate_user);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("called click");
                    if (!locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER))) {
                        showErrorDialog();
                    } else if (location != null) {
                        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                        map.moveCamera(center);

                        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                        map.animateCamera(zoom);

                    }
                }
            });

            return view;
        }

        //----------------------------------------
        //	initializing the map
        //----------------------------------------
        private void initializeMap() {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(2.923218, 101.642023), 14.0f));
            initializeMarkers();
            Log.d(TAG, "map initialized");
        }

        //----------------------------------------
        //	initializing location manager
        //----------------------------------------
        private void initializeLocationManager() {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            locationProvider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(locationProvider);

            if (location != null) {
                onLocationChanged(location);
            }
        }

        //----------------------------------------
        //	Overriding location listener
        //----------------------------------------
        @Override
        public void onLocationChanged(Location location) {
            Log.i("called", "onLocationChanged");
        }

        @Override
        public void onProviderDisabled(String arg0) {
            showErrorDialog();
        }

        private void showErrorDialog() {
            Log.i("called", "showErrorDialog");
            //initialize error dialog
            if (errorDialog == null) {
                errorDialog = new AlertDialog.Builder(getActivity()).create();
                errorDialog.setTitle("Location Service disabled");
                errorDialog.setMessage("Please enable location service to get more accurate information.");
                errorDialog.setInverseBackgroundForced(true);
                errorDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
            }

            if (!errorDialog.isShowing()) {
                errorDialog.show();
            } else {
                errorDialog.dismiss();
            }
        }

        //----------------------------------------
        //	Initializing map markers
        //----------------------------------------
        public void initializeMarkers() {

//            for (Lot lot : parkingLots) {
//                Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(lot.getLatitute()), Double.valueOf(lot.getLongitute()))));
//                markers.add(marker);
//            }

        }

        @Override
        public void onProviderEnabled(String arg0) {
            Log.i("called", "onProviderEnabled");
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            Log.i("called", "onStatusChanged");
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.i("called", "Map --> onResume");
            locationManager.requestLocationUpdates(this.locationProvider, 400, 1, this);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.i("called", "Map --> onPause");
            locationManager.removeUpdates(this);
        }

    }

//    public class MarkerAsyncTask extends AsyncTask<String, Void, Boolean> {

//        @Override
//        protected Boolean doInBackground(String... params) {
//
//            try {
//                HttpClient client = new DefaultHttpClient();
//                HttpPost post = new HttpPost(APIUtils.APIURL);
//                JSONDTO requestJSONObj = new JSONDTO();
//                requestJSONObj.setServiceName(APIUtils.GET_MARKER);
//                String requestJSONString = APIUtils.toJson(requestJSONObj);
//
//                //Prepare json to send with httppost
//                post.setEntity(new StringEntity(requestJSONString, "UTF-8"));
//                //Setup header types needed to transfer JSON
//                post.setHeader("Content-Type", "application/json");
//                post.setHeader("Accept-Encoding", "application/json");
//                post.setHeader("Accept-Language", "en-US");
//
//                HttpResponse response = client.execute(post);
//                Log.d(TAG, "getting data from server......");
//
//                StatusLine statusLine = response.getStatusLine();
//                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
//                    HttpEntity entity = response.getEntity();
//                    String data = EntityUtils.toString(entity);
//
//                    Log.d(TAG + "MarkerAsync:\nJSON sent from server: {}", data);
//
//                }
//
//            } catch (IOException e) {
//                //TODO auto generated stub
//                e.printStackTrace();
//            }
//            return null;
//
//        }
//    }
}

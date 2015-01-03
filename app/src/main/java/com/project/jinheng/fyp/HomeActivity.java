package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.Lot;
import com.project.jinheng.fyp.classes.MyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by JinHeng on 11/3/2014.
 */
public class HomeActivity extends BaseActivity {

    private final String TAG = "HomeActivity";

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

        public static List<Marker> globalMarkerList;

        private static final String TAG = "HomeFragment";
        private GoogleMap map;
        private Location location;
        private LocationManager locationManager;
        private String locationProvider;
        private AlertDialog errorDialog;
        private ProgressDialog progressDialog;
        private List<Lot> parkingLots = new ArrayList<Lot>();
        private boolean asyncRunning = false;
        private boolean markerLoaded = false;

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

            FloatingActionButton locateMeButton = (FloatingActionButton) view.findViewById(R.id.button_locate_user);
            locateMeButton.setOnClickListener(new View.OnClickListener() {
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
        public void onLocationChanged(Location newLocation) {
            Log.i("called", "onLocationChanged");
            if (!asyncRunning && !markerLoaded) {
                location = newLocation;
                initializeMarkers(newLocation.getLatitude(), newLocation.getLongitude());
            }
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
        public void initializeMarkers(Double latitude, Double longitude) {

            //declare Asynctask's task
            AsyncTask<JSONDTO, Void, JSONDTO> markerAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {

                @Override
                protected JSONDTO doInBackground(JSONDTO... params) {
                    JSONDTO jsonFromServer;
                    try {
                        jsonFromServer = APIUtils.processAPICalls(params[0]);
                        return jsonFromServer;

                    } catch (MyException e) {
                        Log.e(TAG, e.getMessage());
                        JSONDTO returnDTO = new JSONDTO();
                        JSONError error = new JSONError(e.getError(), e.getMessage());
                        returnDTO.setError(error);
                        return returnDTO;
                    } catch (Exception e) {
                        JSONDTO returnDTO = new JSONDTO();
                        e.printStackTrace();
                        Log.e(TAG, "Exception occurred when calling API");
                        JSONError error = new JSONError(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                        returnDTO.setError(error);
                        return returnDTO;
                    }
                }

                @Override
                protected void onPostExecute(JSONDTO jsondto) {
                    try {
                        if (jsondto.getError() != null) {
                            throw new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                        } else {

                            if (jsondto.getParkingLots() != null) {
                                parkingLots = jsondto.getParkingLots();
                            }

                            globalMarkerList.clear();
                            for (Lot lot : parkingLots) {
                                if (lot.getLongitude() != null && lot.getLatitude() != null && lot.getLotName() != null && lot.getAddress() != null) {
                                    Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(lot.getLatitude(), lot.getLongitude())).title(lot.getLotName()).snippet(lot.getAddress()));
                                    globalMarkerList.add(marker);
                                } else {
                                    Log.e(TAG, "Data error occured");
                                }
                            }
                            markerLoaded = true;

                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                            asyncRunning = false;
                        }

                    } catch (MyException e) {
                        AlertDialog error = new AlertDialog.Builder(getActivity()).create();
                        error.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        error.setMessage(e.getMessage());
                        error.setInverseBackgroundForced(true);
                        error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                              TODO don't know what to do here
                            }
                        });
                        error.show();

                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                    }
                }
            };

            //finish declaring async task definition
            //build json object to process login
            JSONDTO dataToProcess = new JSONDTO();
            dataToProcess.setServiceName(APIUtils.GET_PARKING_LOTS);
            dataToProcess.setLatitude(latitude);
            dataToProcess.setLongitude(longitude);
            //TODO hardcoded groupType, change to use zoom level
            dataToProcess.setGroupType("city");

            //reverse locate the current city of the user
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                if (addresses.size() > 0 && addresses.get(0).getLocality() != null) {
                    dataToProcess.setCriteria(addresses.get(0).getLocality());
                    Toast.makeText(getActivity(), "You're now in " + addresses.get(0).getLocality(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "could not locate current city");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                asyncRunning = true;
                markerAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
            } else {
                markerAPICall.execute(dataToProcess);
            }
        }

        @Override
        public void onProviderEnabled(String arg0) {
            Log.i("called", "onProviderEnabled");
            if (errorDialog != null) {
                errorDialog.dismiss();
            }
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

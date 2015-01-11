package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.melnykov.fab.FloatingActionButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.GoogleNearbyPlaces;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.Lot;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.SearchClasses;
import com.project.jinheng.fyp.classes.Slot;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by JinHeng
 */
public class HomeActivity extends BaseActivity {

    private final String TAG = "HomeActivity";

    private long timeWhenBackPressed = 0;
    private static Marker justToPassAMarker;
    private static TextView justToPassAText;
    private static FloatingActionButton justToPassAButton;
    private static Location justToPassALocation;
    public static boolean mapIsTouched;
    private static ProgressDialog progressDialog;
    private static GoogleMap justToPassAMap;

    private List<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BaseActivity.needSearch = true;
        super.onCreate(savedInstanceState);
        Fragment fragment = HomeFragment.newInstance(R.layout.fragment_home);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
        Log.d(TAG, "HomeFragment inflated");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            AsyncTask<String, Void, GoogleNearbyPlaces> placesAPICall = new AsyncTask<String, Void, GoogleNearbyPlaces>() {
                @Override
                protected void onPreExecute() {
                    progressDialog = MyProgressDialog.initiate(HomeActivity.this);
                    progressDialog.show();
                }

                @Override
                protected GoogleNearbyPlaces doInBackground(String... params) {
                    try {
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            String lat = String.valueOf(justToPassALocation.getLatitude());
                            String lng = String.valueOf(justToPassALocation.getLongitude());
                            String query = params[0].trim();
                            query = query.replace(" ", "+");
                            HttpGet httpGet = new HttpGet(APIUtils.PLACES_API_BASE + APIUtils.PLACES_API_TYPE_NEARBYSEARCH + "?location=" + lat + "," + lng + "&rankby=distance&name=" + query + APIUtils.PLACES_API_KEY);
                            HttpClient client = new DefaultHttpClient();

                            Log.d(TAG, "getting data from places API......");
                            HttpResponse response;

                            response = client.execute(httpGet);
                            HttpEntity entity = response.getEntity();
                            String data = EntityUtils.toString(entity);
                            Log.d(TAG + "\nJSON sent from Google: ", data);
                            GoogleNearbyPlaces returnedJSON = APIUtils.create().fromJson(data, GoogleNearbyPlaces.class);
                            //result is returned
                            if (returnedJSON.getStatus().equals("OK")) {
                                return returnedJSON;
                            } else if (returnedJSON.getStatus().equals("ZERO_RESULTS")) {
                                //no result is returned
                                return null;
                            } else {
                                //other codes
                                throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());

                            }
                        }
                    } catch (MyException e) {
                        Log.e(TAG, "Unexpected error");
                        e.printStackTrace();
                    } catch (HttpHostConnectException e) {
                        Log.e(TAG, "Server timed out");
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(GoogleNearbyPlaces places) {
                    if (places != null) {
                        List<SearchClasses.Result> results = places.getResults();

                        //clear map first
                        for (Marker m : markers) {
                            m.remove();
                        }
                        markers = new ArrayList<>();

                        for (SearchClasses.Result r : results) {
                            Double lat = r.getGeometry().getLocation().getLat();
                            Double lng = r.getGeometry().getLocation().getLng();
                            Marker marker = justToPassAMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(r.getName())
                                            .snippet(r.getVicinity())
                            );

                            markers.add(marker);
                        }
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                        LatLng position = markers.get(0).getPosition();
                        justToPassAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(position.latitude, position.longitude), 16.5f));
                        markers.get(0).showInfoWindow();
                    } else {
                        Toast.makeText(HomeActivity.this, "No matching result found from the search", Toast.LENGTH_SHORT).show();
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            };

            if (justToPassALocation != null) {
                if (query.length() >= 3) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        placesAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
                    } else {
                        placesAPICall.execute(query);
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Please search for more than 3 words.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(HomeActivity.this, "Unable to obtain GPS Location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showErrorDialog(MyException e) {
        AlertDialog error = new AlertDialog.Builder(HomeActivity.this).create();
        error.requestWindowFeature(Window.FEATURE_NO_TITLE);
        error.setMessage(e.getMessage());
        error.setInverseBackgroundForced(true);
        error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        error.show();
    }

    @Override
    public void onBackPressed() {

        if (BaseActivity.drawerOpen) {
            super.onBackPressed();
        } else if (markers.size() > 0) {
            for (Marker m : markers) {
                m.remove();
            }
        } else {
            Toast exitConfirmationToast = Toast.makeText(HomeActivity.this, "Press back again to exit the application.", Toast.LENGTH_SHORT);

            final int interval = 2000;
            // if info window is opened, close it instead of trying to exit
            if (justToPassAMarker != null) {
                justToPassAMarker.hideInfoWindow();
                justToPassAButton.setVisibility(View.INVISIBLE);
                justToPassAText.setVisibility(View.INVISIBLE);
                justToPassAText = null;
                justToPassAButton = null;
                justToPassAMarker = null;
            } else {
                // exit the application by pressing back 2 twice
                if (timeWhenBackPressed + interval > System.currentTimeMillis()) {
                    super.onBackPressed();
                    finish();
                } else {
                    Log.d(TAG, "Back button pressed once");
                    exitConfirmationToast.show();
                }
                timeWhenBackPressed = System.currentTimeMillis();
            }

        }
    }

    public static class HomeFragment extends Fragment implements LocationListener {

        private static final String TAG = "HomeFragment";
        private Location location;
        private LocationManager locationManager;
        private String locationProvider;
        private AlertDialog errorDialog;
        private List<Lot> parkingLots = new ArrayList<>();
        private boolean asyncRunning = false;
        private boolean markerLoaded = false;
        private Marker currentMarker; //to close that marker when other thing clicked
        private Toast connectingToastFragment;
        private final Handler connectingToastHandler = new Handler();
        private GoogleMap map;
        private List<Marker> parkingMarkers = new ArrayList<>();
        private final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                connectingToastFragment.show();
                Log.d(TAG, "Connecting to server...");
                connectingToastHandler.postDelayed(this, 2500);
            }
        };
        private View view;
        private TextView parkHereText;
        private FloatingActionButton parkHereButton;

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
            this.view = view;

            if (map == null) {
                map = ((MyMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                justToPassAMap = map;
                if (map == null) {
                    Log.e(TAG, "Map not loaded");
                }
            }

            initializeMap();
            initializeLocationManager();

            FloatingActionButton locateMeButton = (FloatingActionButton) view.findViewById(R.id.button_locate_user);
            locateMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER))) {
                        locationErrorDialog();
                    } else if (location != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f));

                        if (parkHereButton.getVisibility() == View.VISIBLE) {
                            parkHereButton.setVisibility(View.INVISIBLE);
                            parkHereText.setVisibility(View.INVISIBLE);
                        }
                        if (currentMarker != null) {
                            currentMarker.hideInfoWindow();
                        } else {
                            if (!asyncRunning) {
                                clearParkingMarkers();
                                markerLoaded = false;
                                Log.d(TAG, "reloaded markers");
                                initializeMarkers(location.getLatitude(), location.getLongitude());
                            }
                        }
                    }
                }

            });

            FloatingActionButton findVehicleButton = (FloatingActionButton) view.findViewById(R.id.button_locate_vehicle);
            findVehicleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AsyncTask<JSONDTO, Void, JSONDTO> checkUserParkedAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {
                        @Override
                        protected void onPreExecute() {
                            progressDialog = MyProgressDialog.initiate(getActivity());
                            progressDialog.show();
                        }

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
                            asyncRunning = false;
                            if (jsondto != null) {
                                if (jsondto.isAlreadyParkedThere()) {
                                    Intent intent = new Intent(getActivity(), LocateVehicleActivity.class);
                                    intent.putExtra("vehicleDetails", APIUtils.toJson(jsondto));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getActivity(), "You have not parked your vehicle yet!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                MyException e = new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                                showErrorDialog(e);
                            }
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                        }

                    };

                    JSONDTO dataToProcess = new JSONDTO();
                    SharedPreferences settings = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                    String userEmail = settings.getString("email", null);
                    if (userEmail != null) {
                        dataToProcess.setServiceName(APIUtils.CHECK_VEHICLE);
                        dataToProcess.setEmail(userEmail);
                    } else {
                        locationErrorDialog();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        asyncRunning = true;
                        checkUserParkedAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                    } else {
                        asyncRunning = true;
                        checkUserParkedAPICall.execute(dataToProcess);
                    }

                }
            });

            View rootLayout = view.findViewById(R.id.root_layout);
            rootLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Toast.makeText(getActivity(), "fts", Toast.LENGTH_SHORT);
                    if (currentMarker.isInfoWindowShown()) {
                        currentMarker.hideInfoWindow();
                        parkHereButton.setVisibility(View.INVISIBLE);
                        parkHereText.setVisibility(View.INVISIBLE);
                    }
                    return false;
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
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(2.923218, 101.642023), 15.0f));
            map.setTrafficEnabled(true);

            parkHereText = getParkHereText(view);
            parkHereButton = getParkHereButton(view);

            //remove the ask user parking
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    parkHereButton.setVisibility(View.INVISIBLE);
                    parkHereText.setVisibility(View.INVISIBLE);
                }
            });
            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    if (HomeActivity.mapIsTouched) {
                        parkHereButton.setVisibility(View.INVISIBLE);
                        parkHereText.setVisibility(View.INVISIBLE);
                    }
                }
            });
            //animation of the marker
            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                                             @Override
                                             public boolean onMarkerClick(final Marker marker) {
                                                 final Handler handler = new Handler();

                                                 final long startTime = SystemClock.uptimeMillis();
                                                 final long duration = 1200;

                                                 Projection proj = map.getProjection();
                                                 final LatLng markerLatLng = marker.getPosition();
                                                 Point startPoint = proj.toScreenLocation(markerLatLng);
                                                 startPoint.offset(0, -50);
                                                 final LatLng startLatLng = proj.fromScreenLocation(startPoint);

                                                 final Interpolator interpolator = new BounceInterpolator();

                                                 handler.post(new Runnable() {
                                                     @Override
                                                     public void run() {
                                                         long elapsed = SystemClock.uptimeMillis() - startTime;
                                                         float t = interpolator.getInterpolation((float) elapsed / duration);
                                                         double lng = t * markerLatLng.longitude + (1 - t) * startLatLng.longitude;
                                                         double lat = t * markerLatLng.latitude + (1 - t) * startLatLng.latitude;
                                                         marker.setPosition(new LatLng(lat, lng));

                                                         if (t < 1.0) {
                                                             // Post again 16ms later.
                                                             handler.postDelayed(this, 16);
                                                         }
                                                     }
                                                 });
                                                 marker.showInfoWindow();
                                                 currentMarker = marker;
                                                 justToPassAMarker = marker;
                                                 justToPassAButton = parkHereButton;
                                                 justToPassAText = parkHereText;

                                                 if (location != null) {

                                                     //ask user wanna park or not if current marker is near user and when it is open
                                                     Lot lot = parkingLots.get(Integer.valueOf(marker.getSnippet()));
                                                     boolean opened;
                                                     if (lot.getOperationHour().equals("24 Hour")) {
                                                         opened = true;
                                                     } else {
                                                         opened = isParkingOpened(lot.getOpenHour(), lot.getCloseHour());
                                                     }

                                                     //calculate distance and enable the button
                                                     if (APIUtils.calculateDistance(location.getLatitude(), location.getLongitude(), marker.getPosition().latitude, marker.getPosition().longitude, "M") <= 0.2 && opened) {
                                                         parkHereText.setVisibility(View.VISIBLE);
                                                         parkHereButton.setVisibility(View.VISIBLE);
                                                         parkHereButton.setOnClickListener(new View.OnClickListener() {
                                                             @Override
                                                             public void onClick(View v) {
                                                                 final AsyncTask<JSONDTO, Void, JSONDTO> reparkVehicle = new AsyncTask<JSONDTO, Void, JSONDTO>() {
                                                                     @Override
                                                                     protected void onPreExecute() {
                                                                         progressDialog = MyProgressDialog.initiate(getActivity());
                                                                         progressDialog.show();
                                                                     }

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
                                                                             asyncRunning = false;
                                                                             if (jsondto.getError() != null) {
                                                                                 throw new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                                                                             }
                                                                             SharedPreferences.Editor editor = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0).edit();
                                                                             editor.putBoolean("parked", true).apply();
                                                                             Toast.makeText(getActivity(), "OK! Your vehicle is now parked in " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                                                                             if (progressDialog != null) {
                                                                                 progressDialog.dismiss();
                                                                             }

                                                                         } catch (MyException e) {
                                                                             showErrorDialog(e);
                                                                             if (progressDialog != null) {
                                                                                 progressDialog.dismiss();
                                                                             }
                                                                         }
                                                                     }
                                                                 };

                                                                 final AsyncTask<JSONDTO, Void, JSONDTO> parkVehicleAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {
                                                                     @Override
                                                                     protected void onPreExecute() {
                                                                         progressDialog = MyProgressDialog.initiate(getActivity());
                                                                         progressDialog.show();
                                                                     }

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
                                                                             asyncRunning = false;
                                                                             if (jsondto.getError() != null) {
                                                                                 throw new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                                                                             } else if (jsondto.isAlreadyParkedThere()) {
                                                                                 parkHereButton.setVisibility(View.INVISIBLE);
                                                                                 parkHereText.setVisibility(View.INVISIBLE);
                                                                                 Toast.makeText(getActivity(), "Your vehicle is already parked here!", Toast.LENGTH_SHORT).show();
                                                                             } else {
                                                                                 parkHereButton.setVisibility(View.INVISIBLE);
                                                                                 parkHereText.setVisibility(View.INVISIBLE);
                                                                                 SharedPreferences.Editor editor = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0).edit();
                                                                                 editor.putBoolean("parked", true).apply();
                                                                                 Toast.makeText(getActivity(), "OK! Your vehicle is now parked in " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                                                                             }
                                                                             if (progressDialog != null) {
                                                                                 progressDialog.dismiss();
                                                                             }

                                                                         } catch (MyException e) {
                                                                             AlertDialog reParkDialog = new AlertDialog.Builder(getActivity()).create();
                                                                             reParkDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                                             reParkDialog.setMessage(e.getMessage());
                                                                             reParkDialog.setInverseBackgroundForced(true);
                                                                             reParkDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                                                                                 @Override
                                                                                 public void onClick(DialogInterface dialog, int which) {
                                                                                     JSONDTO dataToProcess = new JSONDTO();
                                                                                     dataToProcess.setServiceName(APIUtils.PARK_VEHICLE);
                                                                                     SharedPreferences settings = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                                                                                     Lot parkedLot = parkingLots.get(Integer.valueOf(marker.getSnippet()));
                                                                                     Date parkedTime = new Date();
                                                                                     String userEmail = settings.getString("email", null);
                                                                                     if (userEmail == null) {
                                                                                         Log.e(TAG, "Abnormal behavior, user email does not exist");
                                                                                     }
                                                                                     //to be saved in db
                                                                                     Slot parkedSlot = new Slot();
                                                                                     parkedSlot.setStatus("U");
                                                                                     parkedSlot.setParkTime(parkedTime);
                                                                                     parkedSlot.setLot(parkedLot);

                                                                                     dataToProcess.setSlot(parkedSlot);
                                                                                     dataToProcess.setEmail(userEmail);
                                                                                     dataToProcess.setForceRepark(true);
                                                                                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                                                         asyncRunning = true;
                                                                                         reparkVehicle.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                                                                                     } else {
                                                                                         asyncRunning = true;
                                                                                         reparkVehicle.execute(dataToProcess);
                                                                                     }
                                                                                     parkHereButton.setVisibility(View.INVISIBLE);
                                                                                     parkHereText.setVisibility(View.INVISIBLE);
                                                                                 }
                                                                             });
                                                                             reParkDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                                                                                 @Override
                                                                                 public void onClick(DialogInterface dialog, int which) {
                                                                                     parkHereButton.setVisibility(View.INVISIBLE);
                                                                                     parkHereText.setVisibility(View.INVISIBLE);
                                                                                 }
                                                                             });
                                                                             reParkDialog.show();

                                                                             if (progressDialog != null) {
                                                                                 progressDialog.dismiss();
                                                                             }

                                                                         }
                                                                     }

                                                                 };

                                                                 //finish declaring async task definition
                                                                 //build json object to process login
                                                                 //do it in background as it lags when changing connection
                                                                 JSONDTO dataToProcess = new JSONDTO();
                                                                 dataToProcess.setServiceName(APIUtils.PARK_VEHICLE);
                                                                 SharedPreferences settings = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                                                                 Lot parkedLot = parkingLots.get(Integer.valueOf(marker.getSnippet()));
                                                                 Date parkedTime = new Date();
                                                                 String userEmail = settings.getString("email", null);
                                                                 if (userEmail == null) {
                                                                     Log.e(TAG, "Abnormal behavior, user email does not exist");
                                                                 }
                                                                 Slot parkedSlot = new Slot();
                                                                 parkedSlot.setStatus("U");
                                                                 parkedSlot.setParkTime(parkedTime);
                                                                 parkedSlot.setLot(parkedLot);
                                                                 parkedSlot.setParkTime(parkedTime);

                                                                 dataToProcess.setSlot(parkedSlot);
                                                                 dataToProcess.setEmail(userEmail);
                                                                 dataToProcess.setForceRepark(false);

                                                                 //reverse locate the current city of the user,
                                                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                                     asyncRunning = true;
                                                                     parkVehicleAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                                                                 } else {
                                                                     asyncRunning = true;
                                                                     parkVehicleAPICall.execute(dataToProcess);
                                                                 }
                                                             }

                                                         });
                                                     } else {
                                                         parkHereButton.setVisibility(View.INVISIBLE);
                                                         parkHereText.setVisibility(View.INVISIBLE);
                                                     }
                                                 }
                                                 return false;
                                             }
                                         }

            );

            map.clear();
            Log.d(TAG, "map initialized");
        }

        //----------------------------------------
        //	initializing location manager
        //----------------------------------------

        private void initializeLocationManager() {

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            locationProvider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(locationProvider);
        }

        //----------------------------------------
        //	Overriding location listener
        //----------------------------------------
        @Override
        public void onLocationChanged(Location newLocation) {
            Log.i("called", "onLocationChanged");
            if (!asyncRunning) {

                location = newLocation;
                justToPassALocation = location;
                if (markerLoaded && currentMarker == null) {
                    clearParkingMarkers();
                    markerLoaded = false;
                    initializeMarkers(newLocation.getLatitude(), newLocation.getLongitude());
                }
                if (!markerLoaded) {
                    clearParkingMarkers();
                    initializeMarkers(newLocation.getLatitude(), newLocation.getLongitude());
                }

            }
        }

        @Override
        public void onProviderDisabled(String arg0) {
            locationErrorDialog();
            if (!markerLoaded) {
                if (location != null) {
                    clearParkingMarkers();
                    initializeMarkers(location.getLatitude(), location.getLongitude());
                } else {
                    clearParkingMarkers();
                    initializeMarkers(2.923218, 2.923218);
                }
            }
        }

        //----------------------------------------
        //	Display indefinite toast
        //----------------------------------------
        private void displayOrDisableToast(int mode) {
            //declare toast, handler and runnable object for onProgressUpdate
            connectingToastFragment = Toast.makeText(getActivity(), "Connecting to server...", Toast.LENGTH_SHORT);

            if (mode == 0) {
                connectingToastHandler.postDelayed(runnable, 2500);
            } else if (mode == 1) {
                connectingToastHandler.removeCallbacks(runnable);
            }
        }

        //----------------------------------------
        //	Initializing map markers
        //----------------------------------------
        public void initializeMarkers(final Double latitude, final Double longitude) {

            //declare Asynctask's task
            final AsyncTask<JSONDTO, Integer, JSONDTO> markerAPICall = new AsyncTask<JSONDTO, Integer, JSONDTO>() {

                @Override
                protected JSONDTO doInBackground(JSONDTO... params) {
                    JSONDTO jsonFromServer;
                    try {
                        publishProgress(1);
                        jsonFromServer = APIUtils.processAPICalls(params[0]);
                        return jsonFromServer;

                    } catch (MyException e) {
                        Log.e(TAG, e.getMessage());
                        JSONDTO returnDTO = new JSONDTO();
                        JSONError error = new JSONError(e.getError(), e.getMessage());
                        returnDTO.setError(error);
                        return returnDTO;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception occurred when calling API");
                        JSONError error = new JSONError(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                        JSONDTO returnDTO = new JSONDTO();
                        returnDTO.setError(error);
                        return returnDTO;
                    }
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    displayOrDisableToast(0);
                }

                @Override
                protected void onPostExecute(JSONDTO jsondto) {
                    try {
                        asyncRunning = false;
                        displayOrDisableToast(1);
                        if (jsondto != null) {
                            if (jsondto.getError() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //TODO hardcorded to solve bug
                                        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo data = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                                        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                        if (!wifi.isConnected() && !data.isConnected()) {
                                            Toast.makeText(getActivity(), "System is currently unavailable. Functionality will be limited.", Toast.LENGTH_LONG).show();
                                        } else {
                                            if (!markerLoaded) {
                                                clearParkingMarkers();
                                                initializeMarkers(latitude, longitude);
                                            }
                                        }
                                    }

                                });

                            } else {

                                if (jsondto.getParkingLots() != null) {
                                    parkingLots = jsondto.getParkingLots();
                                }

                                //initialize custom info window and clicked event
                                map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                                             //use self defined info window frame
                                                             @Override
                                                             public View getInfoWindow(Marker marker) {
                                                                 View v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);

                                                                 TextView title = (TextView) v.findViewById(R.id.info_window_title);
                                                                 TextView address = (TextView) v.findViewById(R.id.info_window_address);
                                                                 TextView details = (TextView) v.findViewById(R.id.info_window_details);
                                                                 ImageView availability = (ImageView) v.findViewById(R.id.info_window_availability);
                                                                 TextView price = (TextView) v.findViewById(R.id.info_window_price);
                                                                 TextView instruction = (TextView) v.findViewById(R.id.info_window_instruction);

                                                                 if (marker.getSnippet().length() > 2) {
                                                                     title.setText(marker.getTitle());
                                                                     address.setText(marker.getSnippet());
                                                                     details.setVisibility(View.INVISIBLE);
                                                                     availability.setVisibility(View.INVISIBLE);
                                                                     price.setVisibility(View.INVISIBLE);
                                                                     instruction.setVisibility(View.INVISIBLE);

                                                                 } else {
                                                                     details.setVisibility(View.VISIBLE);
                                                                     availability.setVisibility(View.VISIBLE);
                                                                     price.setVisibility(View.VISIBLE);
                                                                     instruction.setVisibility(View.VISIBLE);

                                                                     if (parkingLots != null) {
                                                                         Integer parkingLotIndex = Integer.valueOf(marker.getSnippet());
                                                                         Lot lot = parkingLots.get(parkingLotIndex);
                                                                         title.setText(lot.getLotName());
                                                                         address.setText(lot.getAddress() + " " + lot.getCity() + ", " + lot.getState());

                                                                         //set price
                                                                         DecimalFormat formatter = new DecimalFormat("RM ###.00");
                                                                         if (lot.getPrice() != null) {
                                                                             String type = lot.getPrice().getPriceType();
                                                                             if (type.equals("FLAT")) {
                                                                                 Double value = Double.valueOf(lot.getPrice().getFlatRate()) / 100;
                                                                                 String output = formatter.format(value);
                                                                                 price.setText(output + " per hour");
                                                                             } else if (type.equals("DYNAMIC") || type.equals("RATE")) {
                                                                                 Double value = Double.valueOf(lot.getPrice().getFirstHour()) / 100;
                                                                                 String output = formatter.format(value);
                                                                                 price.setText("First Hour: " + output);
                                                                             }
                                                                         } else {
                                                                             price.setText("");
                                                                         }

                                                                         //display lot type accordingly
                                                                         if (lot.getLotType().equals("B")) {
                                                                             details.setText("Basement Parking");
                                                                         } else if (lot.getLotType().equals("O")) {
                                                                             details.setText("Open Parking");
                                                                         } else if (lot.getLotType().equals("P")) {
                                                                             details.setText("Paid Parking");
                                                                         } else {
                                                                             details.setText("");
                                                                         }

                                                                         //set image according to opening hour
                                                                         boolean open = false; //to indicate parking open or close
                                                                         boolean unknown = false; //for unknown operating time

                                                                         if (lot.getOpenHour() != null || lot.getCloseHour() != null) {
                                                                             open = isParkingOpened(lot.getOpenHour(), lot.getCloseHour());
                                                                         } else {
                                                                             unknown = true;
                                                                         }

                                                                         if (unknown) { //if unknown set to grey
                                                                             availability.setBackgroundResource(R.drawable.ic_light_blue);
                                                                         } else if (open) {
                                                                             availability.setBackgroundResource(R.drawable.ic_light_green);
                                                                         } else if (!open) {
                                                                             availability.setBackgroundResource(R.drawable.ic_light_red);
                                                                         }
                                                                     }
                                                                 }
                                                                 return v;
                                                             }

                                                             @Override
                                                             public View getInfoContents(Marker marker) {
                                                                 return null;
                                                             }
                                                         }

                                );

                                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                                                     @Override
                                                                     public void onInfoWindowClick(Marker marker) {
                                                                         Intent intent = new Intent(getActivity(), LotDetailActivity.class);

                                                                         //prepare lot object to sent to activity
                                                                         if (parkingLots != null) {
                                                                             if (marker.getSnippet().length() <= 2) {
                                                                                 Integer parkingLotIndex = Integer.valueOf(marker.getSnippet());
                                                                                 Lot lot = parkingLots.get(parkingLotIndex);
                                                                                 String json = APIUtils.toJson(lot);
                                                                                 intent.putExtra("details", json);
                                                                                 startActivity(intent);
                                                                                 getActivity().overridePendingTransition(R.anim.bottom_to_top_in, R.anim.fade_out);
                                                                             }
                                                                         }
                                                                     }
                                                                 }

                                );

                                Integer parkingLotListIndex = 0;
                                for (Lot lot : parkingLots) {
                                    if (lot.getLongitude() != null && lot.getLatitude() != null && lot.getLotName() != null && lot.getAddress() != null) {
                                        if (lot.getAvailability().equals("U")) {
                                            Marker marker = map.addMarker(new MarkerOptions()
                                                    .position(new LatLng(lot.getLatitude(), lot.getLongitude()))
                                                    .title(lot.getLotName())
                                                    .snippet(parkingLotListIndex.toString())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_grey)));
                                            parkingLotListIndex++;
                                            parkingMarkers.add(marker);
                                        } else if (lot.getAvailability().equals("H")) {
                                            Marker marker = map.addMarker(new MarkerOptions()
                                                    .position(new LatLng(lot.getLatitude(), lot.getLongitude())).title(lot.getLotName())
                                                    .snippet(parkingLotListIndex.toString())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green)));
                                            parkingLotListIndex++;
                                            parkingMarkers.add(marker);
                                        } else if (lot.getAvailability().equals("M")) {
                                            Marker marker = map.addMarker(new MarkerOptions()
                                                    .position(new LatLng(lot.getLatitude(), lot.getLongitude())).title(lot.getLotName())
                                                    .snippet(parkingLotListIndex.toString())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange)));
                                            parkingLotListIndex++;
                                            parkingMarkers.add(marker);
                                        } else if (lot.getAvailability().equals("L")) {
                                            Marker marker = map.addMarker(new MarkerOptions()
                                                    .position(new LatLng(lot.getLatitude(), lot.getLongitude())).title(lot.getLotName())
                                                    .snippet(parkingLotListIndex.toString())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red)));
                                            parkingLotListIndex++;
                                            parkingMarkers.add(marker);
                                        } else {
                                            throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                                        }
                                    } else {
                                        Log.e(TAG, "Data error occured");
                                    }
                                }
                                markerLoaded = true;
                            }
                        }
                    } catch (MyException e) {
                        showErrorDialog(e);
                    }
                }
            };

            //finish declaring async task definition
            //build json object to process login
            //do it in background as it lags when changing connection
            JSONDTO dataToProcess = new JSONDTO();
            dataToProcess.setServiceName(APIUtils.GET_PARKING_LOTS);
            dataToProcess.setLatitude(latitude);
            dataToProcess.setLongitude(longitude);
            //TODO hardcoded groupType, change to use zoom level
            dataToProcess.setGroupType("city");

            //reverse locate the current city of the user,
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses.size() > 0 && addresses.get(0).getLocality() != null) {
                    dataToProcess.setCriteria(addresses.get(0).getLocality());
                } else {
                    Log.e(TAG, "could not locate current city");
                    //TODO hardcode to solve bug
                    dataToProcess.setCriteria("Cyberjaya");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
            locationManager.requestLocationUpdates(this.locationProvider, 400, 50, this);
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            Log.i("called", "onStatusChanged");
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.i("called", "Map --> onResume");
            locationManager.requestLocationUpdates(this.locationProvider, 400, 50, this);
            if (asyncRunning) {
                displayOrDisableToast(0);
            }
        }

        @Override
        public void onStop() {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            super.onStop();
        }

        @Override
        public void onPause() {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            super.onPause();
            Log.i("called", "Map --> onPause");
            locationManager.removeUpdates(this);
            parkHereText.setVisibility(View.INVISIBLE);
            parkHereButton.setVisibility(View.INVISIBLE);
            displayOrDisableToast(1);
        }

        private void clearParkingMarkers() {
            if (parkingMarkers != null) {
                for (Marker m : parkingMarkers) {
                    m.remove();
                }
            }
        }

        private boolean isParkingOpened(Integer openHour, Integer closeHour) {
            int hourNow;
            Calendar timeNow = Calendar.getInstance();
            hourNow = timeNow.get(Calendar.HOUR_OF_DAY);

            //logic to check whether parking is open now
            //convert to 12 hour format

            //in the same day
            if (closeHour > openHour) {
                if (hourNow >= openHour && hourNow < closeHour) {
                    return true;
                }
            } else {
                //between different day
                if (hourNow < 24 && hourNow > 1 && hourNow >= openHour) {
                    return true;
                } else if (hourNow >= 0 && hourNow <= closeHour) {
                    return true;
                }
            }
            return false;
        }

        private void showErrorDialog(MyException e) {
            AlertDialog error = new AlertDialog.Builder(getActivity()).create();
            error.requestWindowFeature(Window.FEATURE_NO_TITLE);
            error.setMessage(e.getMessage());
            error.setInverseBackgroundForced(true);
            error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            error.show();
        }

        private void locationErrorDialog() {
            Log.i("called", "locationErrorDialog");
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

        private TextView getParkHereText(View view) {
            return (TextView) view.findViewById(R.id.want_to_park_text);
        }

        private FloatingActionButton getParkHereButton(View view) {
            return (FloatingActionButton) view.findViewById(R.id.want_to_park_button);
        }

    }
}

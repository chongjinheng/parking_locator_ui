package com.project.jinheng.fyp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.Lot;

import java.text.DecimalFormat;

import com.project.jinheng.fyp.MonitorLocationService.LocalBinder;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class LotDetailActivity extends BaseActivity {

    private static Lot lot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String json = getIntent().getStringExtra("details");
        lot = (Lot) APIUtils.fromJSON(json, Lot.class);

        Fragment fragment = LotDetailFragment.newInstance(R.layout.fragment_lotdetails);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public static class LotDetailFragment extends Fragment {

        private static final String TAG = "LotDetailFragment";

        private GoogleMap map;
        private AlertDialog errorDialog;
        private boolean serviceBounded;
        private MonitorLocationService monitorLocationService;

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceBounded = true;
                LocalBinder localBinder = (LocalBinder) service;
                monitorLocationService = localBinder.getServiceInstance();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBounded = false;
                monitorLocationService = null;
            }
        };

        public static LotDetailFragment newInstance(int layout) {
            LotDetailFragment classInstance = new LotDetailFragment();
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            classInstance.setArguments(args);
            return classInstance;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(getArguments().getInt("layout"), container, false);

            if (map == null) {
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.lot_detail_map)).getMap();
                if (map == null) {
                    Log.d(TAG, "Map not loaded");
                }
            }
            initializeInterface(view);
            initializeMap();

            final RelativeLayout navigateView = (RelativeLayout) view.findViewById(R.id.navigate_to_lot_box);
            final ImageView navigateButton = (ImageView) view.findViewById(R.id.navigate_to_lot_button);

            View.OnTouchListener myTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        navigateView.setBackgroundResource(R.color.primary_light);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {

                        navigateView.setBackgroundResource(R.drawable.bg_button_translucent);

                        if (lot != null) {
                            Uri uri = Uri.parse("geo:0,0?q=" + lot.getLatitude() + "," + lot.getLongitude());
                            Intent serviceIntent = new Intent(getActivity(), MonitorLocationService.class);

                            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            if (serviceBounded) {
                                getActivity().unbindService(connection);
                                serviceBounded = false;
                            }
                            SharedPreferences settings = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("serviceTarget", APIUtils.toJson(lot));
                            editor.apply();
                            getActivity().bindService(serviceIntent, connection, BIND_AUTO_CREATE);
                        } else {
                            Log.e(TAG, "Lot not found when trying to intent to navigation");
                            showErrorDialog();
                        }
                    }
                    return true;
                }
            };

            navigateButton.setOnTouchListener(myTouchListener);
            navigateView.setOnTouchListener(myTouchListener);

            return view;
        }

        private void initializeInterface(View view) {
            TextView name = (TextView) view.findViewById(R.id.detail_lot_name);
            TextView type = (TextView) view.findViewById(R.id.detail_lot_type);
            TextView address = (TextView) view.findViewById(R.id.detail_lot_address);
            TextView operationHour = (TextView) view.findViewById(R.id.detail_lot_operation_hour);
            TextView capacity = (TextView) view.findViewById(R.id.detail_lot_capacity);
            TextView availability = (TextView) view.findViewById(R.id.detail_lot_availability);
            TextView nearbyAttraction = (TextView) view.findViewById(R.id.detail_lot_nearby);
            TextView price = (TextView) view.findViewById(R.id.detail_lot_price);

            if (lot.getLotName() != null || lot.getLotType() != null || lot.getAddress() != null || lot.getCity() != null || lot.getState() != null || lot.getOperationHour() != null
                    || lot.getCapacity() != null || lot.getAvailability() != null || lot.getNearbyAttraction() != null || lot.getPrice() != null) {

                name.setText(lot.getLotName());
                switch (lot.getLotType()) {
                    case "O":
                        type.setText("Open Parking");
                        break;
                    case "P":
                        type.setText("Indoor Parking");
                        break;
                    case "B":
                        type.setText("Basement Parking");
                        break;
                    default:
                        Log.e(TAG, "Lot type not recognized");
                        showErrorDialog();
                        break;
                }

                address.setText(lot.getAddress() + " " + lot.getCity() + ", " + lot.getState());
                operationHour.setText(lot.getOperationHour());
                capacity.setText(lot.getCapacity().toString());

                switch (lot.getAvailability()) {
                    case "H":
                        availability.setText("High");
                        availability.setTextColor(getResources().getColor(R.color.accent_dark));
                        break;
                    case "M":
                        availability.setText("Medium");
                        availability.setTextColor(getResources().getColor(R.color.orange));
                        break;
                    case "L":
                        availability.setText("Low");
                        availability.setTextColor(getResources().getColor(R.color.red_main));
                        break;
                    case "U":
                        availability.setText("Unknown");
                        availability.setTextColor(getResources().getColor(R.color.hint));
                        break;
                    default:
                        Log.e(TAG, "Availability of lot not found");
                        showErrorDialog();
                }

                nearbyAttraction.setText(lot.getNearbyAttraction());

                DecimalFormat formatter = new DecimalFormat("RM ###.00");
                //TODO price type
                String output = null;
                switch (lot.getPrice().getPriceType()) {
                    case "FLAT":
                        output = "Flat Rate " + formatter.format(Double.valueOf(lot.getPrice().getFlatRate()) / 100);
                        break;
                    case "DYNAMIC":
                        output = "First Hour: " + formatter.format(Double.valueOf(lot.getPrice().getFirstHour()) / 100) + "       Subsequent Hour: " + formatter.format(Double.valueOf(lot.getPrice().getSubsHour()));
                        break;
                    case "RATE":
                        output = "First Hour: " + formatter.format(Double.valueOf(lot.getPrice().getFirstHour()) / 100) + "       Subsequent Hour: " + formatter.format(Double.valueOf(lot.getPrice().getSubsHour()));
                        break;
                    default:
                        Log.e(TAG, "Price type not defined");
                        showErrorDialog();
                }

                price.setText(output);

            } else {
                Log.e(TAG, "Null field in lot");
                showErrorDialog();
            }
        }

        private void showErrorDialog() {
            Log.i("called", "showErrorDialog");
            //initialize error dialog
            if (errorDialog == null) {
                errorDialog = new AlertDialog.Builder(getActivity()).create();
                errorDialog.setTitle("Error");
                errorDialog.setMessage("Something went wrong, please try again later");
                errorDialog.setInverseBackgroundForced(true);
                errorDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });

            }

            if (!errorDialog.isShowing()) {
                errorDialog.show();
            } else {
                errorDialog.dismiss();
            }
        }

        private void initializeMap() {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setTrafficEnabled(true);

            String json = getActivity().getIntent().getStringExtra("details");
            if (json != null) {
                Lot lot = (Lot) APIUtils.fromJSON(json, Lot.class);
                MarkerOptions option = new MarkerOptions().position(new LatLng(lot.getLatitude(), lot.getLongitude()));
                switch (lot.getAvailability()) {
                    case "H":
                        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green));
                        break;
                    case "M":
                        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange));
                        break;
                    case "L":
                        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red));
                        break;
                    case "U":
                        option.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_grey));
                        break;
                }
                map.addMarker(option);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lot.getLatitude(), lot.getLongitude()), 14.0f));
            } else {
                Log.e(TAG, "unable to get json from Home");
                showErrorDialog();
            }
        }

        @Override
        public void onResume() {
            Log.d(TAG, "On resume called");
            super.onResume();
            if (serviceBounded) {
                getActivity().unbindService(connection);
                serviceBounded = false;
            }
        }

        @Override
        public void onStop() {
            Log.d(TAG, "On stop called");
            super.onStop();
            if (serviceBounded) {
                getActivity().unbindService(connection);
                serviceBounded = false;
            }
        }
    }

}

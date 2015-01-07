package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
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
import com.project.jinheng.fyp.BaseActivity;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.Lot;

import org.apache.commons.lang.time.DateUtils;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class LocateVehicleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = LocateVehicleFragment.newInstance(R.layout.fragment_locate_vehicle);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    public static class LocateVehicleFragment extends Fragment {

        private static final String TAG = "LocateVehicleFragment";
        private GoogleMap map;
        private AlertDialog errorDialog;
        private Lot lot;

        public static LocateVehicleFragment newInstance(int layout) {
            LocateVehicleFragment classInstance = new LocateVehicleFragment();
            Bundle args = new Bundle();
            args.putInt("layout", layout);
            classInstance.setArguments(args);
            return classInstance;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(getArguments().getInt("layout"), container, false);

            if (map == null) {
                map = ((MyMapFragment) getFragmentManager().findFragmentById(R.id.lot_detail_map)).getMap();
                if (map == null) {
                    Log.d(TAG, "Map not loaded");
                }
            }
            initializeInterface(view);
            initializeMap();

            final RelativeLayout navigateView = (RelativeLayout) view.findViewById(R.id.navigate_to_vehicle_box);
            final ImageView navigateButton = (ImageView) view.findViewById(R.id.navigate_to_vehicle_button);

            View.OnTouchListener myTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        navigateView.setBackgroundResource(R.color.primary_light);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {

                        navigateView.setBackgroundResource(R.drawable.bg_button_translucent);

                        if (lot != null) {
                            Uri uri = Uri.parse("geo:0,0?q=" + lot.getLatitude() + "," + lot.getLongitude());
                            getActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
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
            //TODO this is temporary
            SharedPreferences settings = getActivity().getSharedPreferences(SplashScreen.PREFS_NAME, 0);
            String lotJson = settings.getString("tempParkedLocation", null);
            String timeJson = settings.getString("parkedTime", null);
            lot = (Lot) APIUtils.fromJSON(lotJson, Lot.class);
            //get time passed
            Calendar startedParking = (Calendar) APIUtils.fromJSON(timeJson, Calendar.class);
            Calendar timeNow = Calendar.getInstance();
            long elapsedHours;
            if (DateUtils.isSameDay(startedParking, timeNow)) {
                elapsedHours = timeNow.get(Calendar.HOUR_OF_DAY) - startedParking.get(Calendar.HOUR_OF_DAY);
            } else {
                //check how many days passed
                long end = timeNow.getTimeInMillis();
                long start = startedParking.getTimeInMillis();
                long elapsedDays = TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
                elapsedHours = timeNow.get(Calendar.HOUR_OF_DAY) - startedParking.get(Calendar.HOUR_OF_DAY) + (elapsedDays * 24);
            }
            TextView lotName = (TextView) view.findViewById(R.id.vehicle_lot_name);
            TextView address = (TextView) view.findViewById(R.id.vehicle_lot_address);
            TextView type = (TextView) view.findViewById(R.id.vehicle_lot_type);
            TextView timeParkedTitle = (TextView) view.findViewById(R.id.detail_vehicle_parked_time_title);
            TextView timeParked = (TextView) view.findViewById(R.id.detail_vehicle_parked_time);

            if (lot != null) {
                if (lotName != null || address != null || type != null || timeParked != null || timeParkedTitle != null) {
                    lotName.setText(lot.getLotName());
                    address.setText(lot.getAddress());
                    type.setText(lot.getLotType());
                    //continue here TODO ;aldfjaldskfja;sldkjf
                } else {
                    showErrorDialog();
                }

            }
            showErrorDialog();
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
    }

}

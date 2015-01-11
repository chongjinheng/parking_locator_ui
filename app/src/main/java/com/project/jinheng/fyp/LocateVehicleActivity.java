package com.project.jinheng.fyp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.Lot;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.Slot;

import org.apache.commons.lang.time.DateUtils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class LocateVehicleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BaseActivity.needSearch = false;
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
        private Slot slot;
        private ProgressDialog progressDialog;
        private boolean unParked = false;

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
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.vehicle_detail_map)).getMap();
                if (map == null) {
                    Log.d(TAG, "Map not loaded");
                }
            }
            initializeInterface(view);
            initializeMap();

            final RelativeLayout navigateView = (RelativeLayout) view.findViewById(R.id.navigate_to_vehicle_box);
            final TextView navigateButton = (TextView) view.findViewById(R.id.navigate_to_vehicle_button);

            View.OnTouchListener myTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        navigateView.setBackgroundResource(R.color.primary_light);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {

                        navigateView.setBackgroundResource(R.drawable.bg_button_translucent);
                        if (!unParked) {
                            if (lot != null) {
                                AlertDialog userConfirmation = new AlertDialog.Builder(getActivity()).create();
                                userConfirmation.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                userConfirmation.setMessage("Are you sure you want to un-park your vehicle?");
                                userConfirmation.setInverseBackgroundForced(true);
                                userConfirmation.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AsyncTask<JSONDTO, Void, JSONDTO> unParkVehicle = new AsyncTask<JSONDTO, Void, JSONDTO>() {
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
                                                if (jsondto != null) {
                                                    if (jsondto.isForceRepark()) {
                                                        unParked = true;
                                                        Toast.makeText(getActivity(), "Your vehicle is successfully un-parked from " + lot.getLotName(), Toast.LENGTH_SHORT).show();
                                                        Uri uri = Uri.parse("geo:0,0?q=" + lot.getLatitude() + "," + lot.getLongitude());
                                                        getActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                                    }
                                                } else {
                                                    Log.e(TAG, "JSON is not returned");
                                                    showErrorDialog();
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
                                            dataToProcess.setEmail(userEmail);
                                            dataToProcess.setServiceName(APIUtils.REMOVE_VEHICLE);
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                            unParkVehicle.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                                        } else {
                                            unParkVehicle.execute(dataToProcess);
                                        }

                                    }
                                });
                                userConfirmation.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //do nothing
                                    }
                                });
                                userConfirmation.show();

                            } else {
                                Log.e(TAG, "Lot not found when trying to intent to navigation");
                                showErrorDialog();
                            }
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
            String json = getActivity().getIntent().getStringExtra("vehicleDetails");
            JSONDTO jsonFromServer = (JSONDTO) APIUtils.fromJSON(json, JSONDTO.class);
            if (jsonFromServer.getSlot() != null) {
                slot = jsonFromServer.getSlot();
                lot = slot.getLot();
            } else {
                Log.e(TAG, "Unable to get slot from intent");
                showErrorDialog();
            }

            //get time passed
            Calendar startedParking = Calendar.getInstance();
            startedParking.setTime(slot.getParkTime());
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
            TextView parkingPrice = (TextView) view.findViewById(R.id.detail_vehicle_price);
            TextView timeParked = (TextView) view.findViewById(R.id.detail_vehicle_parked_time);
            TextView floorTitle = (TextView) view.findViewById(R.id.detail_vehicle_floor_title);
            TextView floor = (TextView) view.findViewById(R.id.detail_vehicle_floor);
            TextView positionTitle = (TextView) view.findViewById(R.id.detail_vehicle_position_title);
            TextView position = (TextView) view.findViewById(R.id.detail_vehicle_position);

            if (lot != null) {
                if (lot.getLotName() != null || lot.getAddress() != null) {
                    lotName.setText(lot.getLotName());
                    address.setText(lot.getAddress());

                    DecimalFormat formatter = new DecimalFormat("RM ##0.00");
                    String output = null;
                    Double subsequentHour;
                    switch (lot.getPrice().getPriceType()) {

                        case "FLAT":
                            output = formatter.format((Double.valueOf(lot.getPrice().getFlatRate()) / 100) * elapsedHours);
                            break;
                        case "DYNAMIC":
                            subsequentHour = (double) (lot.getPrice().getSubsHour() * (elapsedHours - 1) / 100);
                            output = formatter.format(((lot.getPrice().getFirstHour() / 100) + subsequentHour));
                            break;
                        case "RATE":
                            subsequentHour = (double) (lot.getPrice().getSubsHour() * (elapsedHours - 1) / 100);
                            output = formatter.format(((lot.getPrice().getFirstHour() / 100) + subsequentHour));
                            break;
                        default:
                            Log.e(TAG, "Price type not defined");
                            showErrorDialog();
                    }
                    parkingPrice.setText(output);
                    timeParked.setText(elapsedHours + " Hours");

                    if (slot.getFloorLevel() != null || slot.getPosition() != null) {
                        floorTitle.setVisibility(View.VISIBLE);
                        positionTitle.setVisibility(View.VISIBLE);
                        floor.setText(slot.getFloorLevel());
                        position.setText(slot.getPosition());
                    }

                } else {
                    Log.e(TAG, "Unable to get lot name and price from intent");
                    showErrorDialog();
                }

            } else {
                Log.e(TAG, "Unable to get lot from intent");
                showErrorDialog();
            }
        }

        private void initializeMap() {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setTrafficEnabled(true);

            if (lot != null) {
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

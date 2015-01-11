package com.project.jinheng.fyp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.Lot;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.Slot;

import java.util.Date;

/**
 * Created by JinHeng on 1/6/2015.
 */
public class MonitorLocationService extends Service {

    IBinder binder = new LocalBinder();
    LocationManager locationManager;
    String locationProvider;
    Lot lot;
    private Context context;
    private static String TAG = "MonitorLocationService";

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public MonitorLocationService getServiceInstance() {
            return MonitorLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service started");
        context = this;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, 400, 50, myLocationListener);
    }

    LocationListener myLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            SharedPreferences settings = context.getSharedPreferences(SplashScreen.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            String json = settings.getString("serviceTarget", null);
            if (json != null) {
                lot = (Lot) APIUtils.fromJSON(json, Lot.class);
                //calculate distance of location to lot
                //counter 1000 as not available in shared pref
                Integer counter = settings.getInt("serviceCounter", 1000);
                if (counter == 1000) {
                    editor.putInt("serviceCounter", 0).apply();
                }
                if (APIUtils.calculateDistance(location.getLatitude(), location.getLongitude(), lot.getLatitude(), lot.getLongitude(), "M") <= 0.1) {
                    //if is 100 meter around, increase the counter
                    counter++;
                    Log.d(TAG, "Service counter incremented");
                    editor.putInt("serviceCounter", counter).apply();
                }
                //if the user is 100 meter nearby for 5 times consider him as parked
                if (counter == 5) {
                    Log.d(TAG, "Parking vehicle with service....");
                    AsyncTask<JSONDTO, Void, Void> serviceParkVehicle = new AsyncTask<JSONDTO, Void, Void>() {
                        @Override
                        protected Void doInBackground(JSONDTO... params) {
                            try {
                                APIUtils.processAPICalls(params[0]);
                            } catch (MyException e) {
                                Log.e(TAG, e.getMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "Exception occurred when calling API");
                            }
                            return null;
                        }
                    };
                    boolean parked = settings.getBoolean("parked", false);
                    if (parked) {
                        locationManager.removeUpdates(this);
                    } else {
                        JSONDTO dataToProcess = new JSONDTO();
                        dataToProcess.setServiceName(APIUtils.PARK_VEHICLE);
                        Date parkedTime = new Date();
                        String userEmail = settings.getString("email", null);
                        if (userEmail == null) {
                            Log.e(TAG, "Abnormal behavior, user email does not exist");
                        }
                        Slot parkedSlot = new Slot();
                        parkedSlot.setStatus("U");
                        parkedSlot.setParkTime(parkedTime);
                        parkedSlot.setLot(lot);
                        parkedSlot.setParkTime(parkedTime);

                        dataToProcess.setSlot(parkedSlot);
                        dataToProcess.setEmail(userEmail);
                        dataToProcess.setForceRepark(false);

                        //reverse locate the current city of the user,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            serviceParkVehicle.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                        } else {
                            serviceParkVehicle.execute(dataToProcess);
                        }
                        editor.remove("serviceCounter").apply();
                        locationManager.removeUpdates(this);
                    }
                }

            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}

package com.project.jinheng.fyp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.Lot;

/**
 * Created by JinHeng on 1/6/2015.
 */
public class MonitorLocationService extends Service {

    IBinder binder = new LocalBinder();
    LocationManager locationManager;
    String locationProvider;
    Location location;
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
                Integer counter = settings.getInt("serviceCounter", 1000);
                if (counter == 1000) {
                    editor.putInt("serviceCounter", 0).apply();
                }
                if (APIUtils.calculateDistance(location.getLatitude(), location.getLongitude(), lot.getLatitude(), lot.getLongitude(), "M") <= 200) {
                    //if is 200 meter around, increase the counter
                    counter++;
                    editor.putInt("serviceCounter", counter).apply();
                }
                //if the user is 200 meter nearby for 3 times (6 minutes) consider him as parked
                if (counter == 3) {
                    //TODO mark it into the database! User parked
                    editor.remove("serviceCounter").apply();
                    locationManager.removeUpdates(this);
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

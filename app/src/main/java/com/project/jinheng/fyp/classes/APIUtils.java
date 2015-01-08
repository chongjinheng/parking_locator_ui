package com.project.jinheng.fyp.classes;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOError;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * Created by JinHeng on 11/18/2014.
 */
public class APIUtils {

    private static String TAG = "API";
    public static final String APIURL = "http://192.168.1.5:8080/";

    //Service names
    public static final String LOGIN = "LOGIN";
    public static final String FB_LOGIN = "FB_LOGIN";
    public static final String GET_PARKING_LOTS = "GET_PARKING_LOTS";
    public static final String PARK_VEHICLE = "PARK_VEHICLE";
    public static final String CHECK_VEHICLE = "CHECK_VEHICLE";
    public static final String REMOVE_VEHICLE = "REMOVE_VEHICLE";

    public static String facebookPrintKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {

            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);

            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    public static void googleGetShaKey(Activity context) {

        try {

            PackageInfo packageInfo;
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i(TAG, "KeyHash:" + Base64.encodeToString(md.digest(),
                        Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            //TODO handle it
            e.printStackTrace();
        }

    }

    public static JSONDTO processAPICalls(JSONDTO dto) throws Exception {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(APIUtils.APIURL);
            if (dto.getServiceName() == null) {
                throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
            } else {
                String requestJSONString = APIUtils.toJson(dto);
                Log.d(TAG + "\nJSON sent to server:", requestJSONString);

                //Prepare json to send with http post
                post.setEntity(new StringEntity(requestJSONString, "UTF-8"));
                //Setup header types needed to transfer JSON
                post.setHeader("Content-Type", "application/json");
                post.setHeader("Accept-Encoding", "application/json");
                post.setHeader("Accept-Language", "en-US");

                Log.d(TAG, "getting data from server......");
                HttpResponse response = client.execute(post);

                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);

                    Log.d(TAG + "\nJSON sent from server:", data);
                    JSONDTO returnedDTO = APIUtils.create().fromJson(data, JSONDTO.class);
                    if (returnedDTO.getError() != null) {
                        throw new MyException(returnedDTO.getError().getCode(), returnedDTO.getError().getMessage());
                    } else {
                        if (dto.getServiceName().equals(returnedDTO.getServiceName())) {
                            return returnedDTO;
                        } else {
                            throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                        }

                    }
                } else {
                    throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch (HttpHostConnectException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return null;
    }

    public static Gson create() {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
        return gsonBuilder.disableHtmlEscaping().create();
    }

    public static Object fromJSON(final String secret, final Class clazz) {
        return create().fromJson(secret, clazz);
    }

    public static String toJson(final Object dto) {
        return create().toJson(dto);
    }

    public static Double calculateDistance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit.equals("K")) {
            dist = dist * 1.609344;
        } else if (unit.equals("M")) {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}

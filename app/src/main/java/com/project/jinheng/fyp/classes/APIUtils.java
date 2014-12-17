package com.project.jinheng.fyp.classes;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOError;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by JinHeng on 11/18/2014.
 */
public class APIUtils {

    private static String TAG = "API";
    public static final String APIURL = "http://192.168.1.2:8080/";

    //Service names
    public static final String LOGIN = "LOGIN";
    public static final String FB_LOGIN = "FB_LOGIN";
    public static final String GET_MARKER = "GET_MARKER";

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
                    throw new MyException(ErrorStatus.APPLICATION_SYSTEM_DOWN.getName(), ErrorStatus.APPLICATION_SYSTEM_DOWN.getErrorMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
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
}

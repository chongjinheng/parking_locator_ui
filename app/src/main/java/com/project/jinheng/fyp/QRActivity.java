package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.google.zxing.common.StringUtils;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.Slot;

import eu.livotov.zxscan.ScannerFragment;
import eu.livotov.zxscan.ScannerView;

/**
 * Created by JinHeng on 1/27/2015.
 */
public class QRActivity extends Activity implements ScannerView.ScannerViewEventListener {

    private ScannerView scannerView;
    private ProgressDialog progressDialog;
    private static final String TAG = "QRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        if (scannerView == null) {
            scannerView = (ScannerView) findViewById(R.id.scanner_qr);
            scannerView.setScannerViewEventListener(this);
            scannerView.startScanner();
        }
    }

    @Override
    public boolean onCodeScanned(String s) {
        scannerView.stopScanner();
        String intentData = getIntent().getStringExtra("slot");
        Slot slot = (Slot) APIUtils.fromJSON(intentData, Slot.class);

        if (org.apache.commons.lang.StringUtils.isNumeric(s)) {
            slot.setSlotID(Long.valueOf(s));
            updateSlot(slot);
        }
        finish();
        return true;
    }

    private void updateSlot(Slot slot) {
        AsyncTask<JSONDTO, Void, JSONDTO> QRScannedAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {
            @Override
            protected void onPreExecute() {
                progressDialog = MyProgressDialog.initiate(QRActivity.this);
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
                    if (jsondto.getError() != null) {
                        showErrorDialog(new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage()));
                    }
                    if (jsondto.isSlotUpdated()) {
                        Toast.makeText(QRActivity.this, "Your vehicle location is saved successfully", Toast.LENGTH_SHORT).show();
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
        dataToProcess.setServiceName(APIUtils.QR_SCANNED);
        dataToProcess.setSlot(slot);
        SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
        String email = settings.getString("email", null);
        dataToProcess.setEmail(email);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            QRScannedAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
        } else {
            QRScannedAPICall.execute(dataToProcess);
        }
    }

    private void showErrorDialog(MyException e) {
        AlertDialog error = new AlertDialog.Builder(QRActivity.this).create();
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

}

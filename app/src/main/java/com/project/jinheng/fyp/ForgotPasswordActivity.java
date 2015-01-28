package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.MyException;

/**
 * Created by JinHeng on 12/2/2014.
 */
public class ForgotPasswordActivity extends Activity {

    private ImageButton imageButton;
    private RelativeLayout fpasswordContainer;
    private EditText fpasswordEmail;
    private ActionProcessButton resetPasswordButton;
    private Button resetPasswordButtonFake;
    private ProgressDialog progressDialog;
    private static String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        imageButton = (ImageButton) findViewById(R.id.dropdown_arrow_fpassword);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
            }
        });

        /**
         * Define when will the register button be enabled
         */
        TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    checkInputAndEnableReset();
                    return true;
                }
                return false;
            }
        };
        fpasswordContainer = (RelativeLayout) findViewById(R.id.fpassword_container);

        fpasswordEmail = (EditText) findViewById(R.id.forgot_pass_edit_text);
        fpasswordEmail.setOnEditorActionListener(editorActionListener);

        resetPasswordButton = (ActionProcessButton) findViewById(R.id.button_reset_pass);
        resetPasswordButtonFake = (Button) findViewById(R.id.button_fpassword_disabled);

        fpasswordContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                checkInputAndEnableReset();
            }
        });

    }

    public void checkInputAndEnableReset() {
        if (!fpasswordEmail.getText().toString().equals("")) {
            resetPasswordButton.setEnabled(true);
            resetPasswordButton.setAlpha(1.0f);
            resetPasswordButtonFake.setAlpha(0.0f);
            resetPasswordButtonFake.setVisibility(View.INVISIBLE);

        } else {
            resetPasswordButton.setEnabled(false);
            resetPasswordButton.setAlpha(0.0f);
            resetPasswordButtonFake.setAlpha(0.5f);
            resetPasswordButtonFake.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public void forgotPasswordButtonClicked(View view) {

        AsyncTask<JSONDTO, Void, JSONDTO> forgotPasswordAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {
            @Override
            protected void onPreExecute() {
                progressDialog = MyProgressDialog.initiate(ForgotPasswordActivity.this);
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
                    if (jsondto != null) {
                        if (jsondto.getError() != null) {
                            throw new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                        } else {
                            AlertDialog fpSuccess = new AlertDialog.Builder(ForgotPasswordActivity.this).create();
                            fpSuccess.setCancelable(false);
                            fpSuccess.setTitle("Password reset");
                            fpSuccess.setMessage("Password reset Successfully!\nYou will receive an email with your temporary pin soon.");
                            fpSuccess.setCancelable(false);
                            fpSuccess.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
                                }
                            });
                            fpSuccess.show();

                        }
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                } catch (MyException e) {
                    showErrorDialog(e);
                }
            }
        };

        JSONDTO dataToProcess = new JSONDTO();
        dataToProcess.setServiceName(APIUtils.FORGOT_PASSWORD);
        dataToProcess.setEmail(fpasswordEmail.getText().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            forgotPasswordAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
        } else {
            forgotPasswordAPICall.execute(dataToProcess);
        }
    }

    private void showErrorDialog(MyException e) {
        AlertDialog error = new AlertDialog.Builder(this).create();
        error.requestWindowFeature(Window.FEATURE_NO_TITLE);
        error.setMessage(e.getMessage());
        error.setInverseBackgroundForced(true);
        error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        error.show();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

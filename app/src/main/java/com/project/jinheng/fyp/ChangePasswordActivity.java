package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.Validators;

import org.w3c.dom.Text;

/**
 * Created by JinHeng on 1/11/2015.
 */
public class ChangePasswordActivity extends Activity {

    private static String TAG = "ChangePasswordActivity";
    private RelativeLayout cpasswordContainer;
    private EditText currentPassword;
    private EditText changePassword;
    private EditText repeatChangePassword;
    private TextView changePasswordValidator;
    private TextView changePasswordRepeatValidator;
    private ActionProcessButton changePasswordButton;
    private Button changePasswordButtonFake;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

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
        cpasswordContainer = (RelativeLayout) findViewById(R.id.cpassword_container);

        currentPassword = (EditText) findViewById(R.id.current_pass_edit_text);
        currentPassword.setOnEditorActionListener(editorActionListener);
        changePassword = (EditText) findViewById(R.id.change_pass_edit_text);
        changePassword.setOnEditorActionListener(editorActionListener);
        repeatChangePassword = (EditText) findViewById(R.id.change_pass_repeat_edit_text);
        repeatChangePassword.setOnEditorActionListener(editorActionListener);
        changePasswordValidator = (TextView) findViewById(R.id.change_pass_validator);
        changePasswordRepeatValidator = (TextView) findViewById(R.id.change_pass_not_match);

        changePasswordButton = (ActionProcessButton) findViewById(R.id.button_change_pass);
        changePasswordButtonFake = (Button) findViewById(R.id.button_cpassword_disabled);

        cpasswordContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                checkInputAndEnableReset();
            }
        });
    }

    public void checkInputAndEnableReset() {
        if (!changePassword.getText().toString().equals("") || repeatChangePassword.getText().toString().equals("")) {
            changePasswordButton.setEnabled(true);
            changePasswordButton.setAlpha(1.0f);
            changePasswordButtonFake.setAlpha(0.0f);
            changePasswordButtonFake.setVisibility(View.INVISIBLE);

        } else {
            changePasswordButton.setEnabled(false);
            changePasswordButton.setAlpha(0.0f);
            changePasswordButtonFake.setAlpha(0.5f);
            changePasswordButtonFake.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public void changePasswordButtonClicked(View view) {
        boolean inputCorrect = false;
        //get facebook login info and email from shared preference
        final SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
        String email = settings.getString("email", null);
        boolean facebookLog = settings.getBoolean("facebookLog", false);
        boolean forceChangePassword = settings.getBoolean("forceChangePassword", false);

        //check if there are any errors
        if (!Validators.checkPasswordStyle(changePassword.getText().toString())) {
            changePasswordValidator.setVisibility(View.VISIBLE);
            inputCorrect = false;
        } else {
            inputCorrect = true;
        }
        if (!changePassword.getText().toString().equals(repeatChangePassword.getText().toString())) {
            if (changePasswordValidator.getVisibility() == View.INVISIBLE) {
                changePasswordRepeatValidator.setVisibility(View.VISIBLE);
                inputCorrect = false;
            }
        } else {
            inputCorrect = true;
        }
        if (inputCorrect) {

            AsyncTask<JSONDTO, Void, JSONDTO> forgotPasswordAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {
                @Override
                protected void onPreExecute() {
                    progressDialog = MyProgressDialog.initiate(ChangePasswordActivity.this);
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
                                AlertDialog cpSuccess = new AlertDialog.Builder(ChangePasswordActivity.this).create();
                                cpSuccess.setCancelable(false);
                                cpSuccess.setTitle("Password Changed");
                                cpSuccess.setMessage("Password is successfully changed. \n\nReturning to Home page.");
                                cpSuccess.setCancelable(false);
                                cpSuccess.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ChangePasswordActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.remove("forceChangePassword").apply();
                                    }
                                });
                                cpSuccess.show();
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
            if (!facebookLog) {
                if (email != null) {
                    dataToProcess.setServiceName(APIUtils.CHANGE_PASSWORD);
                    dataToProcess.setEmail(email);
                    dataToProcess.setOldPassword(currentPassword.getText().toString().trim());
                    dataToProcess.setPassword(changePassword.getText().toString().trim());
                    dataToProcess.setForceChangePassword(forceChangePassword);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        forgotPasswordAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                    } else {
                        forgotPasswordAPICall.execute(dataToProcess);
                    }
                }
            } else {
                Toast.makeText(this, "Facebook user is not allowed to change password", Toast.LENGTH_SHORT).show();
            }
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

package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
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
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.Validators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by JinHeng on 10/29/2014.
 */
public class RegisterActivity extends Activity {

    private String TAG = "Register Activity";
    private ImageButton imageButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private TextView emailValidator;
    private TextView passwordValidator;
    private TextView repeatPasswordValidator;
    private RelativeLayout registerContainer;
    private ActionProcessButton registerButton;
    private Button registerButtonFake;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageButton = (ImageButton) findViewById(R.id.dropdown_arrow_register);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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
                    checkInputAndEnableRegister();
                    return true;
                }
                return false;
            }
        };
        registerContainer = (RelativeLayout) findViewById(R.id.register_container);

        emailEditText = (EditText) findViewById(R.id.email_register);
        emailEditText.setOnEditorActionListener(editorActionListener);
        passwordEditText = (EditText) findViewById(R.id.password_register);
        passwordEditText.setOnEditorActionListener(editorActionListener);
        repeatPasswordEditText = (EditText) findViewById(R.id.password_register_repeat);
        repeatPasswordEditText.setOnEditorActionListener(editorActionListener);

        emailValidator = (TextView) findViewById(R.id.email_validator_text);
        passwordValidator = (TextView) findViewById(R.id.password_style_validator_text);
        repeatPasswordValidator = (TextView) findViewById(R.id.password_repeat_validator_text);
        registerButton = (ActionProcessButton) findViewById(R.id.button_register);
        registerButtonFake = (Button) findViewById(R.id.button_register_disabled);
        registerButton.setEnabled(false);

        registerContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                checkInputAndEnableRegister();
            }
        });
    }

    public void checkInputAndEnableRegister() {
        if (!emailEditText.getText().toString().equals("") && !passwordEditText.getText().toString().equals("")
                && !repeatPasswordEditText.getText().toString().equals("")) {
            registerButton.setEnabled(true);
            registerButton.setAlpha(1.0f);
            registerButtonFake.setAlpha(0.0f);
            registerButtonFake.setVisibility(View.INVISIBLE);

        } else {
            registerButton.setEnabled(false);
            registerButton.setAlpha(0.0f);
            registerButtonFake.setAlpha(0.5f);
            registerButtonFake.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
    }

    public void registerButtonClicked(View view) {
        boolean inputCorrect = false;

        //check if there are any errors
        if (!Validators.validateEmail(emailEditText.getText().toString())) {
            emailValidator.setVisibility(View.VISIBLE);
        } else {
            inputCorrect = true;
        }
        if (!Validators.checkPasswordStyle(passwordEditText.getText().toString())) {
            passwordValidator.setVisibility(View.VISIBLE);
            inputCorrect = false;
        } else {
            inputCorrect = true;
        }
        if (!passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
            if (passwordValidator.getVisibility() == View.INVISIBLE) {
                repeatPasswordValidator.setVisibility(View.VISIBLE);
                inputCorrect = false;
            }
        } else {
            inputCorrect = true;
        }
        if (inputCorrect) {
            emailValidator.setVisibility(View.INVISIBLE);
            repeatPasswordValidator.setVisibility(View.INVISIBLE);
            passwordValidator.setVisibility(View.INVISIBLE);
            AsyncTask<JSONDTO, Void, JSONDTO> registerAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {

                @Override
                protected void onPreExecute() {
                    progressDialog = MyProgressDialog.initiate(RegisterActivity.this);
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
                    //process login
                    if (jsondto != null) {
                        if (jsondto.getError() != null) {
                            MyException e = new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                            showRegisterResultDialog(false, e);
                        } else if (jsondto.getLoginMode() != null || jsondto.getEmail() != null) {
                            int loginMode = jsondto.getLoginMode();

                            if (loginMode == 0) {
                                //register success
                                SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();

                                editor.putBoolean("LoggedIn",true);
                                editor.putBoolean("facebookLog", false);
                                editor.putString("email", jsondto.getEmail().trim());
                                String[] splitEmail = jsondto.getEmail().trim().split("@");
                                String name = splitEmail[0];
                                editor.putString("name", name);
                                editor.apply();
                                showRegisterResultDialog(true, null);
                            } else {
                                showRegisterResultDialog(false, null);
                            }

                        } else {
                            showRegisterResultDialog(false, null);
                        }
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            };

            JSONDTO dataToProcess = new JSONDTO();
            dataToProcess.setServiceName(APIUtils.REGISTER);
            dataToProcess.setEmail(emailEditText.getText().toString());
            dataToProcess.setPassword(passwordEditText.getText().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                registerAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
            } else {
                registerAPICall.execute(dataToProcess);
            }

        }
    }

    public void showRegisterResultDialog(Boolean success, MyException e) {

        AlertDialog registerDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        registerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        registerDialog.setInverseBackgroundForced(true);
        if (success) {
            registerDialog.setMessage("Congratulations! \nYou have registered successfully.\n\nA confirmation will be sent to you shortly.");
            registerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    ComponentName cn = intent.getComponent();
                    Intent clearTopIntent = IntentCompat.makeRestartActivityTask(cn);
                    startActivity(clearTopIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.top_to_bottom_in);
                    finish();
                }
            });
        } else {
            if (e != null) {
                registerDialog.setMessage(e.getMessage());
            } else {
                registerDialog.setMessage("Something went wrong, Please try again later.");
            }
            registerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

        }

        registerDialog.show();
    }

}


package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dd.processbutton.iml.ActionProcessButton;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.LoginUtils;
import com.project.jinheng.fyp.classes.MyException;

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
        passwordValidator = (TextView) findViewById(R.id.password_validator_text);
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
        //TODO db register api call here
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);

        EditText email = (EditText) findViewById(R.id.email_register);
        EditText password = (EditText) findViewById(R.id.password_register);
        EditText passwordRepeat = (EditText) findViewById(R.id.password_register_repeat);

        //TODO register api call here
        boolean registerSuccess = true;
        //process login
        if (registerSuccess) {
            try {
                if (email.getText() != null && password.getText() != null && !TextUtils.isEmpty(email.getText()) && !TextUtils.isEmpty(password.getText())) {
                    boolean loginSuccess = LoginUtils.proceesLogin(email.getText().toString(), password.getText().toString());
                    if (loginSuccess) {
                        //let the app remember user have logged in
                        SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean("LoggedIn", true);
                        editor.apply();

                        startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                        finish();
                    } else
                        throw new MyException(ErrorStatus.LOGIN_ERROR, ErrorStatus.LOGIN_ERROR.getErrorMessage());
                }
            } catch (MyException e) {
                AlertDialog error = new AlertDialog.Builder(this).create();
                error.setTitle(e.getError().getName());
                error.setMessage(e.getMessage());
                error.setInverseBackgroundForced(true);
                error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                    TODO don't know what to do here
                    }
                });
                error.show();
            }
        }

    }

}
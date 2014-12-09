package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.LoginUtils;
import com.project.jinheng.fyp.classes.MyException;

import java.util.Arrays;

/**
 * Created by JinHeng on 10/28/2014.
 */
public class LoginActivity extends Activity {

    public static final String TAG = "LoginActivity";
    private UiLifecycleHelper uiHelper;
    private LoginButton loginButton;
    private RelativeLayout loginContainer;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {
            onSessionStateChange(session, sessionState, e);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        //to override the base interface
        setContentView(R.layout.activity_login);

        loginButton = (LoginButton) findViewById(R.id.facebookLoginButton);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        loginContainer = (RelativeLayout) findViewById(R.id.login_container);
        loginContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Facebook session opened");

//            final Session requestSession = session;
//            Request request = Request.newMeRequest(session, new Request.GraphUserCallback(){
//                @Override
//                public void onCompleted(GraphUser graphUser, Response response) {
//                    if (requestSession == Session.getActiveSession() && graphUser != null){
//                        Log.i("Facebook", "Username found " + graphUser.getName());

            Log.d(TAG, "Facebook login successful, displaying home activity");
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("LoggedIn", true);
            editor.apply();

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

        } else if (state.isClosed()) {
            System.out.println("invoked close");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void loginButtonClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

        EditText email = (EditText) findViewById(R.id.username_login);
        EditText password = (EditText) findViewById(R.id.password_login);
        //process login
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
                    overridePendingTransition(R.anim.right_to_left_in, R.anim.fade_out);
                    finish();
                } else
                    throw new MyException(ErrorStatus.LOGIN_ERROR, ErrorStatus.LOGIN_ERROR.getErrorMessage());
            } else {
                throw new MyException(ErrorStatus.NO_INFO, ErrorStatus.NO_INFO.getErrorMessage());
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

    public void registerTextClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.bottom_to_top_in, R.anim.fade_out);
    }

    public void forgotPasswordClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right_in, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
}

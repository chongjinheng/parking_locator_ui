package com.project.jinheng.fyp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.project.jinheng.fyp.classes.APIUtils;
import com.project.jinheng.fyp.classes.ErrorStatus;
import com.project.jinheng.fyp.classes.JSONDTO;
import com.project.jinheng.fyp.classes.JSONError;
import com.project.jinheng.fyp.classes.MyException;
import com.project.jinheng.fyp.classes.Validators;

import java.net.URL;
import java.util.Arrays;

/**
 * Created by JinHeng on 10/28/2014.234
 */
public class LoginActivity extends Activity {

    public static final String TAG = "LoginActivity";
    private UiLifecycleHelper uiHelper;
    private ProgressDialog progressDialog;
    private Integer loginModeFromServer;
    private Boolean isActivityRunning;
    private String facebookUID; //save into preference
    private String loginEmail; //save into preference
    private String facebookUserName; //save into preference
    private String facebookUserEmail; //save into preference

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

        LoginButton loginButton = (LoginButton) findViewById(R.id.facebookLoginButton);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        RelativeLayout loginContainer = (RelativeLayout) findViewById(R.id.login_container);
        loginContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    private void onSessionStateChange(final Session session, SessionState state, final Exception exception) {
        if (state.isOpened()) {
            Log.d(TAG, "Facebook session opened");

            //declare Asynctask's task
            AsyncTask<JSONDTO, Void, JSONDTO> loginAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {

                @Override
                protected void onPreExecute() {
                    progressDialog = MyProgressDialog.initiate(LoginActivity.this);
                    progressDialog.show();
                }

                @Override
                protected JSONDTO doInBackground(JSONDTO... params) {

                    final Session requestSession;
                    requestSession = session;
                    Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser graphUser, Response response) {
                            if (requestSession == Session.getActiveSession() && graphUser != null) {
                                Log.i("Facebook", "User ID found " + graphUser.getId());
                                facebookUID = graphUser.getId();
                                facebookUserName = graphUser.getName();
                                facebookUserEmail = graphUser.getProperty("email").toString();
                            }
                        }
                    });
                    request.executeAndWait();

                    if (exception != null) {
                        JSONDTO returnDTO = new JSONDTO();
                        Log.e(TAG, "Exception occurred when logging into Facebook");
                        JSONError error = new JSONError("Facebook login exception", exception.getMessage());
                        returnDTO.setError(error);
                        return returnDTO;
                    }

                    JSONDTO jsonFromServer, tempDTO;
                    try {
                        //because now only we get facebook username to call the api
                        tempDTO = params[0];
                        tempDTO.setFacebookUID(facebookUID);
                        tempDTO.setName(facebookUserName);
                        jsonFromServer = APIUtils.processAPICalls(tempDTO);
                        return jsonFromServer;

                    } catch (MyException e) {
                        Log.e(TAG, e.getMessage());
                        JSONDTO returnDTO = new JSONDTO();
                        JSONError error = new JSONError(e.getError(), e.getMessage());
                        returnDTO.setError(error);
                        return returnDTO;
                    } catch (Exception e) {
                        JSONDTO returnDTO = new JSONDTO();
                        Log.e(TAG, "Exception occurred when calling API");
                        JSONError error = new JSONError(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                        returnDTO.setError(error);
                        return returnDTO;
                    }
                }

                @Override
                protected void onPostExecute(JSONDTO jsondto) {
                    try {

                        if (jsondto.getError() != null) {
                            throw new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                        } else {
                            Boolean loginStatus = false;

                            if (jsondto.getLoginMode() == null || !jsondto.getServiceName().equals(APIUtils.FB_LOGIN)) {
                                Log.e(TAG, "no login mode or username is null");
                                throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                            } else {
                                if (jsondto.getLoginMode() == 2) {
                                    loginStatus = true;
                                }
                            }

                            if (loginStatus) {
                                saveLoginState();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Log.d(TAG, "Facebook login successful, displaying home activity");
                                startActivity(intent);
                                overridePendingTransition(R.anim.right_to_left_in, R.anim.fade_out);
                                finish();
                            } else {
                                Log.e(TAG, "did not logged in successfully");
                                throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                            }

                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }
                        }

                    } catch (MyException e) {

                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                        AlertDialog error = new AlertDialog.Builder(LoginActivity.this).create();
                        error.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        error.setMessage(e.getMessage());
                        error.setInverseBackgroundForced(true);
                        error.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        });
                        error.show();

                        Session session = Session.getActiveSession();
                        if (session != null) {
                            if (!session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session = new Session(LoginActivity.this);
                            Session.setActiveSession(session);
                            session.closeAndClearTokenInformation();
                        }
                    }
                }
            };

            //finish declaring async task definition
            //build json object to process login
            JSONDTO dataToProcess = new JSONDTO();
            dataToProcess.setServiceName(APIUtils.FB_LOGIN);
            dataToProcess.setName(facebookUserName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                loginAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
            } else {
                loginAPICall.execute(dataToProcess);
            }

        } else if (state.isClosed())

        {
            System.out.println("invoked close");
            if (!isActivityRunning) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    public void loginButtonClicked(View view) {
        try {
            EditText emailEditText = (EditText) findViewById(R.id.username_login);
            EditText passwordEditText = (EditText) findViewById(R.id.password_login);
            //process login
            if (emailEditText.getText() != null && passwordEditText.getText() != null && !TextUtils.isEmpty(emailEditText.getText()) && !TextUtils.isEmpty(passwordEditText.getText())) {
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();

                //check for input pattern
                if (!Validators.validateEmail(email)) {
                    Log.e(TAG, "email style error");
                    throw new MyException(ErrorStatus.EMAIL_STYLE_ERROR.getName(), ErrorStatus.EMAIL_STYLE_ERROR.getErrorMessage());
                } else if (!Validators.checkPasswordStyle(password)) {
                    Log.e(TAG, "password style error");
                    throw new MyException(ErrorStatus.PASSWORD_STYLE_ERROR.getName(), ErrorStatus.PASSWORD_STYLE_ERROR.getErrorMessage());
                }

                //declare Asynctask's task
                AsyncTask<JSONDTO, Void, JSONDTO> loginAPICall = new AsyncTask<JSONDTO, Void, JSONDTO>() {

                    @Override
                    protected void onPreExecute() {
                        progressDialog = MyProgressDialog.initiate(LoginActivity.this);
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

                            if (jsondto.getError() != null) {
                                throw new MyException(jsondto.getError().getCode(), jsondto.getError().getMessage());
                            } else {
                                Boolean loginStatus = false;

                                if (jsondto.getLoginMode() != null) {
                                    loginModeFromServer = jsondto.getLoginMode();
                                } else {
                                    Log.e(TAG, "no login mode");
                                    throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                                }

                                switch (loginModeFromServer) {
                                    case 0:
                                        Log.d(TAG, "login successfully");
                                        loginStatus = true;
                                        break;

                                    case 1:
                                        Log.d(TAG, "logged in with temporary password");
                                        loginStatus = true;
                                        break;
                                }

                                if (loginStatus) {
                                    loginEmail = email;
                                    saveLoginState();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.right_to_left_in, R.anim.fade_out);
                                    finish();
                                } else {
                                    Log.e(TAG, "did not logged in successfully");
                                    throw new MyException(ErrorStatus.ACCESS_DENIED.getName(), ErrorStatus.ACCESS_DENIED.getErrorMessage());
                                }

                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                            }

                        } catch (MyException e) {
                            showErrorDialog(e);
                            if (progressDialog != null) {
                                progressDialog.dismiss();
                            }

                        }
                    }
                };

                //finish declaring async task definition
                //build json object to process login
                JSONDTO dataToProcess = new JSONDTO();
                dataToProcess.setServiceName(APIUtils.LOGIN);
                dataToProcess.setEmail(email.trim());
                dataToProcess.setPassword(password.trim());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    loginAPICall.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataToProcess);
                } else {
                    loginAPICall.execute(dataToProcess);
                }
            } else {
                Log.e(TAG, "info is not sufficient");
                throw new MyException(ErrorStatus.NO_INFO.getName(), ErrorStatus.NO_INFO.getErrorMessage());
            }
        } catch (MyException e) {
            showErrorDialog(e);
        }
    }

    private void saveLoginState() {
        //let the app remember user have logged in
        SharedPreferences settings = getSharedPreferences(SplashScreen.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("LoggedIn", true);
        if (facebookUID != null) {
            editor.putBoolean("facebookLog", true);
            editor.putString("facebookUID", facebookUID);
            editor.putString("name", facebookUserName);
            editor.putString("email", facebookUserEmail);
        }
        if (loginEmail != null) {
            editor.putBoolean("facebookLog", false);
            editor.putString("email", loginEmail);
            String[] splitEmail = loginEmail.split("@");
            String name = splitEmail[0];
            editor.putString("name", name);
        }

        editor.apply();
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
        isActivityRunning = true;
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityRunning = false;
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityRunning = false;
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        uiHelper.onDestroy();
    }

    @Override
    protected void onStart() {
        isActivityRunning = true;
        super.onStart();
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

    private void showErrorDialog(MyException e) {
        AlertDialog error = new AlertDialog.Builder(LoginActivity.this).create();
        error.setTitle(e.getError());
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

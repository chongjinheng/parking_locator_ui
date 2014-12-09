package com.project.jinheng.fyp.classes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.project.jinheng.fyp.R;
import com.project.jinheng.fyp.SplashScreen;

/**
 * Created by JinHeng on 11/9/2014.
 */
public class LoginUtils {

    //hardcode info from server TODO implement api
    final static String emailFromServer = "admin@gmail.com";
    final static String passwordFromServer = "admin";

    public static boolean proceesLogin(String email, String password) throws MyException{
        try {
            email = email.trim();
            password = password.trim();
            if (email.equals(emailFromServer)) {
                if (password.equals(passwordFromServer)) {
                    //TODO log something

                    return true;
                } else {
                    Log.d("Login Error", "password not match");
                    System.out.println("password not match");
                    throw new MyException(ErrorStatus.LOGIN_ERROR, ErrorStatus.LOGIN_ERROR.getErrorMessage());
                }
            } else {
                //log something TODO change logging
                System.out.println("username not found");
                throw new MyException(ErrorStatus.LOGIN_ERROR, ErrorStatus.LOGIN_ERROR.getErrorMessage());
            }
        } catch (MyException e) {
            throw e;
        }
    }
}

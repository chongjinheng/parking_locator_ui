package com.project.jinheng.fyp.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by JinHeng on 12/17/2014.
 */
public class Validators {

    /**
     * Validations for email address format
     *
     * @param inputEmail
     * @return boolean
     */
    public static boolean validateEmail(String inputEmail) {
        Pattern pattern;
        Matcher matcher;

        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(EMAIL_PATTERN);

        matcher = pattern.matcher(inputEmail);
        return matcher.matches();
    }

    /**
     * Validations for password address format
     *
     * @param password
     * @return boolean
     */
    public static boolean checkPasswordStyle(String password) {
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^[\\p{L}\\p{N}]{8,15}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);

        matcher = pattern.matcher(password);
        return matcher.matches();

    }
}

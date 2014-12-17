package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 10/29/2014.
 */
public class MyException extends Exception {
    private final String error;

    public MyException(String error, String errorMessage) {
        super(errorMessage);
        this.error = error;
    }

    public String getError() {
        return error;
    }

}

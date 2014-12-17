package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 12/17/2014.
 */
public class JSONError {

    private final String code;
    private final String message;

    public JSONError(final String errorCode, final String message) {
        this.code = errorCode;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 10/29/2014.
 */
public enum ErrorStatus {

    ACCESS_DENIED("Access Denied", "Access is denied"),
    NO_INFO("Error", "Please fill in the required information"),
    LOGIN_ERROR("Login Error", "Please check and ensure that both email address and password are correct");

    private final String name;
    private final String errorMessage;

    ErrorStatus(final String name, final String errorMessage){
        this.name = name;
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

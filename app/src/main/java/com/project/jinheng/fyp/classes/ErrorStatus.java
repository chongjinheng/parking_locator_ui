package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 10/29/2014.
 */
public enum ErrorStatus {

    ACCESS_DENIED("Access Denied", "Access is denied."),
    NO_INFO("No entries", "Please fill in the required information."),
    PASSWORD_STYLE_ERROR("Login Error", "Your password must be within 8 to 15 characters."),
    EMAIL_STYLE_ERROR("Login Error", "Please check and ensure that the email address entered is valid."),
    APPLICATION_SYSTEM_DOWN("System Down", "System is currently unavailable. Please try again later"),
    GOOGLE_NO_RESULT("No entries", "No matching places found.");

    private final String name;
    private final String errorMessage;

    ErrorStatus(final String name, final String errorMessage) {
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

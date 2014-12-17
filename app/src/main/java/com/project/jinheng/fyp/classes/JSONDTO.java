package com.project.jinheng.fyp.classes;

import java.util.List;

/**
 * Created by JinHeng on 12/16/2014.
 */
public class JSONDTO {

    private String serviceName;
    private String email;
    private String name;
    private String facebookUID;
    private String password;
    private Integer loginMode;
    private JSONError error;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getLoginMode() {
        return loginMode;
    }

    public void setLoginMode(Integer loginMode) {
        this.loginMode = loginMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFacebookUID() {
        return facebookUID;
    }

    public void setFacebookUID(String facebookUID) {
        this.facebookUID = facebookUID;
    }

    public JSONError getError() {
        return error;
    }

    public void setError(JSONError error) {
        this.error = error;
    }
}

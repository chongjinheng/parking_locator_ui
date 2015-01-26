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

    private Long ID;
    /**
     * Change Password **
     */
    private String oldPassword;
    private boolean isForceChangePassword;

    /**
     * Maps ***
     */
    private Double latitude;
    private Double longitude;
    private List<Lot> parkingLots;
    private String groupType;
    private String criteria;

    /**
     * Parking *
     */
    private Slot slot;
    private Boolean alreadyParkedThere;
    private Boolean forceRepark;
    private Boolean slotUpdated;

    public Boolean isSlotUpdated() {
        return slotUpdated;
    }

    public void setSlotUpdated(Boolean slotUpdated) {
        this.slotUpdated = slotUpdated;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public boolean isForceChangePassword() {
        return isForceChangePassword;
    }

    public void setForceChangePassword(boolean isForceChangePassword) {
        this.isForceChangePassword = isForceChangePassword;
    }

    public Boolean getForceRepark() {
        return forceRepark;
    }

    public Boolean getAlreadyParkedThere() {
        return alreadyParkedThere;
    }

    public Boolean isForceRepark() {
        return forceRepark;
    }

    public void setForceRepark(Boolean forceRepark) {
        this.forceRepark = forceRepark;
    }

    public Boolean isAlreadyParkedThere() {
        return alreadyParkedThere;
    }

    public void setAlreadyParkedThere(Boolean alreadyParkedThere) {
        this.alreadyParkedThere = alreadyParkedThere;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public List<Lot> getParkingLots() {
        return parkingLots;
    }

    public void setParkingLots(List<Lot> parkingLots) {
        this.parkingLots = parkingLots;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

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

package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 12/16/2014.
 */
public class Lot {

    private Integer id;
    private String lotName;
    private String lotType;
    private Double longitude;
    private Double latitude;
    private String address;
    private String operationHour;
    private Long capacity;
    private String availability;
    private String nearbyAttraction;
    private String city;
    private String state;
    private Price price;
    private Integer openHour;
    private Integer closeHour;

    public Integer getOpenHour() {
        return openHour;
    }

    public void setOpenHour(Integer openHour) {
        this.openHour = openHour;
    }

    public Integer getCloseHour() {
        return closeHour;
    }

    public void setCloseHour(Integer closeHour) {
        this.closeHour = closeHour;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNearbyAttraction() {
        return nearbyAttraction;
    }

    public void setNearbyAttraction(String nearbyAttraction) {
        this.nearbyAttraction = nearbyAttraction;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public String getLotType() {
        return lotType;
    }

    public void setLotType(String lotType) {
        this.lotType = lotType;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOperationHour() {
        return operationHour;
    }

    public void setOperationHour(String operationHour) {
        this.operationHour = operationHour;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}

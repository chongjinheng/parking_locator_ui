package com.project.jinheng.fyp.classes;

import java.math.BigDecimal;

/**
 * Created by JinHeng on 12/16/2014.
 */
public class Lot {

    private Integer id;
    private String lotName;
    private String lotType;
    private String longitute;
    private String latitute;
    private String address;
    private String operationHour;
    private Long capacity;
    private String availability;

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

    public String getLongitute() {
        return longitute;
    }

    public void setLongitute(String longitute) {
        this.longitute = longitute;
    }

    public String getLatitute() {
        return latitute;
    }

    public void setLatitute(String latitute) {
        this.latitute = latitute;
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

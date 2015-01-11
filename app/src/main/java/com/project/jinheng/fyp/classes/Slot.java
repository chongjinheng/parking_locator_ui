package com.project.jinheng.fyp.classes;

import com.project.jinheng.fyp.classes.adapters.ParkingUser;

import java.util.Date;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class Slot {

    private Long slotID;

    private String floorLevel;

    private String position;

    private Date parkTime;

    private String status;

    private ParkingUser user;

    private Lot lot;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Long getSlotID() {
        return slotID;
    }

    public void setSlotID(Long slotID) {
        this.slotID = slotID;
    }

    public String getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(String floorLevel) {
        this.floorLevel = floorLevel;
    }

    public Date getParkTime() {
        return parkTime;
    }

    public void setParkTime(Date parkTime) {
        this.parkTime = parkTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ParkingUser getUser() {
        return user;
    }

    public void setUser(ParkingUser user) {
        this.user = user;
    }

    public Lot getLot() {
        return lot;
    }

    public void setLot(Lot lot) {
        this.lot = lot;
    }
}

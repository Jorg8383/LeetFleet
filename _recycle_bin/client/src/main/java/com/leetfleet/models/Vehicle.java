package com.leetfleet.models;

public class Vehicle {

    private int mileage;
    private int oilLevel;
    private int tyrePressure;

    private boolean doorsLocked;

    public Vehicle(){}

    public Vehicle(int mileage, int oilLevel, int tyrePressure) {
        this.mileage = mileage;
        this.oilLevel = oilLevel;
        this.tyrePressure = tyrePressure;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public int getOilLevel() {
        return oilLevel;
    }

    public void setOilLevel(int oilLevel) {
        this.oilLevel = oilLevel;
    }

    public int getTyrePressure() {
        return tyrePressure;
    }

    public void setTyrePressure(int tyrePressure) {
        this.tyrePressure = tyrePressure;
    }

    public void changeDoorsLocked() {
        this.doorsLocked = !this.doorsLocked;
    }

    public boolean isDoorsLocked() {
        return doorsLocked;
    }

    public void setDoorsLocked(boolean doorsLocked) {
        this.doorsLocked = doorsLocked;
    }
}

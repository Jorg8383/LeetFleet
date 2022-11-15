package lf.core;

import java.util.Objects;

public class Vehicle {

    private String vehicleId;
    private String fleetId;
    // private boolean isOn;

    public Vehicle() {
    }

    public Vehicle(String vehicleId, String fleetId) {
        this.vehicleId = vehicleId;
        this.fleetId = fleetId;
        // this.isOn = isOn;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    // public void setVehicleId(int vehicleId) {
    // this.vehicleId = vehicleId;
    // }

    public String getFleetId() {
        return this.fleetId;
    }

    // public void setFleetId(int fleetId) {
    // this.fleetId = fleetId;
    // }

    // public boolean isIsOn() {
    // return this.isOn;
    // }

    // public boolean getIsOn() {
    // return this.isOn;
    // }

    // public void setIsOn(boolean isOn) {
    // this.isOn = isOn;
    // }

    // public Vehicle vehicleId(int vehicleId) {
    // setVehicleId(vehicleId);
    // return this;
    // }

    // public Vehicle fleetId(int fleetId) {
    // setFleetId(fleetId);
    // return this;
    // }

    // public Vehicle isOn(boolean isOn) {
    // setIsOn(isOn);
    // return this;
    // }

    // @Override
    // public boolean equals(Object o) {
    // if (o == this)
    // return true;
    // if (!(o instanceof Vehicle)) {
    // return false;
    // }
    // Vehicle vehicle = (Vehicle) o;
    // return vehicleId == vehicle.vehicleId && fleetId == vehicle.fleetId && isOn
    // == vehicle.isOn;
    // }

    // @Override
    // public int hashCode() {
    // return Objects.hash(vehicleId, fleetId, isOn);
    // }

    // @Override
    // public String toString() {
    // return "{" +
    // " vehicleId='" + getVehicleId() + "'" +
    // ", fleetId='" + getFleetId() + "'" +
    // ", isOn='" + isIsOn() + "'" +
    // "}";
    // }

}

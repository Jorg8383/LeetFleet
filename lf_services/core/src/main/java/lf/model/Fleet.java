package lf.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lf.core.VehicleIdRange;

/**
 * Basic (serialisable) representation of Fleet.
 */
public class Fleet {

    private String name;  // Name/description of the Fleet Manager
    private String managerId;  // Internal Fleet Manager Id

    @JsonIgnore
    private VehicleIdRange carelessFleetIdRange;

    @JsonIgnore
    private List<Vehicle> vehicles;

    public Fleet() {
    }

    public Fleet(String name, String managerId) {
        this.name = name;
        this.managerId = managerId;
    }

    //------------------------------------------------------------

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getManagerId() {
        return managerId;
    }
    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public VehicleIdRange getCarelessFleetIdRange() {
        return carelessFleetIdRange;
    }
    public void setCarelessFleetIdRange(VehicleIdRange carelessFleetIdRange) {
        this.carelessFleetIdRange = carelessFleetIdRange;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

}
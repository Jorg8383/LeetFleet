package lf.model;

public class Vehicle {

    enum LockStatuses {
        LOCKED,
        UNLOCKED
    }

    private String vehicleId;
    private String fleetId;

    private Float tyrePressure;
    private Float totalMileage;
    private Float oilLevel;
    private Long nextServiceMileage;
    private String doorStatus;
    private String maintenanceNeeded;

    public static Vehicle createForMileage(String vehicleId, String fleetId, Float totalMileage)
    {
        return new Vehicle(
            vehicleId, fleetId, new Float(0), totalMileage,
            new Float(0),  new Long(0), "", "");
    }

    public Vehicle(String vehicleId, String fleetId, Float tyrePressure, Float totalMileage, Float oilLevel,
            Long nextServiceMileage, String doorStatus, String maintenanceNeeded) {
        this.vehicleId = vehicleId;
        this.fleetId = fleetId;
        this.tyrePressure = tyrePressure;
        this.totalMileage = totalMileage;
        this.oilLevel = oilLevel;
        this.nextServiceMileage = nextServiceMileage;
        this.doorStatus = doorStatus;
        this.maintenanceNeeded = maintenanceNeeded;
    }

    public Vehicle() {
    }

    // private boolean isOn;

    public Vehicle(String vehicleId, String fleetId) {
        this.vehicleId = vehicleId;
        this.fleetId = fleetId;
        // this.isOn = isOn;
    }

    public String getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getFleetId() {
        return this.fleetId;
    }

    public void setFleetId(String fleetId) {
        this.fleetId = fleetId;
    }

    public Float getTyrePressure() {
        return tyrePressure;
    }

    public void setTyrePressure(Float tyrePressure) {
        this.tyrePressure = tyrePressure;
    }

    public Float getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(Float totalMileage) {
        this.totalMileage = totalMileage;
    }

    public Long getNextServiceMileage() {
        return nextServiceMileage;
    }

    public void setNextServiceMileage(Long nextServiceMileage) {
        this.nextServiceMileage = nextServiceMileage;
    }

    public String getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }

    public String getMaintenanceNeeded() {
        return maintenanceNeeded;
    }

    public void setMaintenanceNeeded(String maintenanceNeeded) {
        this.maintenanceNeeded = maintenanceNeeded;
    }

    public Float getoilLevel() {
        return this.oilLevel;
    }

    public void setOilLevel(Float oilLevel) {
        this.oilLevel = oilLevel;
    }
}
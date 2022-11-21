package lf.actor;






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
    private Long nextServiceMileage;
    private String doorStatus;
    private String maintenanceNeeded;

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
}
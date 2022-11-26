package lf.actor;

public class VehicleClass {
    public String vehicleId;
    public String fleetId;

    public VehicleClass() {
    }

    public VehicleClass(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public VehicleClass(String vehicleId, String fleetId) {
        this.vehicleId = vehicleId;
        this.fleetId = fleetId;
    }

    public void setFleetId(String id) {
        this.fleetId = id;
    }

}

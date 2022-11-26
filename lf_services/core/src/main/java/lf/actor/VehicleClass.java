package lf.actor;

import com.fasterxml.jackson.annotation.JsonCreator;

public class VehicleClass {
    public String vehicleId;
    public String fleetId;

//     @JsonCreator
//     public VehicleClass(@JsonProperty("vehicleId") String vehicleId, @JsonProperty("fleetId") String fleetId) {
//         this.vehicleId = vehicleId;
//         this.fleetId = fleetId;
//   }

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

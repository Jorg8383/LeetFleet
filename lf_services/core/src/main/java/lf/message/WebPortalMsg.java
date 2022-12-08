package lf.message;

import java.util.List;

import lf.model.Fleet;
import lf.model.Vehicle;

/**
 *
 */
public class WebPortalMsg {

  public interface Message {
  };

  public final static class VehicleToWebP implements Message, LFSerialisable {
    public Vehicle vehicle;

    public VehicleToWebP(Vehicle vehicle) {
      this.vehicle = vehicle;
    }
  }

  public final static class FleetListToWebP implements Message, LFSerialisable {
    public List<Fleet> fleets;

    public FleetListToWebP(List<Fleet> fleets) {
      this.fleets = fleets;
    }
  }

  public final static class VehicleListToWebP implements Message, LFSerialisable {
    public List<Vehicle> vehicles;

    public VehicleListToWebP(List<Vehicle> vehicles) {
      this.vehicles = vehicles;
    }
  }

}

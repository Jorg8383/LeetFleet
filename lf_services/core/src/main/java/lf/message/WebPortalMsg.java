package lf.message;

import lf.model.Vehicle;

/**
 *
 */
public class WebPortalMsg {

  public interface Message {
  };

  public final static class VehicleToWebP implements Message {
    public Vehicle vehicle;

    public VehicleToWebP(Vehicle vehicle) {
      this.vehicle = vehicle;
    }
  }

}

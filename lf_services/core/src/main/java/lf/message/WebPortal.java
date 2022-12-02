package lf.message;

import lf.model.Vehicle;

/**
 *
 */
public class WebPortal {

  public interface Message {
  };

  public final static class ResponseVehicleToWebPortal implements Message {
    public Vehicle vehicle;

    public ResponseVehicleToWebPortal(Vehicle vehicle) {
      this.vehicle = vehicle;
    }
  }

}

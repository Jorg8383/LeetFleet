package lf.actor;

import akka.actor.typed.ActorRef;
import lf.model.Vehicle;

/**
 *
 */
public class WebPortalMessages {

  public interface Message {
  };

  // public final static class FirstMessageToWebPortal implements Message {
  // public final String theProof;

  // public FirstMessageToWebPortal(String theProof) {
  // this.theProof = theProof;
  // }
  // }

  public final static class FirstMessageToWebPortal implements Message {
    public Vehicle vehicle;

    public FirstMessageToWebPortal(Vehicle vehicle) {
      this.vehicle = vehicle;
    }
  }

}

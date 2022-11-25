package lf.actor;

import akka.actor.typed.ActorRef;

public interface WebPortalInterface {

  public interface Message {
  };

  /*
   * A message from the Regsitry confirming successful registration
   */
  // TODO: The web portal is so important... do we want to take action if this
  // message does not arrive?
  public final static class RegWebPortalSuccess implements Message {
    public final ActorRef<Registry.Message> registryRef;

    public RegWebPortalSuccess(ActorRef<Registry.Message> registryRef) {
      this.registryRef = registryRef;
    }
  }

  public final static class FirstMessageToWebPortal implements Message {
    public final String theProof;
    public final ActorRef<VehicleEvent.Message> vehicleEventRef;

    public FirstMessageToWebPortal(String theProof, ActorRef<VehicleEvent.Message> vehicleEventRef) {
      this.theProof = theProof;
      this.vehicleEventRef = vehicleEventRef;
    }
  }

  public final static class NewVehicleEventActorRef implements Message {
    public final ActorRef<VehicleEvent.Message> vehicleEventRef;
    public NewVehicleEventActorRef(ActorRef<VehicleEvent.Message> vehicleEventRef) {
      this.vehicleEventRef = vehicleEventRef;
    }
  }

}

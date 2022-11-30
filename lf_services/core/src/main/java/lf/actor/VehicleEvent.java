package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.message.FleetManager;
import lf.model.Vehicle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class VehicleEvent extends AbstractBehavior<VehicleEvent.Message> {

  // MESSAGES:
  //
  public interface Message {
  }

  public final static class FleetManagerList implements Message {
    public final Collection<ActorRef<FleetManager.Message>> fleetManagerRefs;
    public final ActorRef<Registry.Message> registryRef;

    public FleetManagerList(Collection<ActorRef<FleetManager.Message>> fleetManagerRefs,
        ActorRef<Registry.Message> registryRef) {
      this.fleetManagerRefs = fleetManagerRefs;
      this.registryRef = registryRef;
    }
  }

  public final static class FleetManagerRef implements Message {
    public final ActorRef<FleetManager.Message> fleetManagerRef;
    public final ActorRef<Registry.Message> registryRef;

    public FleetManagerRef(ActorRef<FleetManager.Message> fleetManagerRef,
        ActorRef<Registry.Message> registryRef) {
      this.fleetManagerRef = fleetManagerRef;
      this.registryRef = registryRef;
    }
  }

  // public final static class FirstMessageFromWebPortal implements Message {
  // public final String theProof;
  // public final ActorRef<WebPortalMessages.FirstMessageToWebPortal> portalRef;

  // public FirstMessageFromWebPortal(String theProof,
  // ActorRef<WebPortalMessages.FirstMessageToWebPortal> portalRef) {
  // this.theProof = theProof;
  // this.portalRef = portalRef;
  // }
  // }

  public final static class FirstMessageFromWebPortal implements Message {
    public final Vehicle vehicle;
    public final ActorRef<WebPortalMessages.FirstMessageToWebPortal> portalRef;

    public FirstMessageFromWebPortal(
        Vehicle vehicle, ActorRef<WebPortalMessages.FirstMessageToWebPortal> portalRef) {
      this.vehicle = vehicle;
      this.vehicle.setFleetId("success lads");
      this.portalRef = portalRef;
    }
  }

  // ENCAPSULATION:

  // The web-portal actor gets a special, reserved ID.
  public static long fleetMgrId;
  public static ActorRef<FleetManager.Message> fleetMgrRef;
  public static String messageFromVehicle; // <<---- THIS IS THE MILLION DOLLAR QUESTION - WHAT IS THE PAYLOAD FROM THE
                                           // VEHICLE

  // CREATE THIS ACTOR
  public static Behavior<Message> create() {
    return Behaviors.setup(VehicleEvent::new);
  }

  // ADD TO CONTEXT
  private VehicleEvent(ActorContext<Message> context) {
    super(context);
    // constructor stuff here
  }

  // =========================================================================

  // MESSAGE HANDLING:

  // ???? cache a message when we get it ??
  // ??? do the discovery or query???/
  // ???? on receipt of the discovery or query - forward the message ???

  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(FleetManagerList.class, this::onFleetManagerList)
        .onMessage(FleetManagerRef.class, this::onFleetManagerRef)
        // .onMessage(FirstMessageFromWebPortal.class,
        // this::onFirstMessageFromWebPortal)
        .onMessage(
            FirstMessageFromWebPortal.class, this::onInitialVehicleMessage)

        .build();
  }

  // From WebPortal

  // GET A MESSAGE, STORE IT IN THE ????String messageFromVehicle????

  // From Registry

  private Behavior<Message> onFleetManagerList(FleetManagerList message) {
    // Store the all important ref to the portal
    Collection<ActorRef<FleetManager.Message>> fleetManagerRefs = message.fleetManagerRefs;

    // We don't know which fleet this vehicle belongs to, so forward this
    // (initial) communication to all managers. We Will expect a reply with
    // the appropriate fleetId for this vehicle in due course...
    for (ActorRef<FleetManager.Message> fleetManagerRef : fleetManagerRefs) {
      // fleetManagerRef.tell(new WHAT_WHAT_WHAT(, getContext().getSelf()));
      System.out.println("FIX ME FIX ME FIX ME");
    }
    return this;
  }

  private Behavior<Message> onFleetManagerRef(FleetManagerRef message) {
    // We now know the current actor ref for the fleet this vehicle belongs to,
    // so forward this vehicle communication to that managers.

    // CAN WE SHUTDOWN THIS ACTOR HERE???!?!?!?
    // message.fleetManagerRef.tell(new WHAT_WHAT_WHAT(, getContext().getSelf()));
    System.out.println("FIX ME FIX ME FIX ME");
    return this;
  }

  // private Behavior<Message>
  // onFirstMessageFromWebPortal(FirstMessageFromWebPortal message) {
  // message.portalRef.tell(new
  // WebPortalMessages.FirstMessageToWebPortal(message.theProof));

  // return this;
  // }

  private Behavior<Message> onInitialVehicleMessage(FirstMessageFromWebPortal message) {
    message.portalRef.tell(new WebPortalMessages.FirstMessageToWebPortal(message.vehicle));

    return this;
  }

}
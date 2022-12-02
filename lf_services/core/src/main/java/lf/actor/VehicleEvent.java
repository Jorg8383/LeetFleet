package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.actor.Registry.ListFleetManagers;
import lf.actor.Registry.QueryFleetManager;
import lf.message.FleetManager;
import lf.message.WebPortal;
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

  public final static class MessageFromWebPortal implements Message {
    public final Vehicle vehicle;
    public final ActorRef<WebPortal.ResponseVehicleToWebPortal> replyTo;
    public final ActorRef<Registry.Message> registryRef;

    public MessageFromWebPortal(
        Vehicle vehicle, ActorRef<WebPortal.ResponseVehicleToWebPortal> portalRef,
        ActorRef<Registry.Message> registryRef) {
      this.vehicle = vehicle;
      // this.vehicle.setFleetId("success lads");
      this.replyTo = portalRef;
      this.registryRef = registryRef;
    }
  }

  // ENCAPSULATION:

  // The web-portal actor gets a special, reserved ID.
  private ActorRef<WebPortal.Message> portalRef;
  private Vehicle vehicle;

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
        .onMessage(MessageFromWebPortal.class, this::onVehicleMessage)

        .build();
  }

  // From WebPortal

  /**
   * This first message, from the web-portal, is what we expect to receive just
   * after this actor has spawned.  We are a 'use once, then throw away' actor
   * that lives just long enough to handle a single request/response from the
   * outside world (well... from a WoT vehicle in the outside world). We expect
   * this message to contain:
   * <ul>
   *  <li>A Vehicle
   *    <ul>
   *      <li>This vehicle may or may not include a FleetManager Id</li>
   *    </ul>
   *  </li>
   *  <li>An actor ref for the web portal so we can respond once any work is done</li>
   * </ul>
   * @param message
   * @return
   */
  private Behavior<Message> onVehicleMessage(MessageFromWebPortal message) {
    // this thing gets in a message which contains a vehicle

    // STORE VEHCILE ATTRIBUTES IN THIS ACTOR

    // NEXt : IS THERE A FLEET ID!???
    if (message.vehicle.getFleetId() == "") {
      // If no - send a message to to the registry for all fleet managers
      message.registryRef.tell(new ListFleetManagers(this.getContext().getSelf()));
      message.vehicle.setFleetId("yololololo");
    } else {
      // IF yes - send a message to the registr to get the actore ref for that fleet
      // manager
      message.registryRef.tell(new QueryFleetManager(message.vehicle.getFleetId(), this.getContext().getSelf()));
      System.out.print("Fleet id exists");
    }

    // This actor should be able to recieve a message from the registry with a list
    // of fleet manager refs
    message.replyTo.tell(new WebPortal.ResponseVehicleToWebPortal(message.vehicle));
    return this;
  }

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



}
package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.actor.Registry.ListFleetMgrRefs;
import lf.message.FleetManagerMsg;
import lf.message.VehicleEventMsg;
import lf.message.VehicleEventMsg.Message;
import lf.message.WebPortalMsg;
import lf.model.Vehicle;

import java.util.*;

public class VehicleWotEvent extends AbstractBehavior<VehicleEventMsg.Message> {

  // MESSAGES:
  // All messages shared with VehicleWebEvent.

  // ENCAPSULATION:

  // The web-portal actor gets a special, reserved ID.
  private ActorRef<WebPortalMsg.VehicleToWebP> portalRef;
  private ActorRef<Registry.Message> registryRef;
  private Vehicle vehicle;

  // CREATE THIS ACTOR
  public static Behavior<Message> create() {
    return Behaviors.setup(VehicleWotEvent::new);
  }

  // ADD TO CONTEXT
  private VehicleWotEvent(ActorContext<Message> context) {
    super(context);
    // constructor stuff here
  }

  // =========================================================================

  // MESSAGE HANDLING:

  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
      .onMessage(VehicleEventMsg.EventFromWebP.class, this::onWotEventFromWebP)
      .onMessage(VehicleEventMsg.FleetMgrRefList.class, this::onFleetManagerList)
      .onMessage(VehicleEventMsg.EventComplete.class, this::onEventComplete)
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
  private Behavior<Message> onWotEventFromWebP(VehicleEventMsg.EventFromWebP message) {
    // this thing gets in a message which contains a vehicle

    // First, store the various Event attributes in this actor (for it's life)
    portalRef   = message.replyTo;
    registryRef = message.registryRef;
    vehicle     = message.vehicle;
    getContext().getLog().info("In event, just unmarshalled,  Fleet Manager ID is -> " + vehicle.getFleetId());

    // "vehicle.fleetId" might be null or empty or blank...
    // We don't care! We send off the fleetId 'as is' to the registry. If the
    // fleetId is valid we get back a list of 'one fleet manager'. If it's invalid
    // we should get back a list of all of them.
    registryRef.tell(new ListFleetMgrRefs(vehicle.getFleetId(), this.getContext().getSelf()));

    return this;
  }

  // From Registry

  /**
   * This second message, from the registry, we expect to receive so we can send
   * this event on to the correct fleet manager.  If we don't know the fleet manager
   * id then the registry will have returned a list of 'all fleet managers' and
   * we spam the lot of them with this event.
   *
   * @param message
   * @return
   */
  private Behavior<Message> onFleetManagerList(VehicleEventMsg.FleetMgrRefList message) {
    // Store the all important ref to the portal
    Collection<ActorRef<FleetManagerMsg.Message>> fleetManagerRefs = message.fleetManagerRefs;

    // We don't know which fleet this vehicle belongs to, so forward this
    // (initial) communication to all managers. We Will expect a reply with
    // the appropriate fleetId for this vehicle in due course...
    for (ActorRef<FleetManagerMsg.Message> fleetManagerRef : fleetManagerRefs) {
      fleetManagerRef.tell(new FleetManagerMsg.ProcessVehicleWotUpdate(vehicle, getContext().getSelf()));
    }
    return this;
  }

  private Behavior<Message> onEventComplete(VehicleEventMsg.EventComplete message) {
    // Everything is done!
    vehicle = message.vehicle;  // pedantry. keep state updated so future mods don't break.

    // Time to complete the promise this actor was created for...
    portalRef.tell(new WebPortalMsg.VehicleToWebP(vehicle));

    // ... and then shutdown.
    return Behaviors.stopped();
  }

}
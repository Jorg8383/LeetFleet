package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import lf.message.FleetManagerMsg;
import lf.message.LFSerialisable;
import lf.message.WebPortalMsg;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The VehicleWebQuery actor is similar to the Registry in many ways BUT
 * <ul>
 *  <li>The Registry doesn't track any messages/keep any 'request/response' state</li>
 *  <li>The Registry gets asked 'what are the Manager refs' and answers. Fin.</li>
 *  <li>The intention was that the Registry be scalable to 'every vehicle on the system'</li>
 *  <li>The VehicleWebQuery actor is a more conventional actor.</li>
 *  <li>It keeps a list of the messages it is currently handling so it can respond.</li>
 *  <li>The intention was that the VehicleWebQuery handle just web queries from Fleet Manager (users)</li>
 * </ul>
 * The VehicleWebQuery actor is used in LeetFleet for routing Vehicle web queries to
 * the correct FleetManager. It stores a reference to each message it handles and
 * if it fails any messages its handling are lost. These messages are read-only
 * queries.
 */
public class VehicleWebQuery extends AbstractBehavior<VehicleWebQuery.Message> {

  // Create a ServiceKey so we can find the VehicleWebQuery actor using the Receptioninst
  // The API of the receptionist is based on actor messages.
  public static final ServiceKey<VehicleWebQuery.Message> vehicleWebQuerySK
      = ServiceKey.create(VehicleWebQuery.Message.class, "vehicleWebQuerySK");

  public interface Message {
  }

 /**
  * Request for a list the registered fleet managers in JSON format.
  */
  public final static class ListVehiclesJson implements Message, LFSerialisable {
    public final long fleetManagerId;
    public final ActorRef<WebPortalMsg.VehicleListToWebP> portalRef;

    public ListVehiclesJson(
      @JsonProperty("fleetManagerId") long fleetManagerId,
      @JsonProperty("portalRef") ActorRef<WebPortalMsg.VehicleListToWebP> portalRef)
    {
      this.fleetManagerId = fleetManagerId;
      this.portalRef = portalRef;
    }
  }

  /**
  * Request for a list the registered fleet managers in JSON format.
  */
  public final static class UpdatedFleetManagerList implements Message, LFSerialisable {
    public final HashMap<Long, ActorRef<FleetManagerMsg.Message>> registry;

    public UpdatedFleetManagerList(@JsonProperty("registry") HashMap<Long, ActorRef<FleetManagerMsg.Message>> registry) {
      this.registry = registry;
    }
  }


  /**
   * Message to handle Listing Response from Receptionist.
   *
   * NOTE: We need to emply a 'messageAdaptor' to convert the message we recieve
   * from the Receptioninst to the one define here that we can understand.
   */
  private static class ListingResponse implements Message, LFSerialisable {
    final Receptionist.Listing listing;

    private ListingResponse(Receptionist.Listing listing) {
      this.listing = listing;
    }
  }

  // ENCAPSULATION:

  // Track which id's map to which 'FleetManager' (as the responses
  // can arrive in any order).
  private HashMap<Long, ActorRef<FleetManagerMsg.Message>> registryMirror;

  public ActorRef<Registry.Message> REGISTRY_REF = null;

  // We need an 'adaptor' - to convert the Receptionist Listing to one we
  // understand!!
  private final ActorRef<Receptionist.Listing> listingResponseAdapter;

  // CREATE THIS ACTOR
  public static Behavior<Message> create() {
    return Behaviors.setup(
        // Register this (key) actor with the receptionist
        context -> {
          context
              .getSystem()
              .receptionist()
              .tell(Receptionist.register(vehicleWebQuerySK, context.getSelf()));

          //return new VehicleWebQuery(context);
          return Behaviors.setup(VehicleWebQuery::new);
        });
  }

  // ADD TO CONTEXT
  private VehicleWebQuery(ActorContext<Message> context) {
    super(context);

    this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

    // Subscribe for Registry list updates!
    context
        .getSystem()
        .receptionist()
        .tell(
            Receptionist.subscribe(
                Registry.registrySK, listingResponseAdapter));
  }

  // =========================================================================

  // MESSAGE HANDLING:
  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
        //.onMessage(ListFleetMgrsJson.class, this::onListFleetMgrsJson)
        .onMessage(ListingResponse.class, this::onListing)
        .onMessage(UpdatedFleetManagerList.class, this::onUpdatedFleetManagerList)
        .onMessage(ListVehiclesJson.class, this::onListVehiclesJson)
        .build();
  }

  // From FleetManager

  // From VehicleEvent

  // private Behavior<Message> onListFleetMgrsJson(ListFleetMgrsJson message) {
  //   // Spoof method. In reality we would have modelled fleet managers. In this
  //   // toy system the four managers are just hard coded.  We could have put this
  //   // method anywhere - I put it here as "it's the closest thing to where fleet
  //   // managers are actually stored" in our demo system.
  //   ArrayList<Fleet> fleets = new ArrayList<Fleet>();
  //   fleets.add(new Fleet("CarelessFleet", "1"));
  //   fleets.add(new Fleet("FastidiousFleet", "2"));
  //   fleets.add(new Fleet("FleetleesFleet", "3"));
  //   fleets.add(new Fleet("ParanoidFleet", "4"));
  //   message.portalRef.tell(new WebPortalMsg.FleetListToWebP(fleets));

  //   return this;
  // }

  // From Receptionist

  private Behavior<Message> onListing(ListingResponse msg) {
    // The receptionist has messaged us, telling us the 'REGISTRY' list has changed.
    getContext().getLog().debug("Receptionist Notification (Registry List Update)");
    msg.listing.getServiceInstances(Registry.registrySK)
        .forEach(
            registryRef -> {
              REGISTRY_REF = registryRef;
            });

    // When we know the REGISTRY_REF (or it is updated) we subscribe to the
    // fleetManagerList is contains. Do a null check just in case we get sub'ed
    // to the list BEFORE the registry has registered.
    if (REGISTRY_REF != null) {
      REGISTRY_REF.tell(new Registry.SubToFleetMgrList(getContext().getSelf()));
    }

    return Behaviors.same();
  }

  private Behavior<Message> onUpdatedFleetManagerList(UpdatedFleetManagerList msg) {
    getContext().getLog().debug("Registry Notification (Registry FleetMgr List Update)");

    // Nothing smart... just update our copy of the 'id to ref' mapping.
    this.registryMirror = msg.registry;

    return Behaviors.same();
  }

  /**
   * For the received fleet manager id - get a list of all active vehicles in
   * Json format.
   * @param msg
   * @return
   */
  private Behavior<Message> onListVehiclesJson(ListVehiclesJson msg) {
    // Want a list of all vehicles for the selected managerId
    ActorRef<FleetManagerMsg.Message> managerRef = registryMirror.get(msg.fleetManagerId);
    // Do a null check just in case an invalidId was received.
    if (managerRef != null) {
      getContext().getLog().debug("In onListVehiclesJson : Valid managerRef retrieved");
      managerRef.tell(new FleetManagerMsg.ListVehiclesJson(msg.portalRef));
    }
    else {
      // Should we send an empty list with a message here!
      getContext().getLog().error("In onListVehiclesJson : No managerRef retrieved");
    }

    return Behaviors.same();
  }

}
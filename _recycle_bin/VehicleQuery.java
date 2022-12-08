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
import lf.message.VehicleEventMsg;
import lf.message.WebPortalMsg;
import lf.model.Fleet;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The VehicleQuery actor is similar to the Registry in many ways BUT
 * <ul>
 *  <li>The Registry doesn't track any messages/keep any 'request/response' state</li>
 *  <li>The Registry gets asked 'what are the Manager refs' and answers. Fin.</li>
 *  <li>The intention was that the Registry be scalable to 'every vehicle on the system'</li>
 *  <li>The VehicleQuery actor is a more conventional actor.</li>
 *  <li>It keeps a list of the messages it is currently handling so it can respond.</li>
 *  <li>The intention was that the VehicleQuery handle just web queries from Fleet Manager (users)</li>
 * </ul>
 * The VehicleQuery actor is used in LeetFleet for routing Vehicle web queries to
 * the correct FleetManager. It stores a reference to each message it handles and
 * if it fails any messages its handling are lost. These messages are read-only
 * queries.
 */
 public class VehicleQuery extends AbstractBehavior<Registry.Message> {

  // Create a ServiceKey so we can find the VehicleQuery actor using the Receptioninst
  // The API of the receptionist is based on actor messages.
  public static final ServiceKey<VehicleQuery.Message> vehicleQuerySK
      = ServiceKey.create(VehicleQuery.Message.class, "vehicleQuerySK");

  public interface Message {
  }

 /**
  * Request for a list the registered fleet managers in JSON format.
  */
  public final static class ListVehiclesJson implements Message, LFSerialisable {
    public final ActorRef<WebPortalMsg.FleetListToWebP> portalRef;

    public ListVehiclesJson(@JsonProperty("portalRef") ActorRef<WebPortalMsg.FleetListToWebP> portalRef) {
      this.portalRef = portalRef;
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

  private static long MESSAGE_ID = 10000;

  // Track which id's map to which 'FleetManager' (as the responses
  // can arrive in any order).
  private static HashMap<Long, ActorRef<FleetManagerMsg.Message>> registry = new HashMap<Long, ActorRef<FleetManagerMsg.Message>>();

  // We need an 'adaptor' - to convert the Receptionist Listing to one we
  // understand!!
  private final ActorRef<Receptionist.Listing> listingResponseAdapter;

  // CREATE THIS ACTOR
  public static Behavior<Message> create() {
    return Behaviors.setup(
        // Register this actor with the receptionist
        context -> {
          context
              .getSystem()
              .receptionist()
              .tell(Receptionist.register(vehicleQuerySK, context.getSelf()));

          //return new VehicleQuery(context);
          return Behaviors.setup(VehicleQuery::new);
        });
  }

  // ADD TO CONTEXT
  private VehicleQuery(ActorContext<Message> context) {
    super(context);

    this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

    // Subscribe for FleetManager list updates!
    context
        .getSystem()
        .receptionist()
        .tell(
            Receptionist.subscribe(
                FleetManagerMsg.fleetManagerServiceKey, listingResponseAdapter));
  }

  // =========================================================================

  // MESSAGE HANDLING:
  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(ListFleetMgrsJson.class, this::onListFleetMgrsJson)
        .onMessage(ListingResponse.class, this::onListing)
        .build();
  }

  // From FleetManager

  // From VehicleEvent

  private Behavior<Message> onListFleetMgrsJson(ListFleetMgrsJson message) {
    // Spoof method. In reality we would have modelled fleet managers. In this
    // toy system the four managers are just hard coded.  We could have put this
    // method anywhere - I put it here as "it's the closest thing to where fleet
    // managers are actually stored" in our demo system.
    ArrayList<Fleet> fleets = new ArrayList<Fleet>();
    fleets.add(new Fleet("CarelessFleet", "1"));
    fleets.add(new Fleet("FastidiousFleet", "2"));
    fleets.add(new Fleet("FleetleesFleet", "3"));
    fleets.add(new Fleet("ParanoidFleet", "4"));
    message.portalRef.tell(new WebPortalMsg.FleetListToWebP(fleets));

    return this;
  }

  // From Receptionist

  private Behavior<Message> onListing(ListingResponse msg) {
    getContext().getLog().info("Receptionist Notification - Fleet Manager Created:");
    registry = new HashMap<Long, ActorRef<FleetManagerMsg.Message>>();
    msg.listing.getServiceInstances(FleetManagerMsg.fleetManagerServiceKey)
        .forEach(
            fleetManagerRef -> {
              // Refresh entire registry every time?
              registry.put(SEED_ID++, fleetManagerRef);
              getContext().getLog().info("\t(fleet manager ref added to registry cache)");
            });
    return Behaviors.same();
  }

}
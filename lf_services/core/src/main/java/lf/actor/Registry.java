package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import lf.message.FleetManager;

import java.util.*;

// READ THESE FIRST:
// What is an Actor?
// https://doc.akka.io/docs/akka/current/general/actors.html#what-is-an-actor-
// https://doc.akka.io/docs/akka/current/typed/actors.html
//
// NOTE: Actors have an explicit lifecycle, they are not automatically destroyed
// when no longer referenced. After having created one, it is YOUR responsibility
// to make sure that it will eventually be terminated as well
// Messages are sent to an "actor Reference" and behind this façade there is a
// Behavior that receives the message and acts upon it.

/**
 *
 */
public class Registry extends AbstractBehavior<Registry.Message> {

  // Create a ServiceKey so we can find the Registry using the Receptioninst
  public static final ServiceKey<Registry.Message> registryServiceKey = ServiceKey.create(Registry.Message.class,
      "registryService");

  // MESSAGES:
  //
  // It is a good practice to put an actor’s associated messages as static
  // classes in the AbstractBehavior’s class. This makes it easier to understand
  // what type of messages the actor expects and handles.
  //
  // Typically, an actor handles more than one specific message type and then
  // there
  // is one common interface that all messages that the actor can handle
  // implements.
  public interface Message {
  }

  // Messages *are* the Actor’s public API, it is a good practice to define
  // messages with good
  // names and rich semantic and domain specific meaning, even if they just wrap
  // your data type.
  // This will make it easier to use, understand and debug actor-based system

  public final static class RegisterFleetManager implements Message {
    public final ActorRef<FleetManager.Message> fleetManRef;

    public RegisterFleetManager(ActorRef<FleetManager.Message> fleetManRef) {
      this.fleetManRef = fleetManRef;
    }
  }

  /* This message does not warrant a response - so no ActorRef stored */
  public final static class DeRegisterManager implements Message {
    public final long idToBeDeRegistered;

    public DeRegisterManager(long idToBeDeRegistered) {
      this.idToBeDeRegistered = idToBeDeRegistered;
    }
  }

  // I HAVE NO IDEA if a FleetManager will ever need the WebPortal reference
  // I just left this in as an example of how to get the WebPortal ActorRef.
  public final static class QueryWebPortal implements Message {
    public final ActorRef<FleetManager.Message> fleetManRef;

    public QueryWebPortal(ActorRef<FleetManager.Message> fleetManRef) {
      this.fleetManRef = fleetManRef;
    }
  }

  // DO WE REQUIRE SEPERATE DISCOVER AND LOOKUP METHODS? IF WE LOOKUP AND FIND
  // NOTHING WE COULD JUST....
  public final static class ListFleetManagers implements Message {
    public final ActorRef<VehicleEvent.Message> vehicleRef;

    public ListFleetManagers(ActorRef<VehicleEvent.Message> vehicleRef) {
      this.vehicleRef = vehicleRef;
    }
  }

  public final static class QueryFleetManager implements Message {
    public final String fleetId;
    public final ActorRef<VehicleEvent.Message> vehicleRef;

    public QueryFleetManager(String fleetId, ActorRef<VehicleEvent.Message> vehicleRef) {
      this.fleetId = fleetId;
      this.vehicleRef = vehicleRef;
    }
  }

  /**
   * Message to handle Listing Response from Receptionist.
   *
   * NOTE: We need to emply a 'messageAdaptor' to convert the message we recieve
   * from the Receptioninst to the one define here that we can understand.
   */
  private static class ListingResponse implements Message {
    final Receptionist.Listing listing;

    private ListingResponse(Receptionist.Listing listing) {
      this.listing = listing;
    }
  }

  // ENCAPSULATION:

  private static long SEED_ID = 10000;

  // Track which id's map to which 'ClientInfos' (as the responses
  // can arrive in any order).
  private static HashMap<Long, ActorRef<FleetManager.Message>> registry = new HashMap<Long, ActorRef<FleetManager.Message>>();

  // Not sure if I require a reference to the context - but will keep one as it's
  // in the example
  private final ActorContext<Message> context;
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
              .tell(Receptionist.register(registryServiceKey, context.getSelf()));

          return Behaviors.setup(Registry::new);
        });
  }

  // ADD TO CONTEXT
  private Registry(ActorContext<Message> context) {
    super(context);

    this.context = context;
    this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

    // Subscribe for FleetManager list updates!
    context
        .getSystem()
        .receptionist()
        .tell(
            Receptionist.subscribe(
                FleetManager.fleetManagerServiceKey, listingResponseAdapter));
  }

  // =========================================================================

  // MESSAGE HANDLING:
  @Override
  public Receive<Message> createReceive() {
    return newReceiveBuilder()
        .onMessage(RegisterFleetManager.class, this::onRegFleetManager)
        .onMessage(DeRegisterManager.class, this::onDeRegFleetManager)
        .onMessage(ListFleetManagers.class, this::onListFleetManagers)
        .onMessage(QueryFleetManager.class, this::onQueryFleetManager)
        .onMessage(ListingResponse.class, this::onListing)
        .build();
  }

  // From FleetManager

  // The type of the messages handled by this behavior is declared to be of class
  // message
  private Behavior<Message> onRegFleetManager(RegisterFleetManager message) {
    // The fleet manager has registered.
    long new_fleet_id = SEED_ID++;
    registry.put(new_fleet_id, message.fleetManRef);

    // We inform the FleetManager that registration was successful
    message.fleetManRef.tell(new FleetManager.RegistrationSuccess(new_fleet_id, getContext().getSelf()));
    return this;
  }

  // DeRegistration is part of orderly shutdown. We don't confirm.
  private Behavior<Message> onDeRegFleetManager(DeRegisterManager message) {
    registry.remove(message.idToBeDeRegistered);
    return this;
  }

  // From VehicleEvent

  private Behavior<Message> onListFleetManagers(ListFleetManagers message) {
    // Return the current state of the registry (all top tier fleet manager refs)
    message.vehicleRef.tell(new VehicleEvent.FleetManagerList(registry.values(), getContext().getSelf()));
    return this;
  }

  private Behavior<Message> onQueryFleetManager(QueryFleetManager message) {
    message.vehicleRef.tell(new VehicleEvent.FleetManagerRef(registry.get(message.fleetId), getContext().getSelf()));
    return this;
  }

  // From Receptionist

  private Behavior<Message> onListing(ListingResponse msg) {
    registry = new HashMap<Long, ActorRef<FleetManager.Message>>();
    msg.listing.getServiceInstances(FleetManager.fleetManagerServiceKey)
        .forEach(
            fleetManagerRef -> {
              // Refresh entire registry every time?
              getContext().getLog().info("IN REGISTRY GOT A MESSAGE FROM THE RECEPTIONISTT !!!");
              registry.put(SEED_ID++, fleetManagerRef);
              getContext().getLog().info("\tFLEET MANAGER REF SUCCESSFULLY ADDED TO REGISTRY !!!");
            });
    return Behaviors.same();
  }

}
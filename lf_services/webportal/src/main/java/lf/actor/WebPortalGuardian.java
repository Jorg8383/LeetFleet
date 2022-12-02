package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import jnr.ffi.annotations.IgnoreError;
import lf.model.Vehicle;

/**
 * The guardian is the top level actor that bootstraps the WebPortal application
 */
public class WebPortalGuardian extends AbstractBehavior<WebPortalGuardian.Message> {

    public interface Message {
    };

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
    //
    // Messages *are* the Actor’s public API, it is a good practice to define
    // messages with good
    // names and rich semantic and domain specific meaning, even if they just wrap
    // your data type.
    // This will make it easier to use, understand and debug actor-based system
    //
    // It is a good practice to put an actor’s associated messages as static
    // classes in the AbstractBehavior’s class. This makes it easier to understand
    // what type of messages the actor expects and handles.
    //
    // Messages *are* the Actor’s public API, it is a good practice to define
    // messages with good
    // names and rich semantic and domain specific meaning, even if they just wrap
    // your data type.
    // This will make it easier to use, understand and debug actor-based system
    public static class BootStrap implements Message {
        public final String note;

        public BootStrap(String note) {
            this.note = note;
        }
    }

    // public final static class ForwardToHandler implements Message {
    // public final String message;
    // public final ActorRef<WebPortalMessages.FirstMessageToWebPortal> replyTo;

    // public ForwardToHandler(String message,
    // ActorRef<WebPortalMessages.FirstMessageToWebPortal> replyTo) {
    // this.message = message;
    // this.replyTo = replyTo;
    // }
    // }

    // Definitely better ways to do this.
    public final static class ForwardToHandler implements Message {
        public final Vehicle message;
        public final ActorRef<WebPortalMessages.MessageToWebPortal> replyTo;

        public ForwardToHandler(
                Vehicle message, ActorRef<WebPortalMessages.MessageToWebPortal> replyTo) {
            this.message = message;
            this.replyTo = replyTo;
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
    @IgnoreError // Ignore unused error for registry - spawned but not used.
    public ActorRef<Registry.Message> REGISTRY_REF = null;
    // We need an 'adaptor' - to convert the Receptionist Listing to one we
    // understand!!
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;

    // =========================================================================

    // CREATE THIS ACTOR
    public static Behavior<WebPortalGuardian.Message> create() {
        return Behaviors.setup(WebPortalGuardian::new);
    }

    // ADD TO CONTEXT

    // In Akka you can’t create an instance of an Actor using the 'new' keyword.
    // Instead, you create Actor instances using factory spawn methods. Spawn
    // does not return an actor instance, but a reference
    // (akka.actor.typed.ActorRef)
    // that points to the actor instance
    private WebPortalGuardian(ActorContext<WebPortalGuardian.Message> context) {
        super(context);

        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

        // Ask about current state of Registry!
        context
                .getSystem()
                .receptionist()
                .tell(
                        Receptionist.find(
                                Registry.registryServiceKey, listingResponseAdapter));

        // Subscribe for Registry list updates!
        context
                .getSystem()
                .receptionist()
                .tell(
                        Receptionist.subscribe(
                                Registry.registryServiceKey, listingResponseAdapter));

    }

    // =========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<WebPortalGuardian.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(WebPortalGuardian.BootStrap.class, this::onBootStrap)
                // .onMessage(WebPortalGuardian.ForwardToHandler.class,
                // this::onForwardToHandler)
                .onMessage(WebPortalGuardian.ForwardToHandler.class, this::onForwardToHandler)
                .onMessage(ListingResponse.class, this::onListing)
                .build();
    }

    private Behavior<WebPortalGuardian.Message> onBootStrap(WebPortalGuardian.BootStrap message) {
        getContext().getLog().info("Starting WebPortalGuardian for {}!", message.note);
        // CODE DEMONSTRATING AN ACTOR SPAWNING ANOTHER ACTOR BASED ON RECEIPT OF A
        // MESSAGE
        // #create-actors
        // ActorRef<???.???> replyTo = getContext().spawn<???>create(3), command.name);
        // registry.tell(new ???.???(command.name, replyTo));
        // #create-actors
        return this;
    }

    // The type of the messages handled by this behavior is declared to be of class
    // message
    // private Behavior<WebPortalGuardian.Message>
    // onForwardToHandler(ForwardToHandler message) {
    // // Create a VehicleEvent actor to handle this request.
    // ActorRef<VehicleEvent.Message> vehicleEventRef =
    // getContext().spawn(VehicleEvent.create(), "Fred"); // <- TEMP
    // // TEMP TEMP
    // // TEMP TEMP
    // // - WE NEED
    // // TO INVENT
    // // A REAL
    // // NAMING
    // // CONVENTION!

    // // We inform the FleetManager that registration was successful
    // getContext().getLog().info("in onForwardToHandler, the message type is!{}!",
    // message.getClass());
    // vehicleEventRef.tell(new
    // VehicleEvent.FirstMessageFromWebPortal(message.message, message.replyTo));
    // return this;
    // }

    private Behavior<WebPortalGuardian.Message> onForwardToHandler(ForwardToHandler message) {
        // Create a VehicleEvent actor to handle this request.
        // Investigate use of context.spawnAnonymous(VehicleEvent.create()); - can we
        // drop unique names for throw-away actors???
        ActorRef<VehicleEvent.Message> vehicleEventRef = getContext().spawn(VehicleEvent.create(), "Fred"); // <- TEMP
                                                                                                            // TEMP TEMP
                                                                                                            // TEMP TEMP
                                                                                                            // - WE NEED
                                                                                                            // TO INVENT
                                                                                                            // A REAL
                                                                                                            // NAMING
                                                                                                            // CONVENTION!

        // We inform the FleetManager that registration was successful
        getContext().getLog().info("in onForwardToHandlerVehicle, the message type is!{}!", message.getClass());
        vehicleEventRef.tell(new VehicleEvent.MessageFromWebPortal(message.message, message.replyTo, REGISTRY_REF));
        return this;
    }

    // From Receptionist

    private Behavior<Message> onListing(ListingResponse msg) {
        // There will only every be one registry in the list in our toy akka system.
        msg.listing.getServiceInstances(Registry.registryServiceKey)
                .forEach(
                        registryRef -> {
                            // Refresh entire registry every time?
                            getContext().getLog().info("IN WEB_PORTAL GOT A MESSAGE FROM THE RECEPTIONISTT !!!");
                            REGISTRY_REF = registryRef;
                        });
        return Behaviors.same();
    }

}

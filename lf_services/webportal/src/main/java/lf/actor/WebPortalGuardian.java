package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import jnr.ffi.annotations.IgnoreError;
import lf.message.LFSerialisable;
import lf.message.WebPortalMsg;
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
    public static class BootStrap implements Message, LFSerialisable {
        public final String note;

        public BootStrap(String note) {
            this.note = note;
        }
    }

    // Definitely better ways to do this.
    public final static class ForwardToHandler implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public final ActorRef<WebPortalMsg.VehicleToWebP> replyTo;

        public ForwardToHandler(
                Vehicle vehicle, ActorRef<WebPortalMsg.VehicleToWebP> replyTo) {
            this.vehicle = vehicle;
            this.replyTo = replyTo;
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
    @IgnoreError // Ignore unused error for registry - spawned but not used.
    public ActorRef<Registry.Message> REGISTRY_REF = null;
    // We need an 'adaptor' - to convert the Receptionist Listing to one we
    // understand!!
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;

    // =========================================================================

    // CREATE THIS ACTOR
    public static Behavior<Message> create() {
        return Behaviors.setup(WebPortalGuardian::new);
    }

    // ADD TO CONTEXT

    // In Akka you can’t create an instance of an Actor using the 'new' keyword.
    // Instead, you create Actor instances using factory spawn methods. Spawn
    // does not return an actor instance, but a reference
    // (akka.actor.typed.ActorRef)
    // that points to the actor instance
    private WebPortalGuardian(ActorContext<Message> context) {
        super(context);

        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);

        // Ask about current state of Registry!
        context
            .getSystem()
            .receptionist()
            .tell(
                Receptionist.find(
                    Registry.registrySK, listingResponseAdapter));

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
                .onMessage(WebPortalGuardian.BootStrap.class, this::onBootStrap)
                // .onMessage(WebPortalGuardian.ForwardToHandler.class,
                // this::onForwardToHandler)
                .onMessage(WebPortalGuardian.ForwardToHandler.class, this::onForwardToHandler)
                .onMessage(ListingResponse.class, this::onListing)
                .build();
    }

    // From AKKA HTTP

    private Behavior<Message> onBootStrap(WebPortalGuardian.BootStrap message) {
        getContext().getLog().info("Starting WebPortalGuardian for {}!", message.note);
        // CODE DEMONSTRATING AN ACTOR SPAWNING ANOTHER ACTOR BASED ON RECEIPT OF A
        // MESSAGE
        // #create-actors
        // ActorRef<???.???> replyTo = getContext().spawn<???>create(3), command.name);
        // registry.tell(new ???.???(command.name, replyTo));
        // #create-actors
        return this;
    }

    // From AKKA HTTP (via Guardian)

    private Behavior<Message> onForwardToHandler(ForwardToHandler message) {
        // Create an (anonymous, disposable) VehicleEvent actor to handle this request.
        ActorRef<VehicleEvent.Message> vehicleEventRef = getContext().spawnAnonymous(VehicleEvent.create());  // 'anonymous' actor
        // ActorRef<VehicleEvent.Message> vehicleEventRef = getContext().spawn(VehicleEvent.create(), "Fred");  // 'normal' named actor

        // Pass the message details (from the HttpServer, via the WebGuardian) to the VehicleEvent actor
        // NOTE: We're forwarding the 'replyTo' reference of AKKA HTTP. The response to this message
        //       will be handled there (and not locally in this Guardian).
        //getContext().getLog().info("The message type is!{}!", message.getClass());
        vehicleEventRef.tell(new VehicleEvent.EventFromWebP(message.vehicle, message.replyTo, REGISTRY_REF));
        return this;
    }

    // From Receptionist

    private Behavior<Message> onListing(ListingResponse msg) {
        // There will only every be one registry in the list in our toy akka system.
        msg.listing.getServiceInstances(Registry.registrySK)
                .forEach(
                        registryRef -> {
                            // Refresh entire registry every time?
                            getContext().getLog().info("Success. Registry Reference from Receptionist complete.");
                            REGISTRY_REF = registryRef;
                        });
        return Behaviors.same();
    }

}

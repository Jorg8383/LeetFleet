package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import jnr.ffi.annotations.IgnoreError;
import lf.message.LFSerialisable;
import lf.message.VehicleEventMsg;
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
    public final static class ForwardToWotHandler implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public final ActorRef<WebPortalMsg.VehicleToWebP> replyTo;

        public ForwardToWotHandler(
                Vehicle vehicle, ActorRef<WebPortalMsg.VehicleToWebP> replyTo) {
            this.vehicle = vehicle;
            this.replyTo = replyTo;
        }
    }

    public final static class ForwardToWebHandler implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public final ActorRef<WebPortalMsg.VehicleToWebP> replyTo;

        public ForwardToWebHandler(
                Vehicle vehicle, ActorRef<WebPortalMsg.VehicleToWebP> replyTo) {
            this.vehicle = vehicle;
            this.replyTo = replyTo;
        }
    }

    public final static class WebListFleetJson implements Message, LFSerialisable {
        public final ActorRef<WebPortalMsg.FleetListToWebP> replyTo;

        public WebListFleetJson(ActorRef<WebPortalMsg.FleetListToWebP> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public final static class WebListVehicleJson implements Message, LFSerialisable {
        public final long managerId;
        public final ActorRef<WebPortalMsg.VehicleListToWebP> replyTo;

        public WebListVehicleJson(
                long managerId, ActorRef<WebPortalMsg.VehicleListToWebP> replyTo) {
            this.managerId = managerId;
            this.replyTo = replyTo;
        }
    }

    public final static class WebGetVehicleJson implements Message, LFSerialisable {
        public final long managerId;
        public final long vehicleId;
        public final ActorRef<WebPortalMsg.VehicleToWebP> replyTo;

        public WebGetVehicleJson(
                long managerId, long vehicleId, ActorRef<WebPortalMsg.VehicleToWebP> replyTo) {
            this.managerId = managerId;
            this.vehicleId = vehicleId;
            this.replyTo = replyTo;
        }
    }

    /**
     * Message to handle RegistryListing Response from Receptionist.
     *
     * NOTE: We need to emply a 'messageAdaptor' to convert the message we recieve
     * from the Receptioninst to the one define here that we can understand.
     */
    private static class RegistryListingResponse implements Message, LFSerialisable {
        final Receptionist.Listing listing;

        private RegistryListingResponse(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    /**
     * Message to handle VehicleQueryListing Response from Receptionist.
     *
     * NOTE: We need to emply a 'messageAdaptor' to convert the message we recieve
     * from the Receptioninst to the one define here that we can understand.
     */
    private static class VehicleQueryListingResponse implements Message, LFSerialisable {
        final Receptionist.Listing listing;

        private VehicleQueryListingResponse(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    // ENCAPSULATION:
    public ActorRef<Registry.Message> REGISTRY_REF = null;
    public ActorRef<VehicleQuery.Message> VEHICLE_QUERY_REF = null;

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

        // We need an 'adaptor' - to convert the Receptionist Registry Listing to one we understand!!
        ActorRef<Receptionist.Listing> registryListingResponseAdapter
            = context.messageAdapter(Receptionist.Listing.class, RegistryListingResponse::new);

        // Subscribe for Registry list updates (returns current set of entries upon subscription)
        context
            .getSystem()
            .receptionist()
            .tell(
                Receptionist.subscribe(
                    Registry.registrySK, registryListingResponseAdapter));

        //------------------------------

        // We need an 'adaptor' - to convert the Receptionist VehicleQuery Listing to one we understand!!
        ActorRef<Receptionist.Listing> vehicleQueryListingResponseAdapter
            = context.messageAdapter(Receptionist.Listing.class, VehicleQueryListingResponse::new);;

        // // Subscribe for VehicleQuery actor list updates (returns current set of entries upon subscription)
        // context
        //     .getSystem()
        //     .receptionist()
        //     .tell(
        //         Receptionist.subscribe(
        //             VehicleQuery.vehicleQuerySK, vehicleQueryListingResponseAdapter));

    }

    // =========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(WebPortalGuardian.BootStrap.class, this::onBootStrap)
                // .onMessage(WebPortalGuardian.ForwardToHandler.class,
                // this::onForwardToHandler)
                .onMessage(WebPortalGuardian.ForwardToWotHandler.class, this::onForwardWotToHandler)
                .onMessage(WebPortalGuardian.ForwardToWebHandler.class, this::onForwardToWebHandler)
                .onMessage(WebPortalGuardian.WebListFleetJson.class, this::onWebListFleetJson)
                .onMessage(WebPortalGuardian.WebListVehicleJson.class, this::onWebListVehicleJson)
                // .onMessage(WebPortalGuardian.WebGetVehicle.class, this::onWebGetVehicle)
                .onMessage(RegistryListingResponse.class, this::onRegistryListing)
                .onMessage(VehicleQueryListingResponse.class, this::onVehicleQueryListing)
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

    // From AKKA HTTP to WoT Handler (i.e. a vehicle update from WoT)

    private Behavior<Message> onForwardWotToHandler(ForwardToWotHandler message) {
        // Create an (anonymous, disposable) VehicleEvent actor to handle this request.
        ActorRef<VehicleEventMsg.Message> vehicleEventRef = getContext().spawnAnonymous(VehicleWotEvent.create());  // 'anonymous' actor
        // ActorRef<VehicleEvent.Message> vehicleEventRef = getContext().spawn(VehicleEvent.create(), "Fred");  // 'normal' named actor

        // Pass the message details (from the HttpServer, via the WebGuardian) to the VehicleEvent actor
        // NOTE: We're forwarding the 'replyTo' reference of AKKA HTTP. The response to this message
        //       will be handled there (and not locally in this Guardian).
        //getContext().getLog().info("The message type is!{}!", message.getClass());
        vehicleEventRef.tell(new VehicleEventMsg.EventFromWebP(message.vehicle, message.replyTo, REGISTRY_REF));
        return this;
    }

    // From AKKA HTTP to Web Handler (i.e. a vehicle update from Web Client)

    private Behavior<Message> onForwardToWebHandler(ForwardToWebHandler message) {
        // Create an (anonymous, disposable) VehicleEvent actor to handle this request.
        ActorRef<VehicleEventMsg.Message> vehicleEventRef = getContext().spawnAnonymous(VehicleWotEvent.create());  // 'anonymous' actor
        // ActorRef<VehicleEvent.Message> vehicleEventRef = getContext().spawn(VehicleEvent.create(), "Fred");  // 'normal' named actor

        // Pass the message details (from the HttpServer, via the WebGuardian) to the VehicleEvent actor
        // NOTE: We're forwarding the 'replyTo' reference of AKKA HTTP. The response to this message
        //       will be handled there (and not locally in this Guardian).
        //getContext().getLog().info("The message type is!{}!", message.getClass());
        vehicleEventRef.tell(new VehicleEventMsg.EventFromWebP(message.vehicle, message.replyTo, REGISTRY_REF));
        return this;
    }

    private Behavior<Message> onWebListFleetJson(WebListFleetJson message) {
        REGISTRY_REF.tell(new Registry.ListFleetMgrsJson(message.replyTo));
        return this;
    }

    private Behavior<Message> WebListVehicleJson(WebListVehicleJson message) {
        ActorRef<VehicleEventMsg.Message> vehicleEventRef = getContext().spawnAnonymous(VehicleWotEvent.create());  // 'anonymous' actor
        vehicleEventRef.tell(new VehicleEventMsg.EventFromWebP(message.managerId, message.replyTo));
        return this;
    }

    // private Behavior<Message> onWebGetVehicle(WebGetVehicle message) {
    //     ActorRef<VehicleEventMsg.Message> vehicleEventRef = getContext().spawnAnonymous(VehicleWotEvent.create());  // 'anonymous' actor
    //     vehicleEventRef.tell(new VehicleEventMsg.EventFromWebP(message, message.replyTo, REGISTRY_REF));
    //     return this;
    // }

    // From Receptionist

    private Behavior<Message> onRegistryListing(RegistryListingResponse msg) {
        // There will only every be one registry in the list in our toy akka system.
        msg.listing.getServiceInstances(Registry.registrySK)
                .forEach(
                        registryRef -> {
                            // Refresh entire registry every time?
                            getContext().getLog().info("Success. \"Registry\" Reference from Receptionist complete.");
                            REGISTRY_REF = registryRef;
                        });
        return Behaviors.same();
    }

    // private Behavior<Message> onVehicleQueryListing(VehicleQueryListingResponse msg) {
    //     // There will only every be one registry in the list in our toy akka system.
    //     msg.listing.getServiceInstances(VehicleQuery.vehicleQuerySK)
    //             .forEach(
    //                 vehicleQueryRef -> {
    //                     getContext().getLog().info("Success. \"VehicleQuery\" Reference from Receptionist complete.");
    //                     VEHICLE_QUERY_REF = vehicleQueryRef;
    //                 });
    //     return Behaviors.same();
    // }

}

package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import jnr.ffi.annotations.IgnoreError;

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

    public final static class RequestVehicleEventActor implements Message {
        public final ActorRef<WebPortalInterface.Message> replyTo;
        public RequestVehicleEventActor(ActorRef<WebPortalInterface.Message> replyTo) {
            this.replyTo = replyTo;
        }
    }

    // ENCAPSULATION:
    @IgnoreError // Ignore unused error for registry - spawned but not used.
    private final ActorRef<WebPortalGuardian.Message> webPortalGuardian;

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
        // #create-actors
        // Spawning - location transparency - this actor could be anywhere in the
        // cluster
        webPortalGuardian = context.spawn(WebPortalGuardian.create(), "webPortalGuardian");
        // #create-actors
    }

    // =========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<WebPortalGuardian.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(WebPortalGuardian.BootStrap.class, this::onBootStrap)
                .onMessage(WebPortalGuardian.BootStrap.class, this::onBootStrap)

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

    // The type of the messages handled by this behavior is declared to be of class message
    private Behavior<WebPortalGuardian.Message> onRegFleetManager(RequestVehicleEventActor message)
    {
        // Create
        ActorRef<VehicleEvent.Message> vehicleEventRef = getContext().spawn(VehicleEvent.create(), null);
        // We inform the FleetManager that registration was successful
        message.replyTo.tell(new FleetManager.RegistrationSuccess("Here ya go!", vehicleEventRef));
        return this;
    }

}

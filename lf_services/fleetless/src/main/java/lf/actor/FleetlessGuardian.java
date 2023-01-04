package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import jnr.ffi.annotations.IgnoreError;
import lf.message.FleetManagerMsg.Message;
import lf.message.LeetFServiceGuardianMsg.BootStrap;

/**
 * The guardian is the top level actor that bootstraps the Registry application
 */
public class FleetlessGuardian extends AbstractBehavior<BootStrap> {

    // MESSAGES:
    //
    // It is a good practice to put an actor’s associated messages as static
    // classes in the AbstractBehavior’s class.
    // For Service actors we make an exception - so startup logic can be shared.

    // ENCAPSULATION:
    @IgnoreError  // Ignore unused error for fleetlessFleetManager - spawned but not referenced.
    private final ActorRef<Message> fleetlessFleetManager;

    //=========================================================================

    // CREATE THIS ACTOR
    public static Behavior<BootStrap> create() {
        return Behaviors.setup(FleetlessGuardian::new);
    }

    // ADD TO CONTEXT

    // In Akka you can’t create an instance of an Actor using the 'new' keyword.
    // Instead, you create Actor instances using factory spawn methods. Spawn
    // does not return an actor instance, but a reference (akka.actor.typed.ActorRef)
    // that points to the actor instance
    private FleetlessGuardian(ActorContext<BootStrap> context) {
        super(context);
        //#create-actors
        // Spawning - location transparency - this actor could be anywhere in the cluster
        fleetlessFleetManager = context.spawn(FleetlessFleetManager.create(), "fleetlessFleetManager");
        //#create-actors
    }

    //=========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<BootStrap> createReceive() {
        return newReceiveBuilder()
            .onMessage(BootStrap.class, this::onBootStrap)
            .build();
    }

    private Behavior<BootStrap> onBootStrap(BootStrap message) {
        getContext().getLog().info("Starting FleetManager for {}!", message.note);
        return this;
    }
}

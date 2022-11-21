package lf.core;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

import lf.actor.Registry;

public abstract class FleetManager extends AbstractBehavior<FleetManager.Message> {

    // MESSAGES:
    //
    public interface Message {
    }

    public final static class NotifyWebPortal implements Message {
        public final ActorRef<Registry.Message> registryRef;

        public NotifyWebPortal(ActorRef<Registry.Message> registryRef) {
            this.registryRef = registryRef;
        }
    }

    // ENCAPSULATION:
    public long MANAGER_ID = 0;
    public ActorRef<Registry.Message> REGISTRY_REF;

    // CREATE THIS ACTOR
    // public static Behavior<Message> create() {
    // return Behaviors.setup(FleetManager::new);
    // }

    // ADD TO CONTEXT
    private FleetManager(ActorContext<Message> context) {
        super(context);
    }

    // =========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(NotifyWebPortal.class, this::onNotifyWebPortal)
                .build();
    }

    private Behavior<Message> onNotifyWebPortal(NotifyWebPortal message) {
        // Store the all important ref to the portal
        REGISTRY_REF = message.registryRef;
        return this;
    }

}

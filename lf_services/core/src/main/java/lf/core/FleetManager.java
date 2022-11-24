package lf.core;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

import lf.actor.Registry;
import lf.actor.WebPortalInterface;

public abstract class FleetManager extends AbstractBehavior<FleetManager.Message> {

    // MESSAGES:
    //
    public interface Message {
    }

    public final static class RegistrationSuccess implements Message {
        public final long mgrId;
        public final ActorRef<Registry.Message> registryRef;

        public RegistrationSuccess(long mgrId, ActorRef<Registry.Message> registryRef) {
            this.mgrId = mgrId;
            this.registryRef = registryRef;
        }
    }

    public final static class NotifyWebPortal implements Message {
        public final ActorRef<WebPortalInterface.Message> portalRef;
        public final ActorRef<Registry.Message> registryRef;

        public NotifyWebPortal(ActorRef<WebPortalInterface.Message> portalRef, ActorRef<Registry.Message> registryRef) {
            this.portalRef = portalRef;
            this.registryRef = registryRef;
        }
    }

    // ENCAPSULATION:
    public long MANAGER_ID = 0;
    public ActorRef<Registry.Message> REGISTRY_REF = null;

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
                .onMessage(RegistrationSuccess.class, this::onRegistrationSuccess)
                .onMessage(NotifyWebPortal.class, this::onNotifyWebPortal)
                .build();
    }

    private Behavior<Message> onRegistrationSuccess(RegistrationSuccess message) {
        // Store the unique id assigned to this FleetManager. We'll need it if
        // we want to 'DeRegister' on shutdown...
        MANAGER_ID = message.mgrId;
        return this;
    }

    private Behavior<Message> onNotifyWebPortal(NotifyWebPortal message) {
        // Store the refernece to the registry
        REGISTRY_REF = message.registryRef;
        return this;
    }

}

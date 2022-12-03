package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import lf.core.VehicleIdRange;
import lf.message.FleetManagerMsg;
import lf.message.FleetManagerMsg.Message;
import lf.message.FleetManagerMsg.RegistrationSuccess;

/**
 * A Fleet Manager. This time for the Notional Fastidious Fleet. Could suit any abstraction.
 */
public class FastidiousFleetManager extends AbstractBehavior<Message> {

    // ENCAPSULATION:
    public long MANAGER_ID = 2;
    private VehicleIdRange fastidiousFleetIdRange = new VehicleIdRange(2500, 4999);

    public ActorRef<Registry.Message> REGISTRY_REF = null;

    // CREATE THIS ACTOR
    public static Behavior<Message> create() {
        return Behaviors.setup(
            // Register this actor with the receptionist
            context -> {
                context
                    .getSystem()
                    .receptionist()
                    .tell(Receptionist.register(FleetManagerMsg.fleetManagerServiceKey, context.getSelf()));

                return Behaviors.setup(FastidiousFleetManager::new);
            }
        );
    }

    // ADD TO CONTEXT
    protected FastidiousFleetManager(ActorContext<Message> context) {
        super(context);
        // send a message to the registry to register!!!!  FFS

        // akka://my-sys@host.example.com:5678/user/service-b
        // registry.tell(new Registry.RegisterFleetManager(getContext().getSelf()));
    }

    // =========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(RegistrationSuccess.class, this::onRegistrationSuccess)
                .build();
    }

    private Behavior<Message> onRegistrationSuccess(RegistrationSuccess message) {
        // Store the unique id assigned to this FleetManager. We'll need it if
        // we want to 'DeRegister' on shutdown...
        MANAGER_ID   = message.mgrId;
        REGISTRY_REF = message.registryRef;
        getContext().getLog().info("FleetManager Registration Confirmed.");
        return this;
    }

}
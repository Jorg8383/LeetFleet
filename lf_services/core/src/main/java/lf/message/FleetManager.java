package lf.message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.ServiceKey;
import lf.actor.Registry;

/**
 * Messages understood by the LeetFleet Fleet Managers
 */
public class FleetManager {

    // Create a ServiceKey so we can find the Registry using the Receptioninst
    public static final ServiceKey<FleetManager.Message> fleetManagerServiceKey
            = ServiceKey.create(FleetManager.Message.class, "fleetManagerService");

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

}

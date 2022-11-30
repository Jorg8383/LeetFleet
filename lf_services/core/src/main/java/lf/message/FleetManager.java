package lf.message;

import akka.actor.typed.ActorRef;
import lf.actor.Registry;
import lf.actor.Registry.Message;

/**
 * Messages understood by the LeetFleet Fleet Managers
 */
public class FleetManager {

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

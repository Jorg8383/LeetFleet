package lf.message;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.ServiceKey;
import lf.actor.Registry;
import lf.actor.VehicleEvent;
import lf.model.Vehicle;

/**
 * Messages understood by the LeetFleet Fleet Managers
 */
public class FleetManagerMsg {

    // Create a ServiceKey so we can find the Registry using the Receptioninst
    public static final ServiceKey<FleetManagerMsg.Message> fleetManagerServiceKey
            = ServiceKey.create(FleetManagerMsg.Message.class, "fleetManagerService");

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

    /**
     * A VehicleUpdate has been sent to this fleet manager. Deal with it, but
     * remain conscious that there is a chance this vehicle is not in our fleet
     * (a broadcast message). If it is, update it and reply. If it isn't, just
     * ignore it.
     */
    public final static class ProcessVehicleUpdate implements Message {
        public final Vehicle vehicle;
        public final ActorRef<VehicleEvent.Message> vehicleEventRef;

        public ProcessVehicleUpdate(Vehicle vehicle, ActorRef<VehicleEvent.Message> vehicleEventRef) {
            this.vehicle = vehicle;
            this.vehicleEventRef = vehicleEventRef;
        }
    }

}

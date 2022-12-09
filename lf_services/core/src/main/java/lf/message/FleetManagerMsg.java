package lf.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.ServiceKey;
import lf.actor.Registry;
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

    public final static class RegistrationSuccess implements Message, LFSerialisable {
        public final long mgrId;
        public final ActorRef<Registry.Message> registryRef;

        public RegistrationSuccess(long mgrId, ActorRef<Registry.Message> registryRef) {
            this.mgrId = mgrId;
            this.registryRef = registryRef;
        }
    }

    /**
     * A VehicleUpdate has been sent to this fleet manager from a WoT 'Thing'.
     * Deal with it, but remain conscious that there is a chance this vehicle is
     * not in our fleet (a broadcast message). If it is, update it and reply. If
     * it isn't, just ignore it.
     */
    public final static class ProcessVehicleWotUpdate implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public final ActorRef<VehicleEventMsg.Message> vehicleWotEventRef;

        public ProcessVehicleWotUpdate(Vehicle vehicle, ActorRef<VehicleEventMsg.Message> vehicleWotEventRef) {
            this.vehicle = vehicle;
            this.vehicleWotEventRef = vehicleWotEventRef;
        }
    }

    /**
     * A VehicleUpdate has been sent to this fleet manager from a Web Client.
     * This is (possible) a request to change state on a WoT 'Thing'. We deal
     * with it, but remain conscious that there is a chance this vehicle is not
     * in our fleet (a broadcast message). If it is, update it and reply. If it
     * isn't, just ignore it.
     */
    public final static class ProcessVehicleWebUpdate implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public final ActorRef<VehicleEventMsg.Message> vehicleWebEventRef;

        public ProcessVehicleWebUpdate(Vehicle vehicle, ActorRef<VehicleEventMsg.Message> vehicleWebEventRef) {
            this.vehicle = vehicle;
            this.vehicleWebEventRef = vehicleWebEventRef;
        }
    }

    /**
     * Every fleet manager has a list of active vehicles. Receipt of this message
     * is a request for a list of those vehicles in JSON format.
     */
    public final static class ListVehiclesJson implements Message, LFSerialisable {
        public final ActorRef<WebPortalMsg.VehicleListToWebP> portalRef;

        public ListVehiclesJson(@JsonProperty("portalRef") ActorRef<WebPortalMsg.VehicleListToWebP> portalRef) {
            this.portalRef = portalRef;
        }
    }

}

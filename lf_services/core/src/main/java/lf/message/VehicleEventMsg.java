package lf.message;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import akka.actor.typed.ActorRef;
import lf.actor.Registry;
import lf.model.Vehicle;

/**
 * Messages understood by the LeetFleet Fleet Managers
 */
public class VehicleEventMsg {

    // MESSAGES:
    //
    public interface Message {
    }

    public final static class EventFromWebP implements Message, LFSerialisable {
      public final Vehicle vehicle;
      public final ActorRef<WebPortalMsg.VehicleToWebP> replyTo;
      public final ActorRef<Registry.Message> registryRef;

      public EventFromWebP(
          Vehicle vehicle, ActorRef<WebPortalMsg.VehicleToWebP> portalRef,
          ActorRef<Registry.Message> registryRef) {
        this.vehicle = vehicle;
        // this.vehicle.setFleetId("success lads");
        this.replyTo = portalRef;
        this.registryRef = registryRef;
      }
    }

    public final static class FleetMgrRefList implements Message, LFSerialisable {
      public final Collection<ActorRef<FleetManagerMsg.Message>> fleetManagerRefs;
      public final ActorRef<Registry.Message> registryRef;

      public FleetMgrRefList(Collection<ActorRef<FleetManagerMsg.Message>> fleetManagerRefs,
          ActorRef<Registry.Message> registryRef) {
        this.fleetManagerRefs = fleetManagerRefs;
        this.registryRef = registryRef;
      }
    }

    public final static class EventComplete implements Message, LFSerialisable {
      public final Vehicle vehicle;

      // All constructors (technically) should be annotated - but we appear to get
      // deserialisation errors for constructors with a single parameter:
      // (@see: https://doc.akka.io/docs/akka/current/serialization-jackson.html)
      public EventComplete(@JsonProperty("vehicle") Vehicle vehicle) {
        this.vehicle = vehicle;
      }
    }


}

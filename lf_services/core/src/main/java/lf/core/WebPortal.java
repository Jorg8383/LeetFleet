package lf.core;

import lf.actor.Registry;

import java.util.List;

import akka.actor.typed.ActorRef;

public interface WebPortal {

  public interface Message {}

    public final static class RegWebPortalSuccess implements Message {
        public final ActorRef<WebPortal.Message> registryRef;
        public RegWebPortalSuccess(ActorRef<WebPortal.Message> registryRef) {
          this.registryRef = registryRef;
        }
      }


      public final static class FleetManagerRefNotice implements Message {
        public final long fleetId;

        public final ActorRef<Registry.Message> registryRef;
        public NotifyFleetManager(List<ActorRef<FleetManager>> fleetMgrList, ActorRef<Registry.Message> registryRef) {
          this.fleetMgrList = fleetMgrList;
          this.registryRef = registryRef;
        }
      }

      public final static class NotifyDiscoveredList implements Message {
        public final List<ActorRef<FleetManager>> fleetMgrList
                              = new ArrayList<ActorRef<FleetManager>>();
        public final ActorRef<Registry.Message> registryRef;
        public NotifyDiscoveredList(List<ActorRef<FleetManager>> fleetMgrList, ActorRef<Registry.Message> registryRef) {
          this.fleetMgrList = fleetMgrList;
          this.registryRef = registryRef;
        }
      }

}

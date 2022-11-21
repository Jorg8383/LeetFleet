package lf.core;

import lf.actor.Registry;

public interface WebPortal {

    public final static class RegWebPortalSuccess implements Message {
        public final ActorRef<Registry.Message> registryRef;
        public RegWebPortalSuccess(ActorRef<Registry.Message> registryRef) {
          this.registryRef = registryRef;
        }
      }


      public final static class NotifyFleetManager implements Message {
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

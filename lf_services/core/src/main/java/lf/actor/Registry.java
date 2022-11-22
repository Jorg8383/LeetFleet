package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.core.FleetManager;
import lf.core.WebPortal;

import java.util.*;

public class Registry extends AbstractBehavior<Registry.Message>  {

    // MESSAGES:
    //
    // It is a good practice to put an actor’s associated messages as static
    // classes in the AbstractBehavior’s class. This makes it easier to understand
    // what type of messages the actor expects and handles.
    //
    // Typically, an actor handles more than one specific message type and then there
    // is one common interface that all messages that the actor can handle implements.
    public interface Message {}

    // Messages *are* the Actor’s public API, it is a good practice to define messages with good
    // names and rich semantic and domain specific meaning, even if they just wrap your data type.
    // This will make it easier to use, understand and debug actor-based system
    //
    // It is a good practice to put an actor’s associated messages as static
    // classes in the AbstractBehavior’s class. This makes it easier to understand
    // what type of messages the actor expects and handles.
    //
    // Messages *are* the Actor’s public API, it is a good practice to define messages with good
    // names and rich semantic and domain specific meaning, even if they just wrap your data type.
    // This will make it easier to use, understand and debug actor-based system
    public final static class RegisterWebPortal implements Message {
      public final ActorRef<WebPortal.Message> portalRef;
      public RegisterWebPortal(ActorRef<WebPortal.Message> portalRef) {
        this.portalRef = portalRef;
      }
    }

    public final static class RegisterFleetManager implements Message {
      public final ActorRef<FleetManager.Message> fleetManRef;
      public RegisterFleetManager(ActorRef<FleetManager.Message> fleetManRef) {
        this.fleetManRef = fleetManRef;
      }
    }

        /* This message does not warrant a response - so no ActorRef stored */
        public final static class DeRegisterManager implements Message {
          public final long idToBeDeRegistered;
          public DeRegisterManager(long idToBeDeRegistered) {
            this.idToBeDeRegistered = idToBeDeRegistered;
          }
        }

        // I HAVE NO IDEA if a FleetManager will ever need the WebPortal reference
        // I just left this in as an example of how to get the WebPortal ActorRef.
        public final static class QueryWebPortal implements Message {
      public final ActorRef<FleetManager.Message> fleetManRef;
      public QueryWebPortal(ActorRef<FleetManager.Message> fleetManRef) {
        this.fleetManRef = fleetManRef;
      }
    }

    // DO WE REQUIRE SEPERATE DISCOVER AND LOOKUP METHODS? IF WE LOOKUP AND FIND
    // NOTHING WE COULD JUST....
    public final static class ListFleetManagers implements Message {
      public final ActorRef<VehicleEvent.Message> vehicleRef;
      public ListFleetManagers(ActorRef<VehicleEvent.Message> vehicleRef) {
        this.vehicleRef = vehicleRef;
      }
    }
    public final static class QueryFleetManager implements Message {
      public final long fleetId;
      public final ActorRef<VehicleEvent.Message> vehicleRef;
      public QueryFleetManager(long fleetId, ActorRef<VehicleEvent.Message> vehicleRef) {
        this.fleetId = fleetId;
        this.vehicleRef = vehicleRef;
      }
    }
    // ENCAPSULATION:

    // The web-portal actor gets a special, reserved ID.
    public static long WEB_PORTAL_ID = 5000;
    private static ActorRef<WebPortal.Message> WEB_PORTAL_REF;

    private static long SEED_ID = 10000;

    // Track which id's map to which 'ClientInfos' (as the responses
    // can arrive in any order).
    private static HashMap<Long, ActorRef<FleetManager.Message>> registry
                        = new HashMap<Long, ActorRef<FleetManager.Message>>();

    // public final static class ThingyThing???? {
    //   public final String name;
    //   public final int age;
    //   public final String countryOfResidence;
    //   @JsonCreator
    //   public User(@JsonProperty("name") String name, @JsonProperty("age") int age, @JsonProperty("countryOfRecidence") String countryOfResidence) {
    //     this.name = name;
    //     this.age = age;
    //     this.countryOfResidence = countryOfResidence;
    //   }
    // }


    // CREATE THIS ACTOR
    public static Behavior<Message> create() {
      return Behaviors.setup(Registry::new);
    }

    // ADD TO CONTEXT
    private Registry(ActorContext<Message> context) {
      super(context);
      // constructor stuff here - like a call to register?  How to get registry ref!?!?!?!?
    }

    //=========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Message> createReceive() {
      return newReceiveBuilder()
          .onMessage(RegisterWebPortal.class, this::onRegWebPortal)
          .onMessage(RegisterFleetManager.class, this::onRegFleetManager)
          .onMessage(DeRegisterManager.class, this::onDeRegFleetManager)
          .onMessage(QueryWebPortal.class, this::onQueryWebPortal)
          .onMessage(ListFleetManagers.class, this::onListFleetManagers)
          .onMessage(QueryFleetManager.class, this::onQueryFleetManager)
          .build();
    }

    // From WebPortal

    private Behavior<Message> onRegWebPortal(RegisterWebPortal message) {
      // Store the all important ref to the portal
      WEB_PORTAL_REF = message.portalRef;

      message.portalRef.tell(new WebPortal.RegWebPortalSuccess(getContext().getSelf()));
      return this;
    }

    // From FleetManager

    // The type of the messages handled by this behavior is declared to be of class message
    private Behavior<Message> onRegFleetManager(RegisterFleetManager message)
    {
      // The fleet manager has registered.
      long new_fleet_id = SEED_ID++;
      registry.put(new_fleet_id, message.fleetManRef);

      // We inform the FleetManager that registration was successful
      message.fleetManRef.tell(new FleetManager.RegistrationSuccess(new_fleet_id, getContext().getSelf()));
      return this;
    }

    // DeRegistration is part of orderly shutdown. We don't confirm.
    private Behavior<Message> onDeRegFleetManager(DeRegisterManager message) {
      registry.remove(message.idToBeDeRegistered);
      return this;
    }

    private Behavior<Message> onQueryWebPortal(QueryWebPortal message) {
      message.fleetManRef.tell(new FleetManager.NotifyWebPortal(WEB_PORTAL_REF, getContext().getSelf()));
      return this;
    }

    // From VehicleEvent

    private Behavior<Message> onListFleetManagers(ListFleetManagers message) {
      // Return the current state of the registry (all top tier fleet manager refs)
      message.vehicleRef.tell(new VehicleEvent.FleetManagerList(registry.values(), getContext().getSelf()));
      return this;
    }

    private Behavior<Message> onQueryFleetManager(QueryFleetManager message) {
      message.vehicleRef.tell(new VehicleEvent.FleetManagerRef(registry.get(message.fleetId),  getContext().getSelf()));
      return this;
    }

  }
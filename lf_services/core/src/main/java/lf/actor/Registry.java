package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.core.FleetManager;
import lf.core.WebPortal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
      public final ActorRef<WebPortal.Message> regPortalRef;
      public final ActorRef<RegWebPortalSuccess> replyRef;
      public RegisterWebPortal(ActorRef<WebPortal> regPortalRef, ActorRef<RegWebPortalSuccess> replyRef) {
        this.regPortalRef = regPortalRef;
        this.replyRef     = replyRef;
      }
    }

    public final static class QueryWebPortal implements Message {
      public final ActorRef<FleetManager.Message> fleetManRef;
      public QueryWebPortal(ActorRef<FleetManager.Message> fleetManRef) {
        this.fleetManRef = fleetManRef;
      }
    }

    public final static class RegisterFleetManager implements Message {
      public final ActorRef<RegFleetManagerSuccess> fleetManRef;
      public RegisterFleetManager(ActorRef<RegFleetManagerSuccess> fleetManRef) {
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

    // DO WE REQUIRE SEPERATE DISCOVER AND LOOKUP METHODS? IF WE LOOKUP AND FIND
    // NOTHING WE COULD JUST....
    public final static class DiscoverFleetManager implements Message {
      public final ActorRef<NotifyDiscoveredList> webPortalRef;
      public DiscoverFleetManager(ActorRef<NotifyDiscoveredList> webPortalRef) {
        this.webPortalRef = webPortalRef;
      }
    }
    public final static class QueryFleetManager implements Message {
      public final long fleetId;
      public final ActorRef<FleetManager> replyTo;
      public Lookup(String name, ActorRef<GetUserResponse> replyTo) {
        this.name = name;
        this.replyTo = replyTo;
      }
    }
    // ENCAPSULATION:

    // The web-portal actor gets a special, reserved ID.
    public static long WEB_PORTAL_ID = 5000;
    private static ActorRef<Registry.Message> WEB_PORTAL_REF;

    private static long SEED_ID = 10000;

    // Track which id's map to which 'ClientInfos' (as the responses
    // can arrive in any order).
    private static HashMap<Long, ActorRef<FleetManager>> registry
                        = new HashMap<Long, ActorRef<FleetManager>>();

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
          .onMessage(QueryWebPortal.class, this::onQueryWebPortal)
          .onMessage(RegisterFleetManager.class, this::onRegFleetManager)
          .onMessage(DeRegisterManager.class, this::onDeRegFleetManager)
          .onMessage(DiscoverFleetManager.class, this::onDiscover)
          .onMessage(QueryFleetManager.class, this::onLookup)
          .build();
    }

    private Behavior<Message> onRegWebPortal(RegisterWebPortal message) {
      // Store the all important ref to the portal
      WEB_PORTAL_REF = message.portalRef;

      message.portalRef.tell(new RegWebPortalSuccess(getContext().getSelf()));
      return this;
    }

    private Behavior<Message> onQueryWebPortal(QueryWebPortal message) {
      users.removeIf(user -> user.name.equals(message.name));
      message.replyTo.tell(new FleetManager.NotifyWebPortal(getContext().getSelf()));
      return this;
    }

    // The type of the messages handled by this behavior is declared to be of class message
    private Behavior<Message> onRegFleetManager(RegisterFleetManager message) {
      // We must be careful not to send out users since it is mutable
      // so for this response we need to make a defensive copy
      message.fleetManRef.tell(new Users(Collections.unmodifiableList(new ArrayList<>(users))));
      return this;
    }

    private Behavior<Message> onDeRegFleetManager(DeRegisterManager message) {
      Optional<User> maybeUser = users.stream()
          .filter(user -> user.name.equals(message.name))
          .findFirst();
      message.replyTo.tell(new GetUserResponse(maybeUser));
      return this;
    }

    private Behavior<Message> onDiscover(DiscoverFleetManager message) {
      users.removeIf(user -> user.name.equals(message.name));
      message.replyTo.tell(new ActionPerformed(String.format("User %s deleted.", message.name)));
      return this;
    }

    private Behavior<Message> onLookup(QueryFleetManager message) {
      Optional<User> maybeUser = users.stream()
          .filter(user -> user.name.equals(message.name))
          .findFirst();
      message.replyTo.tell(new GetUserResponse(maybeUser));
      return this;
    }

  }
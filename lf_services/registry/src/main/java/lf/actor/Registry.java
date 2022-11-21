package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lf.core.AbstractFleetManager;
import lf.core.WebPortal;

import java.util.*;

public class Registry extends AbstractBehavior<Registry.Command>  {

    // MESSAGES:
    //
    // It is a good practice to put an actor’s associated messages as static
    // classes in the AbstractBehavior’s class. This makes it easier to understand
    // what type of messages the actor expects and handles.
    //
    // Typically, an actor handles more than one specific message type and then there
    // is one common interface that all messages that the actor can handle implements.
    interface Command {}

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
    public final static class RegisterWebPortal implements Command {
      public final ActorRef<RegWebPortalSuccess> portalRef;
      public RegisterWebPortal(ActorRef<RegWebPortalSuccess> portalRef) {
        this.portalRef = portalRef;
      }
    }

    public final static class RegWebPortalSuccess implements Command {
      public final ActorRef<Registry.Command> registryRef;
      public RegWebPortalSuccess(ActorRef<Registry.Command> registryRef) {
        this.registryRef = registryRef;
      }
    }

    public final static class GetWebPortal implements Command {
      public final ActorRef<AbstractFleetManager> fleetManRef;
      public GetWebPortal(ActorRef<AbstractFleetManager> fleetManRef) {
        this.fleetManRef = fleetManRef;
      }
    }

    public final static class RegisterFleetManager implements Command {
      public final ActorRef<AbstractFleetManager> fleetManRef;
      public RegisterFleetManager(ActorRef<AbstractFleetManager> fleetManRef) {
        this.fleetManRef = fleetManRef;
      }
    }

    public final static class DeRegisterManager implements Command {
      public final long idToBeDeRegistered;
      public final ActorRef<AbstractFleetManager> fleetManRef;
      public DeRegisterManager(long idToBeDeRegistered, ActorRef<AbstractFleetManager> fleetManRef) {
        this.idToBeDeRegistered = idToBeDeRegistered;
        this.fleetManRef = fleetManRef;
      }
    }

    public final static class Discover implements Command {
      public final String name;
      public final ActorRef<GetUserResponse> replyTo;
      public Discover(String name, ActorRef<GetUserResponse> replyTo) {
        this.name = name;
        this.replyTo = replyTo;
      }
    }

    public final static class Lookup implements Command {
      public final long fleetId;
      public final ActorRef<AbstractFleetManager> replyTo;
      public Lookup(String name, ActorRef<GetUserResponse> replyTo) {
        this.name = name;
        this.replyTo = replyTo;
      }
    }

    // ENCAPSULATION:

    // The web-portal actor gets a special, reserved ID.
    public static long WEB_PORTAL_ID = 5000;
    private static ActorRef<WebPortal> WEB_PORTAL_REF;

    private static long SEED_ID = 10000;

    // Track which id's map to which 'ClientInfos' (as the responses
    // can arrive in any order).
    private static HashMap<Long, ActorRef<AbstractFleetManager>> registry
                        = new HashMap<Long, ActorRef<AbstractFleetManager>>();

    public final static class ThingyThing???? {
      public final String name;
      public final int age;
      public final String countryOfResidence;
      @JsonCreator
      public User(@JsonProperty("name") String name, @JsonProperty("age") int age, @JsonProperty("countryOfRecidence") String countryOfResidence) {
        this.name = name;
        this.age = age;
        this.countryOfResidence = countryOfResidence;
      }
    }


    // CREATE THIS ACTOR
    public static Behavior<Command> create() {
      return Behaviors.setup(Registry::new);
    }

    // ADD TO CONTEXT
    private Registry(ActorContext<Command> context) {
      super(context);
    }

    //=========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Command> createReceive() {
      return newReceiveBuilder()
          .onMessage(RegisterWebPortal.class, this::onRegWebPortal)
          .onMessage(GetWebPortal.class, this::onGetWebPortal)
          .onMessage(RegisterFleetManager.class, this::onRegFleetManager)
          .onMessage(DeRegisterManager.class, this::onDeRegManager)
          .onMessage(Discover.class, this::onDiscover)
          .onMessage(Lookup.class, this::onLookup)
          .build();
    }

    private Behavior<Command> onRegWebPortal(RegisterWebPortal command) {
      // Store the all important ref to the portal
      WEB_PORTAL_REF = command.portalRef;

      command.portalRef.tell(new RegWebPortalSuccess(getContext().getSelf()));
      return this;
    }

    private Behavior<Command> onGetWebPortal(GetWebPortal command) {
      users.removeIf(user -> user.name.equals(command.name));
      command.replyTo.tell(new ActionPerformed(String.format("User %s deleted.", command.name)));
      return this;
    }

    // The type of the messages handled by this behavior is declared to be of class Command
    private Behavior<Command> onRegFleetManager(RegisterFleetManager command) {
      // We must be careful not to send out users since it is mutable
      // so for this response we need to make a defensive copy
      command.fleetManRef.tell(new Users(Collections.unmodifiableList(new ArrayList<>(users))));
      return this;
    }

    private Behavior<Command> onDeRegManager(DeRegister command) {
      Optional<User> maybeUser = users.stream()
          .filter(user -> user.name.equals(command.name))
          .findFirst();
      command.replyTo.tell(new GetUserResponse(maybeUser));
      return this;
    }

    private Behavior<Command> onDiscover(Discover command) {
      users.removeIf(user -> user.name.equals(command.name));
      command.replyTo.tell(new ActionPerformed(String.format("User %s deleted.", command.name)));
      return this;
    }

    private Behavior<Command> onLookup(Lookup command) {
      Optional<User> maybeUser = users.stream()
          .filter(user -> user.name.equals(command.name))
          .findFirst();
      command.replyTo.tell(new GetUserResponse(maybeUser));
      return this;
    }

  }
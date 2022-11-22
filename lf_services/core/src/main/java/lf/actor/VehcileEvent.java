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

public class VehicleEvent extends AbstractBehavior<VehicleEvent.Message>  {

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
      public final ActorRef<WebPortal> regPortalRef;
      public final ActorRef<RegWebPortalSuccess> replyRef;
      public RegisterWebPortal(ActorRef<WebPortal> regPortalRef, ActorRef<RegWebPortalSuccess> replyRef) {
        this.regPortalRef = regPortalRef;
        this.replyRef     = replyRef;
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
      return Behaviors.setup(VehicleEvent::new);
    }

    // ADD TO CONTEXT
    private VehicleEvent(ActorContext<Message> context) {
      super(context);
      // constructor stuff here - like a call to register?  How to get registry ref!?!?!?!?
    }

    //=========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Message> createReceive() {
      return newReceiveBuilder()
          .onMessage(RegisterWebPortal.class, this::onRegWebPortal)

          .build();
    }

    private Behavior<Message> onRegWebPortal(RegisterWebPortal message) {
      // Store the all important ref to the portal
      WEB_PORTAL_REF = message.portalRef;

      message.portalRef.tell(new RegWebPortalSuccess(getContext().getSelf()));
      return this;
    }


  }
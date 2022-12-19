package lf.actor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;

/**
 * Sample class to show:
 * <ul>
 *  <li>Starting a timer</li>
 *  <li>Behavior Switching</li>
 * </ul>
 * Please <strong>do not remove</strong>
 */
public class Buncher {

    public interface Command {}

    //------------------------------------------------------------

    public static final class Batch {
      private final List<Command> messages;

      public Batch(List<Command> messages) {
        this.messages = Collections.unmodifiableList(messages);
      }

      public List<Command> getMessages() {
        return messages;
      }
    }

    //------------------------------------------------------------

    // MESSAGES:

    public static final class ExcitingMessage implements Command {
      public final String message;

      public ExcitingMessage(String message) {
        this.message = message;
      }
    }

    // ENCAPSULATION?????

    private final TimerScheduler<Command> timers;
    private final ActorRef<Batch> target;
    private final Duration after;
    private final int maxSize;

    private static final Object TIMER_KEY = new Object();

    private enum Timeout implements Command {
      INSTANCE
    }

    // create a behaviour (using the Buncher constructor) that has timers...
    // WE WANT THIS!!!!
    public static Behavior<Command> create(ActorRef<Batch> target, Duration after, int maxSize) {
      return Behaviors.withTimers(
        timers -> new Buncher(timers, target, after, maxSize).idle()  // <- Easy to miss... the ".idle()" at the end!!
        );
    }

    // Csontructor for Buncher... takes the timer but doesn't call the 'context'
    // signature constructor from AbstractBehaviour to add us to the context...
    // ...so - how is this an actor?
    private Buncher(
        TimerScheduler<Command> timers, ActorRef<Batch> target, Duration after, int maxSize) {
      this.timers = timers;
      this.target = target;
      this.after = after;
      this.maxSize = maxSize;
    }

    //------------------------------------------------------------

    /**
     * This initial behavior is a one-command - sit and do nothing - type of
     * behavior. Once the first Command (any type) is received though we spring
     * into action and return a different behaviour for this actor!
     * @return
     */
     private Behavior<Command> idle() {
      return Behaviors.receive(Command.class)  // <- CHECK out the behavior definition we have never used - no context mentioned!! - direct receive
          .onMessage(Command.class, this::onIdleCommand)
          .build();
    }

    /**
     * A Command has been received while in 'idle' mode:
     * <ul>
     *  <li>Start a timer</li>
     *  <li>Return the new 'Active' behavior</li>
     * </ul>
     * @param message
     * @return
     */
    private Behavior<Command> onIdleCommand(Command message) {
      timers.startSingleTimer(TIMER_KEY, Timeout.INSTANCE, after);
      return Behaviors.setup(context -> new Active(context, message));
    }

    //------------------------------------------------------------

    /**
     * When in active mode our new behavior is as expected:
     * <ul>
     *  <li>Listen for Timeout or Command messages</li>
     *  <li>On timeout create a batch, send it and return to idle</li>
     *  <li>On command, add message to buffer, check buffer state:
     *    <ul>
     *     <li>If buffer full: cancel timer, send batch, return idle behavior</li>
     *     <li>If buffer not full: return (current) active behavior</li>
     *    </ul>
     *  </li>
     * </ul>
     */
    private class Active extends AbstractBehavior<Command> {

      private final List<Command> buffer = new ArrayList<>();

      protected Active(ActorContext<Command> context, Command firstCommand) {
        super(context);
        buffer.add(firstCommand);
      }

      @Override
      public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(Timeout.class, message -> onTimeout())
            .onMessage(Command.class, this::onCommand)
            .build();
      }

      private Behavior<Command> onTimeout() {
        target.tell(new Batch(buffer));
        return idle(); // switch to idle
      }

      private Behavior<Command> onCommand(Command message) {
        buffer.add(message);
        if (buffer.size() == maxSize) {
          timers.cancel(TIMER_KEY);
          target.tell(new Batch(buffer));
          return idle(); // switch to idle
        } else {
          return this; // stay Active
        }
      }
    }
  }
package lf.fleetman;

import akka.actor.typed.ActorSystem;
import lf.actor.FastidiousGuardian;
import lf.core.LeetFServiceStart;
import lf.message.LeetFServiceGuardian;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.typesafe.config.Config;

public class FastidiousStart extends LeetFServiceStart {
  private static final Logger log = LogManager.getLogger(FastidiousStart.class);

  public static void main(String[] args) {
    akkaHostname = "fastidious"; // Sensible defaults
    akkaPort     = 2553;

    configFromArgs(args);

    Config config = buildOverrideAkkaConfig();

    // An ActorSystem is the intial entry point into Akka.
    // Usually only one ActorSystem is created per application.
    // An ActorSystem has a name and a guardian actor.
    // The bootstrap of your application is typically done within the guardian actor.

    //#actor-system
    final ActorSystem<LeetFServiceGuardian.BootStrap> fastidiousGuardian
                  = ActorSystem.create(FastidiousGuardian.create(), "leet-fleet", config);
    //#actor-system

    //#main-send-messages
    fastidiousGuardian.tell(new LeetFServiceGuardian.BootStrap("Leet-Fleet"));
    //#main-send-messages

    gracefulInteractiveTermination(fastidiousGuardian);

  }

}
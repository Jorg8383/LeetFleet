package lf.fleetman;

import akka.actor.typed.ActorSystem;
import lf.actor.ParanoidGuardian;
import lf.core.LeetFServiceStart;
import lf.message.LeetFServiceGuardian;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.typesafe.config.Config;

public class ParanoidStart extends LeetFServiceStart {
  private static final Logger log = LogManager.getLogger(ParanoidStart.class);

  public static void main(String[] args) {
    akkaHostname = "paranoid"; // Sensible defaults
    akkaPort     = 2555;

    configFromArgs(args);

    Config config = buildOverrideAkkaConfig();

    // An ActorSystem is the intial entry point into Akka.
    // Usually only one ActorSystem is created per application.
    // An ActorSystem has a name and a guardian actor.
    // The bootstrap of your application is typically done within the guardian actor.

    //#actor-system
    final ActorSystem<LeetFServiceGuardian.BootStrap> paranoidGuardian
                  = ActorSystem.create(ParanoidGuardian.create(), "leet-fleet", config);
    //#actor-system

    //#main-send-messages
    paranoidGuardian.tell(new LeetFServiceGuardian.BootStrap("Leet-Fleet"));
    //#main-send-messages

    gracefulInteractiveTermination(paranoidGuardian);

  }

}
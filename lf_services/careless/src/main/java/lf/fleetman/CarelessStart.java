package lf.fleetman;

import akka.actor.typed.ActorSystem;
import lf.actor.CarelessGuardian;
import lf.core.LeetFServiceStart;
import lf.message.LeetFServiceGuardianMsg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.typesafe.config.Config;

public class CarelessStart extends LeetFServiceStart {
  private static final Logger log = LogManager.getLogger(CarelessStart.class);

  public static void main(String[] args) {
    akkaHostname = "careless"; // Sensible defaults
    akkaPort     = 2552;

    configFromArgs(args);

    Config config = buildOverrideAkkaConfig();

    // An ActorSystem is the intial entry point into Akka.
    // Usually only one ActorSystem is created per application.
    // An ActorSystem has a name and a guardian actor.
    // The bootstrap of your application is typically done within the guardian actor.

    //#actor-system
    final ActorSystem<LeetFServiceGuardianMsg.BootStrap> carelessGuardian
                  = ActorSystem.create(CarelessGuardian.create(), "leet-fleet", config);
    //#actor-system

    //#main-send-messages
    carelessGuardian.tell(new LeetFServiceGuardianMsg.BootStrap("Leet-Fleet"));
    //#main-send-messages

    gracefulInteractiveTermination(carelessGuardian);

  }

}
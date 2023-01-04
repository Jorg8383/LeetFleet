package lf.registry;

import akka.actor.typed.ActorSystem;
import lf.actor.RegistryGuardian;
import lf.core.LeetFServiceStart;
import lf.message.LeetFServiceGuardianMsg.BootStrap;

import com.typesafe.config.Config;

public class RegistryStart extends LeetFServiceStart {

  public static void main(String[] args) {
    akkaHostname = "registry"; // Sensible defaults
    akkaPort     = 2550;

    configFromArgs(args);

    Config config = buildOverrideAkkaConfig();

    // An ActorSystem is the intial entry point into Akka.
    // Usually only one ActorSystem is created per application.
    // An ActorSystem has a name and a guardian actor.
    // The bootstrap of your application is typically done within the guardian actor.

    //#actor-system
    final ActorSystem<BootStrap> registryGuardian
                  = ActorSystem.create(RegistryGuardian.create(), "leet-fleet", config);
    //#actor-system

    //#main-send-messages
    registryGuardian.tell(new BootStrap("Leet-Fleet"));
    //#main-send-messages

    gracefulInteractiveTermination(registryGuardian);

  }

}
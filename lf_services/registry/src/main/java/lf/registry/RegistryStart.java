package lf.registry;

import akka.actor.typed.ActorSystem;
import lf.actor.RegistryGuardian;

import java.io.IOException;
public class RegistryStart {
  public static void main(String[] args) {

    // An ActorSystem is the intial entry point into Akka.
    // Usually only one ActorSystem is created per application.
    // An ActorSystem has a name and a guardian actor.
    // The bootstrap of your application is typically done within the guardian actor.

    //#actor-system
    final ActorSystem<RegistryGuardian.BootStrap> registryGuardian
                  = ActorSystem.create(RegistryGuardian.create(), "leet-fleet");
    //#actor-system

    //#main-send-messages
    registryGuardian.tell(new RegistryGuardian.BootStrap("Leet-Fleet"));
    //#main-send-messages

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      registryGuardian.terminate();
    }
  }
}
package lf.core;

import lf.actor.Registry;

import akka.actor.typed.ActorRef;

public interface WebPortal {

  public interface Message {};

  /* A message from the Regsitry confirming successful registration
   */
  //TODO: The web portal is so important... do we want to take action if this message does not arrive?
  public final static class RegWebPortalSuccess implements Message {
    public final ActorRef<Registry.Message> registryRef;

    public RegWebPortalSuccess(ActorRef<Registry.Message> registryRef) {
      this.registryRef = registryRef;
    }
  }

}

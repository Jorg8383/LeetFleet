package lf.actor;

import akka.actor.typed.ActorRef;

/**
 *
 */
public class WebPortalMessages {

  public interface Message {
  };

  /*
   * A message from the Regsitry confirming successful registration
   */
  // TODO: The web portal is so important... do we want to take action if this
  // message does not arrive?
  public final static class RegWebPortalSuccess implements Message {
    public final ActorRef<Registry.Message> registryRef;

    public RegWebPortalSuccess(ActorRef<Registry.Message> registryRef) {
      this.registryRef = registryRef;
    }
  }

  public final static class FirstMessageToWebPortal implements Message {
    public final String theProof;
    public FirstMessageToWebPortal(String theProof) {
      this.theProof = theProof;
    }
  }

}

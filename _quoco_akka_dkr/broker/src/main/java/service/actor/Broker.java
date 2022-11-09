package service.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import scala.concurrent.duration.Duration;
import service.broker.Main;
import service.core.ClientInfo;
import service.message.ApplicationRequest;
import service.message.ApplicationResponse;
import service.message.QuotationRequest;
import service.message.ApplicationRequestDeadline;
import service.message.QuotationResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Broker extends AbstractActor {
    private static final Logger log = LogManager.getLogger(Broker.class);

    private List<ActorRef> quotServiceActorRefs = new ArrayList<ActorRef>();

    // We need to track which id's map to which 'ClientInfos' (as the responses
    // can arrive in any order).
    private static long SEED_ID = 0;
    private static Map<Long, ApplicationResponse> cache
        = new HashMap<Long, ApplicationResponse>();
    // Store the actor refs for the actors that submit ApplicationRequests
    // NOTES:
    //  -> In this demo system this will be the client all the time. But I wanted
    //     to be a little more general
    //  -> It would possibly be more convenient to create a new object to store
    //     both the senderRef and all quotations - but I didn't want to stray too
    //     far from the suggested structures for lab6
    private static Map<Long, ActorRef> applSenderActorRefs
        = new HashMap<Long, ActorRef>();

    // The Receive defines which messages the Actor can handle, along with the
    // implementation of how the messages should be processed.
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            // Use a simple string message to register the quotation service actors.
            .match(String.class,
                msg -> {
                    if (!msg.equals("register"))
                        return;
                    log.info("Registering quotation service: \"" + getSender().path().toString() + "\"");
                    quotServiceActorRefs.add(getSender());

                    // // Testing only:
                    // // Send a hard coded quotation request right back to the sender after registration
                    // getSender().tell(new QuotationRequest(1,
                    //     new ClientInfo("Niki Collier", ClientInfo.FEMALE, 43, 0, 5, "PQR254/1")),
                    //     this.getSelf());
                })
            .match(ApplicationRequest.class,
                msg -> {
                    ClientInfo info = msg.getClientInfo();
                    log.info("Broker: application request recieved for : " + info.getName());

                    // Store the received clientInfo in our cache. We'll use it later...
                    long appReqId = ++SEED_ID;
                    // Create an (empty) ApplicationResponse in the cache,
                    // ready to store the quotations as they arrive.
                    ApplicationResponse appResp =
                            new ApplicationResponse(info);
                    cache.put(new Long(appReqId), appResp);

                    // Store the sender ref for use when responding
                    // (yes... in this system this will always be 'client'...)
                    applSenderActorRefs.put(appReqId,getSender());

                    // Forward this quotation request to all the QuotationService actors...
                    for (ActorRef ref : quotServiceActorRefs) {
                        ref.tell(
                            new QuotationRequest(appReqId, msg.getClientInfo()), getSelf()
                        );
                    }

                    // Schdule a message, sent to ourselves, two seconds in the future!
                    // This message will trigger a response which will send all cached
                    // quotations back to the client.
                    getContext().system().scheduler().scheduleOnce(
                        Duration.create(Main.appReqDeadlineSecs, TimeUnit.SECONDS),
                        getSelf(),
                        new ApplicationRequestDeadline(appReqId),
                        getContext().dispatcher(), null);
                })
            .match(QuotationResponse.class,
                msg -> {
                    // What application does this deadline refer to?
                    Long applReqId = new Long(msg.getId());

                    // We have received a quotation response from one of the services.
                    // Add it to the appropriate response in the cache.
                    cache.get(applReqId).getQuotations().add(msg.getQuotation());
            })
            .match(ApplicationRequestDeadline.class,
                msg -> {
                    // What application does this deadline refer to?
                    Long applReqId = new Long(msg.getId());

                    // Grab the (hopefully populated) ApplicationResponse from
                    // the cache.
                    ApplicationResponse appResp = cache.get(applReqId);

                    // Look up the appropriate sender ref so we know where to send
                    // the message:
                    ActorRef applSenderRef = applSenderActorRefs.get(applReqId);

                    // Send the response back to 'whoever' (the client!) requested it...
                    applSenderRef.tell(appResp, getSelf());
                })
            .build();
    }
}
package service.actor;

import akka.actor.AbstractActor;
import service.core.Quotation;
import service.core.QuotationService;
import service.message.InitQuotationService;
import service.message.QuotationRequest;
import service.message.QuotationResponse;

public class Quoter extends AbstractActor {
    private QuotationService service;

    // The Receive defines which messages the Actor can handle, along with the
    // implementation of how the messages should be processed.
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(InitQuotationService.class,
                msg -> {
                    service = msg.getService();
                    // Don't have to send a reponse. But... perhaps introduce one
                    // to confirm to the supervising actor that Init was successful?
                    // getSender().tell(
                    //         new QuotationResponse(), getSelf());
                })
            .match(QuotationRequest.class,
                msg -> {
                    Quotation quotation = service.generateQuotation(msg.getClientInfo());
                    getSender().tell(
                            new QuotationResponse(msg.getId(), quotation), getSelf());
                })

            .build();
    }
}
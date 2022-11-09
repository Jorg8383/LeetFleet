package client.actor;

import java.text.NumberFormat;

import service.core.ClientInfo;
import service.core.Quotation;
import service.message.ApplicationRequest;
import service.message.ApplicationResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;

public class Client extends AbstractActor {
    private static final Logger log = LogManager.getLogger(Client.class);

    private ActorSelection brokerSelection = null;

    // The Receive defines which messages the Actor can handle, along with the
    // implementation of how the messages should be processed.
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            // Use a simple string message to initialise the client with the broker URL
            .match(String.class,
                msg -> {
                    if (!msg.startsWith("akka://"))
                        return;
                    log.info("Client: Initialising Client with Broker URI");

                    // We use an ActorSelection object here, as we saw in the quotation
                    // service examples, to define an actor that we want to interact with
                    // (the broker) when you don’t have its ActorRef object.

                    // This client takes the broker URI as part of it's init.
                    brokerSelection = context().actorSelection(msg);
                })
            .match(ClientInfo.class,
                msg -> {
                    log.info("Client: Issuing application requests for client \"" + msg.getName() + "\"");

                    // We use an ActorSelection object here, as we saw in the quotation
                    // service examples, to define an actor that we want to interact with
                    // (the broker) when you don’t have its ActorRef object.

                    ApplicationRequest applReq = new ApplicationRequest(msg);
                    brokerSelection.tell(applReq, getSelf());
                })
            .match(ApplicationResponse.class,
                msg -> {
                    displayProfile(msg.getClientInfo());
                    for (Quotation quotation : msg.getQuotations()) {
                        displayQuotation(quotation);
                    }
                })
            .build();
    }

    /**
     * Display the client info nicely.
     *
     * @param info
     */
    public static void displayProfile(ClientInfo info) {
        System.out.println("\n|=================================================================================================================|");
        System.out.println("|                                     |                                     |                                     |");
        System.out.println(
                "| Name: " + String.format("%1$-29s", info.getName()) +
                " | Gender: " + String.format("%1$-27s", (info.getGender()==ClientInfo.MALE?"Male":"Female")) +
                " | Age: " + String.format("%1$-30s", info.getAge())+" |");
        System.out.println(
                "| License Number: " + String.format("%1$-19s", info.getLicenseNumber()) +
                " | No Claims: " + String.format("%1$-24s", info.getNoClaims()+" years") +
                " | Penalty Points: " + String.format("%1$-19s", info.getPoints())+" |");
        System.out.println("|                                     |                                     |                                     |");
        System.out.println("|=================================================================================================================|");
    }

    /**
     * Display a quotation nicely - note that the assumption is that the quotation will follow
     * immediately after the profile (so the top of the quotation box is missing).
     *
     * @param quotation
     */
    public static void displayQuotation(Quotation quotation) {
        System.out.println(
                "| Company: " + String.format("%1$-26s", quotation.getCompany()) +
                " | Reference: " + String.format("%1$-24s", quotation.getReference()) +
                " | Price: " + String.format("%1$-28s", NumberFormat.getCurrencyInstance().format(quotation.getPrice()))+" |");
        System.out.println("|=================================================================================================================|");
    }

}
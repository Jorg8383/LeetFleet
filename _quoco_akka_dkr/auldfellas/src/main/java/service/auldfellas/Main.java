package service.auldfellas;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.actor.Quoter;
import service.core.QuotationHarnessConfig;
import service.message.InitQuotationService;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        QuotationHarnessConfig config = new QuotationHarnessConfig(args);

        ActorSystem system = ActorSystem.create();

        log.info("Creating AuldFellas Quotation Service Actor...");

        // Create a quotation service actor...
        ActorRef afQuoterRef = system.actorOf(Props.create(Quoter.class), "auldfellas");
        // .. and initialise it by sending it a message with an instance of our AFQService
        afQuoterRef.tell(new InitQuotationService(new AFQService()), null);

        // The ActorSelection object is used to define an actor that you want to
        // interact with when you don’t have its ActorRef object.
        ActorSelection selection = system.actorSelection(config.brokerPath);
        // Register our new quotation service actor with the broker...
        selection.tell("register", afQuoterRef);
    }

}

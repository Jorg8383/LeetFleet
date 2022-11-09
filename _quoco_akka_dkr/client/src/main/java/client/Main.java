package client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import client.actor.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.core.ClientInfo;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);

    // Default delay to allow quotation services to respond.
    public static String brokerHostName = null;

    public static void main(String[] args) {
        configFromArgs(args);

        ActorSystem system = ActorSystem.create();

        // Create the client actor...
        ActorRef clientRef = system.actorOf(Props.create(Client.class), "client");

        // First, we initialise the client with the URI to use for the broker.
        clientRef.tell("akka://default@"
            + (brokerHostName == null ? "localhost" : brokerHostName)
            + ":2551/user/broker", ActorRef.noSender());

        // Then ask the client to issue a quotation request for each of our clients...
        for (ClientInfo info : clients) {
            clientRef.tell(info, ActorRef.noSender());
        }
    }

    private static void configFromArgs(String[] args) {
        // Check the command line args for manual host/port configuration
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-b":
                    brokerHostName = args[++i];
                    break;
                default:
                    System.out.println("Unknown flag: " + args[i] + "\n");
                    System.out.println("Valid flags are:");
                    System.out.println("\t-b <Broker Hostname>\tSpecify the hostname of the broker");
                    System.exit(0);
            }
        }
        log.info("ClientConfig:: brokerHostName:" + brokerHostName);

        return;
    }

    /**
     * Test Data
     */
    public static final ClientInfo[] clients = {
        new ClientInfo("Niki Collier", ClientInfo.FEMALE, 43, 0, 5, "PQR254/1"),
        new ClientInfo("Old Geeza", ClientInfo.MALE, 65, 0, 2, "ABC123/4"),
        new ClientInfo("Hannah Montana", ClientInfo.FEMALE, 16, 10, 0, "HMA304/9"),
        new ClientInfo("Rem Collier", ClientInfo.MALE, 44, 5, 3, "COL123/3"),
        new ClientInfo("Jim Quinn", ClientInfo.MALE, 55, 4, 7, "QUN987/4"),
        new ClientInfo("Donald Duck", ClientInfo.MALE, 35, 5, 2, "XYZ567/9")
    };
}
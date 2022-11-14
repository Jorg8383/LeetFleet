// package lf.core;

// import akka.actor.ActorRef;
// import akka.actor.ActorSystem;
// import akka.http.javadsl.Http;
// import akka.http.javadsl.ServerBinding;
// import akka.http.javadsl.server.AllDirectives;
// import akka.http.javadsl.server.Route;
// import akka.actor.Props;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import lf.actor.HttpToAkka;
// import java.util.concurrent.CompletionStage;

// public class Main {
//     private static final Logger log = LogManager.getLogger(Main.class);

//     // Default delay to allow quotation services to respond.
//     public static int appReqDeadlineSecs = 2;

//     public static void main(String[] args) throws Exception {
//         ActorSystem system = ActorSystem.create("HttpToAkka");
//         ActorRef carActor = system.actorOf(CarActor.props(), "carActor");
//         CarServer server = new CarServer(carActor);
//         server.startServer("localhost", 8080, system);
//     }

    // Not sure what below does, So I'll leave in for now
//    private static void configFromArgs(String[] args) {
//        // Check the command line args for manual host/port configuration
//        for (int i = 0; i < args.length; i++) {
//            switch (args[i]) {
//                case "-t":
//                    appReqDeadlineSecs = Integer.parseInt(args[++i]);
//                    break;
//                default:
//                    System.out.println("Unknown flag: " + args[i] + "\n");
//                    System.out.println("Valid flags are:");
//                    System.out.println("\t-t <timeout>\tSpecify the time (in ms) the broker will wait for quotations");
//                    System.exit(0);
//            }
//        }
//        if (appReqDeadlineSecs == 0) {
//            log.warn("Broker 'Quotation Wait Timeout' Set to 0!!");
//            log.warn("Quotations WILL NOT arrive in time!");
//        }
//        log.info("BrokerConfig:: appReqDeadlineSecs:" + appReqDeadlineSecs);
//
//        return;
//    }
//}
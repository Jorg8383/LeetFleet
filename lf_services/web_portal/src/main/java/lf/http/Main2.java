// package lf.core;

// import akka.actor.ActorSystem;
// import akka.actor.Props;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import service.actor.Broker;

// public class Main {
// private static final Logger log = LogManager.getLogger(Main.class);

// // Default delay to allow quotation services to respond.
// public static int appReqDeadlineSecs = 2;

// public static void main(String[] args) {
// configFromArgs(args);

// ActorSystem system = ActorSystem.create();
// log.info("Creating Broker Actor...");
// system.actorOf(Props.create(Broker.class), "broker");
// }

// private static void configFromArgs(String[] args) {
// // Check the command line args for manual host/port configuration
// for (int i = 0; i < args.length; i++) {
// switch (args[i]) {
// case "-t":
// appReqDeadlineSecs = Integer.parseInt(args[++i]);
// break;
// default:
// System.out.println("Unknown flag: " + args[i] + "\n");
// System.out.println("Valid flags are:");
// System.out.println("\t-t <timeout>\tSpecify the time (in ms) the broker will
// wait for quotations");
// System.exit(0);
// }
// }
// if (appReqDeadlineSecs == 0) {
// log.warn("Broker 'Quotation Wait Timeout' Set to 0!!");
// log.warn("Quotations WILL NOT arrive in time!");
// }
// log.info("BrokerConfig:: appReqDeadlineSecs:" + appReqDeadlineSecs);

// return;
// }
// }
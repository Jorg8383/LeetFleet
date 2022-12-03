package lf.core;

import akka.actor.typed.ActorSystem;
import lf.message.LeetFServiceGuardianMsg;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Shared logic for Starting a LeetFleet Service System
 */
public abstract class LeetFServiceStart
{
  private static final Logger log = LogManager.getLogger(LeetFServiceStart.class);

  protected static String akkaHostname = "localhost"; // Sensible defaults
  protected static int akkaPort = 0;  // Typically defaulted in the Implementing Class

  // ------------------------------------------------------------

  /**
   * Parse args - populate required configuration
   **/
  protected static void configFromArgs(String[] args) {
    // Check the command line args for manual host/port configuration
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-a":
          akkaHostname = args[++i];
          break;
        case "-p":
          akkaPort = Integer.parseInt(args[++i]);
          break;
        default:
          System.out.println("Unknown flag: " + args[i] + "\n");
          System.out.println("Valid flags are:");
          System.out.println("\t-a <akkaHostname>\tSpecify the hostname of the where the akka service will run");
          System.out.println("\t-p <akkaPort>\tSpecify the port of the where the akka service will run");
          System.exit(0);
      }
      // WE SHOULD THROW AN ERROR IF THE HOSTNAME IS NOT IN ["core", "localhost"]
    }
    log.info("akkaHostname:" + akkaHostname);
    log.info("akkaPort:" + akkaPort);

    return;
  }

  /**
   *  From loaded configs - build typesafe config for akka
   **/
  protected static Config buildOverrideAkkaConfig() {
    // boot up server using the route as defined below
    // the default is to parse all application.conf, application.json and
    // application.properties found at the root of the class path
    Properties overrideProps = new Properties();
    overrideProps.setProperty("akka.remote.artery.canonical.hostname", akkaHostname);
    overrideProps.setProperty("akka.remote.artery.canonical.port", Integer.toString(akkaPort));
    Config overrideCfg = ConfigFactory.parseProperties(overrideProps);
    Config fullConfig = overrideCfg.withFallback(ConfigFactory.load());

    return fullConfig;
  }

  /**
   *  If in 'localhost' mode, on-terminal prompt and shutdown
   **/
  protected static void gracefulInteractiveTermination(ActorSystem<LeetFServiceGuardianMsg.BootStrap> serviceActor) {
    // Cater for interactive shutdown when running locally:
    if (akkaHostname.equals("localhost")) {
      try {
        System.out.println(">>> Press ENTER to exit <<<");
        System.in.read();
      } catch (IOException ignored) {
      } finally {
        //TODO: Implement an elegant termination when on docker network
        serviceActor.terminate();
      }
    }
  }
}
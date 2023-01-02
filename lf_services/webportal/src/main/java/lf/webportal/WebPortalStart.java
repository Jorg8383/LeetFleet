package lf.webportal;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import lf.actor.WebPortalGuardian;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class WebPortalStart {
  private static final Logger log = LogManager.getLogger(WebPortalStart.class);

  private static String akkaHostname = "localhost"; // Sensible defaults
  private static int akkaPort = 2551;
  private static String httpHostname = "localhost";
  private static int httpPort = 8080;
  private static String redisHostname = "localhost";
  @SuppressWarnings("unused")
  private static int redisPort = 6379;

  public static void main(String[] args) throws Exception {
    configFromArgs(args);

    // boot up server using the route as defined below
    // the default is to parse all application.conf, application.json and
    // application.properties found at the root of the class path
    Properties overrideProps = new Properties();
    overrideProps.setProperty("akka.remote.artery.canonical.hostname", akkaHostname);
    overrideProps.setProperty("akka.remote.artery.canonical.port", Integer.toString(akkaPort));
    Config overrideCfg = ConfigFactory.parseProperties(overrideProps);
    Config fullConfig = overrideCfg.withFallback(ConfigFactory.load());
    // ConfigFactory.load sandwiches customConfig between default reference
    // config and default overrides, and then resolves it.

    // #server-bootstrapping
    Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {

      ActorRef<WebPortalGuardian.Message> webPortalGuardianRef = context.spawn(WebPortalGuardian.create(),
          "WebPortalGuardian");
      // Send a message to the guardian just so we can see it start...
      webPortalGuardianRef.tell(new WebPortalGuardian.BootStrap("Leet-Fleet"));

      // Pass the ***Guardian*** Ref in to the constructor for the Routes class
      // This gives us full access to the Guardians actor context for this system.
      WebPortalRoutes vehicleEventRoutes = new WebPortalRoutes(context.getSystem(), webPortalGuardianRef);

      // Now start the server!
      startHttpServer(vehicleEventRoutes.vehicleEventRoutes(), context.getSystem());

      return Behaviors.empty();
    });
    // #server-bootstrapping

    // boot up server using the route as defined below
    //ActorSystem<NotUsed> system = ActorSystem.create(rootBehavior, "leet-fleet", fullConfig);
    ActorSystem.create(rootBehavior, "leet-fleet", fullConfig);

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      // webPortalGuardianRef.terminate();
    }
  }

  // ------------------------------------------------------------

  // #start-http-server
  static void startHttpServer(Route route, ActorSystem<?> system) {

    CompletionStage<ServerBinding> futureBinding
      = Http.get(system).newServerAt(httpHostname, httpPort).bind(route);

    futureBinding.whenComplete((binding, exception) -> {
      if (binding != null) {
        InetSocketAddress address = binding.localAddress();
        system.log().info("Server online at http://{}:{}/",
            address.getHostString(),
            address.getPort());
      } else {
        system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
        system.terminate();
      }
    });
  }
  // #start-http-server

  // ------------------------------------------------------------

  /* Parse args - populate required configuration */
  private static void configFromArgs(String[] args) {
    // Check the command line args for manual host/port configuration
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-a":
          akkaHostname = args[++i];
          break;
        case "-p":
          akkaPort = Integer.parseInt(args[++i]);
          break;
        case "-h":
          httpHostname = args[++i];
          break;
        case "-w":
          httpPort = Integer.parseInt(args[++i]);
          break;
        case "-r":
          redisHostname = args[++i];
          break;
        default:
          System.out.println("Unknown flag: " + args[i] + "\n");
          System.out.println("Valid flags are:");
          System.out.println("\t-a <akkaHostname>\tSpecify the hostname of the where the akka service will run");
          System.out.println("\t-p <akkaPort>\tSpecify the port of the where the akka service will run");
          System.out.println("\t-h <httpHostname>\tSpecify the hostname of the where the http server will run");
          System.out.println("\t-w <httpPort>\tSpecify the port of the where the http server will run");
          System.out.println("\t-r <redis-hostname>\tSpecify the hostname where the redis server will be found");
          System.exit(0);
      }
      // WE SHOULD THROW AN ERROR IF THE HOSTNAME IS NOT IN ["core", "localhost"]
    }
    log.info("akkaHostname:" + akkaHostname);
    log.info("akkaPort:" + akkaPort);
    log.info("httpHostname:" + httpHostname);
    log.info("httpPort:" + httpPort);
    log.info("redisHostname:" + redisHostname);

    return;
  }

}
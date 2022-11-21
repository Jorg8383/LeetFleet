package lf.http;

import akka.actor.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.remote.WireFormats;
import akka.util.Timeout;
import lf.core.WebPortal;
import lf.redisPojo.Vehicle;
//import org.apache.logging.log4j.core.appender.routing.Route;
import scala.concurrent.duration.Duration;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;

import java.util.Properties;
import java.util.concurrent.CompletionStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.providers.PooledConnectionProvider;

public class HttpToAkka extends AllDirectives implements WebPortal {
  private static final Logger log = LogManager.getLogger(HttpToAkka.class);

  private static String akkaHostname = "localhost";  // Sensible defaults
  private static int    akkaPort = 2550;
  private static String httpHostname = "localhost";
  private static int    httpPort = 8080;
  private static String redisHostname = "localhost";
  private static int    redisPort = 6379;

  public static void main(String[] args) throws Exception {
    configFromArgs(args);

    // boot up server using the route as defined below
    // the default is to parse all application.conf, application.json and application.properties found at the root of the class path
    Properties overrideProps = new Properties();
    overrideProps.setProperty("akka.remote.artery.canonical.hostname", akkaHostname);
    overrideProps.setProperty("akka.remote.artery.canonical.port",  Integer.toString(akkaPort));
    Config overrideCfg = ConfigFactory.parseProperties(overrideProps);
    Config fullConfig = overrideCfg.withFallback(ConfigFactory.load());
    // ConfigFactory.load sandwiches customConfig between default reference
    // config and default overrides, and then resolves it.
    //ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes", ConfigFactory.load(customConf));
    ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes", fullConfig);

    final Http http = Http.get(system);

    // In order to access all directives we need an instance where the routes are
    // define.
    HttpToAkka app = new HttpToAkka();

    final CompletionStage<ServerBinding> binding = http.newServerAt(httpHostname, httpPort)
        .bind(app.createRoute());

    System.out.println("Server online at http://" + httpHostname + ":" + httpPort + "/ on the Docker Network.\nPress RETURN to stop...");

    //==========================================================================
    //   REDIS TESTING
    //==========================================================================

    // Testing jedis connection - to be moved to vehicle actor
    Vehicle truck = new Vehicle("v1", "f1");

    // JedisPooled jedis = new JedisPooled("host.docker.internal", 6379);
    // Protocol.DEFAULT_HOST redisHostname
    HostAndPort config = new HostAndPort(redisHostname, redisPort);
    PooledConnectionProvider provider = new PooledConnectionProvider(config);
    UnifiedJedis client = new UnifiedJedis(provider);
    //JedisPool pool = new JedisPool("localhost", 6379);
      //jedis.set("clientName", "Jedis");
      client.jsonSetLegacy("vehicle:111", truck);
      // jedis..jsonSet("vehicle:111", truck);
      // log.info("client ->", client);
      // Object fred = jedis.jsonGet("111");
      // log.info("fred ->", fred);

    // System.out.println(client.jsonGet("vehicle:111"));
    // client.set("planets", "Venus");
    // System.out.println(client.get("planets"));

    client.close();
    //pool.close();

    // *** WE NEED TO FIGURE OUT HOW TO SHUT IT DOWN - EASIEST WAY (INSECURE!!) IS
    // WITH URL
    // System.in.read(); // let it run until user presses return

    // binding
    // .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
    // .thenAccept(unbound -> system.terminate()); // and shutdown when done
  }

  private Route createRoute() {
    // Akka-Http is indeed mind bending:
    // This page contains some useful examples (in comparisons to another framework)
    return concat(
        // Java Lambda: the -> separates the parameters (left-side) from the implementation (right side).
        path("hello", () ->
          get(() -> complete("<h1>Say hello to akka-http</h1>")))
    );
        // get(() ->
        //     path(segment("client").slash(longSegment()), id ->
        //             complete(Clients.get(id))
        //       )
        //     ),
        // get(() ->
        //   parameter("page", page ->
        //           complete(getPage(page))
        //     )
        //   ),
        //   // The parameterOptional directive passes the parameter as Optional<String>.
        //   // The directive parameterRequiredValue makes the route match only if the parameter contains the specified value.
        // get(() ->
        //     path(segment("api").slash("list"), () ->
        //             parameterOptional("version", version ->
        //                     complete(apiList(version)))
        //       )
        //   ),
        // get(() ->
        //   path(segment("api").slash("list-items"), () ->
        //           parameterList("item", items ->
        //                   complete(apiItems(items)))
        //   )
        // The parameterList directive may take a parameter name to specify a single parameter name to pass on as a List<String>.]
            // get(() ->
            //   pathPrefix("item", () ->
            //     path(longSegment(), (Long id) -> {
            //       final CompletionStage<Optional<Item>> futureMaybeItem = fetchItem(id);
            //       return onSuccess(futureMaybeItem, maybeItem ->
            //         maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
            //           .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
            //       );
            //     }))),
            // post(() ->
            //   // In Akka HTTP every path segment is specified as a separate String concatenated by the slash method on segment.
            //   //path(segment("create-order").slash("now"), () ->
            //   path("create-order", () ->
            //     entity(Jackson.unmarshaller(Order.class), order -> {
            //       CompletionStage<Done> futureSaved = saveOrder(order);
            //       return onSuccess(futureSaved, done ->
            //         complete("order created")
            //       );
            //     })))
  }

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
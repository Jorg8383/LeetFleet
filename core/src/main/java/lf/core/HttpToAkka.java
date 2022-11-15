package lf.core;

import akka.actor.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.remote.WireFormats;
import akka.util.Timeout;
//import org.apache.logging.log4j.core.appender.routing.Route;
import scala.concurrent.duration.Duration;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import java.util.concurrent.CompletionStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lf.core.Vehicle;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.providers.PooledConnectionProvider;

public class HttpToAkka extends AllDirectives {
  private static final Logger log = LogManager.getLogger(HttpToAkka.class);

  private static String hostname = "localhost";  // Sensible defaults
  private static int port = 8080;

  public static void main(String[] args) throws Exception {
    configFromArgs(args);

    // boot up server using the route as defined below
    ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes");

    final Http http = Http.get(system);

    // In order to access all directives we need an instance where the routes are
    // define.
    HttpToAkka app = new HttpToAkka();

    final CompletionStage<ServerBinding> binding = http.newServerAt(hostname, port)
        .bind(app.createRoute());

    System.out.println("Server online at http://" + hostname + ":" + port + "/ on the Docker Network.\nPress RETURN to stop...");

    // Testing jedis connection - to be moved to vehicle actor
    Vehicle truck = new Vehicle("1", "1");

    // JedisPooled jedis = new JedisPooled("host.docker.internal", 6379);
    HostAndPort config = new HostAndPort(hostname, 6379);
    PooledConnectionProvider provider = new PooledConnectionProvider(config);
    UnifiedJedis client = new UnifiedJedis(provider);

    client.jsonSet("vehicle:111", truck);
    log.info("client ->", client);
    Object fred = client.jsonGet("vehicle:111");
    log.info("fred ->", fred);

    // System.out.println(client.jsonGet("vehicle:111"));

    client.set("planets", "Venus");
    System.out.println(client.get("planets"));

    client.close();

    // *** WE NEED TO FIGURE OUT HOW TO SHUT IT DOWN - EASIEST WAY (INSECURE!!) IS
    // WITH URL
    // System.in.read(); // let it run until user presses return

    // binding
    // .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
    // .thenAccept(unbound -> system.terminate()); // and shutdown when done
  }

  private Route createRoute() {
    return concat(
        path("hello", () ->

        get(() -> complete("<h1>Say hello to akka-http</h1>"))));
  }

  /* Parse args - populate required configuration */
  private static void configFromArgs(String[] args) {
    // Check the command line args for manual host/port configuration
    for (int i = 0; i < args.length; i++) {
        switch (args[i]) {
            case "-h":
                hostname = args[++i];
                break;
            case "-p":
                port = Integer.parseInt(args[++i]);
                break;
            default:
                System.out.println("Unknown flag: " + args[i] + "\n");
                System.out.println("Valid flags are:");
                System.out.println("\t-b <Broker Hostname>\tSpecify the hostname of the broker");
                System.exit(0);
        }
    }
    log.info("hostname:" + hostname);
    log.info("port:" + port);

    return;
}
}
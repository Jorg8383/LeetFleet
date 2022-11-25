package lf.http;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.Behaviors;
import lf.actor.VehicleEvent;
import lf.core.WebPortal;
//import org.apache.logging.log4j.core.appender.routing.Route;
import java.time.Duration;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;

import java.util.Properties;
import java.util.concurrent.CompletionStage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static akka.actor.typed.javadsl.AskPattern.ask;

public class HttpToAkka extends AllDirectives implements WebPortal {
  private static final Logger log = LogManager.getLogger(HttpToAkka.class);

  private static String akkaHostname = "localhost";  // Sensible defaults
  private static int    akkaPort = 2551;
  private static String httpHostname = "localhost";
  private static int    httpPort = 8080;
  private static String redisHostname = "localhost";
  private static int    redisPort = 6379;

  private static Duration askTimeout;
  private static Scheduler scheduler;

  public static void main(String[] args) throws Exception
  {
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
    ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "leet-fleet", fullConfig);

    // Pick up the timeout from the config file...
    askTimeout = system.settings().config().getDuration("akka.routes.ask-timeout");
    scheduler = system.scheduler();

    final Http http = Http.get(system);

    // In order to access all directives we need an instance where the routes are
    // defines.
    HttpToAkka app = new HttpToAkka();

    final CompletionStage<ServerBinding> binding = http.newServerAt(httpHostname, httpPort)
        .bind(app.createRoute());

    // *** WE NEED TO FIGURE OUT HOW TO SHUT IT DOWN - EASIEST WAY (INSECURE!!) IS
    // WITH URL
    System.out.println("Server online at http://" + httpHostname + ":" + httpPort + "/ on the Docker Network.\nPress RETURN to stop...");
    System.in.read(); // let it run until user presses return

    binding
      .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
      .thenAccept(unbound -> system.terminate()); // and shutdown when done
  }

  private CompletionStage<WebPortal.FirstMessageToWebPortal> tomsTestMethod() {
    String proofOfLife = "One small step for one man...";

    // ***** CANT DO THIS _ IT CREATES A WHOLE NEW ACTOR SYSTEM FROM CONFIG
    // First we spawn a new VehicleEvent actor...
    // NOTES: The context is "The actor context" - the view of the actor 'cell' from the actor.
    // It exposes contextual information for the actor and the current message.
    ActorRef<VehicleEvent.Message> vehicleEventActor
      = ActorSystem.create(VehicleEvent.create(), "fred");

    return ask(vehicleEventActor, ref -> new VehicleEvent.FirstMessageFromWebPortal(proofOfLife, ref), askTimeout, scheduler);
  }


  // We describe HTTP “routes” and how they should be handled.
  // Each route is composed of one or more level of Directives that narrows down to handling "one specific type of request"
  //
  // One route might start with matching the path of the request, only matching if it is “/hello”
  // ...then narrowing it down to only handle HTTP get requests
  // ...then complete those with a string literal, which will be sent back as an HTTP OK with the string as response body.
  //
  // Another could start with the 'get' and then add on bits of path.
  // It's completely up to you - akka doesn't care how you define the routes.
  // You can define many routes by putting them all in a list and 'concat'ing them together.
  //
  // Another could start with 'multiple' and then you could concat a number of different
  // items for the next matching phase (again using concat)
  //
  // Theres 'promise' style stuff:
  // CompletionStage<Optional<Item>> futureMaybeItem = fetchItem(id);
  // return onSuccess(futureMaybeItem, maybeItem ->
  // maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
  //
  // Methods for grabbing uri content:
  //  - Choose between put/get  : put( / get(
  //  - Sections of path (s1/s2): segment("segment-one").slash(longSegment()), segment-two-a-value)
  //  - Long number in the path : longSegment(), (Long varName)
  //  - Json sent as param      : Jackson.unmarshaller(Order.class), order
  //  - Integer parameters      : parameter(StringUnmarshallers.INTEGER, "varName"
  //  - optional parameters     : parameterOptional("version", version ->
  //
  // Some important definitions:
  // CompletionStage: A stage of a possibly asynchronous computation, that performs an action or
  //                  computes a value *** when another CompletionStage completes ***.
  //                  A stage completes upon termination of its computation, but this may in turn trigger other dependent stages.
  //
  // If stuck, low level Http API's are available where you can use request/response, see:
  //  - https://doc.akka.io/docs/akka-http/current/introduction.html#low-level-http-server-apis
  //
  // Might we need the HttpClient API to talk to a WoT Thing? Hopefully not, but (in case) see:
  //  - https://doc.akka.io/docs/akka-http/current/introduction.html#http-client-api
  //
  // The Route created using java is then “bound” to a port to start serving HTTP requests:

  private Route createRoute() {
    // Akka-Http is indeed mind bending:
    // This page contains some useful examples (in comparisons to another framework)
    return concat(
        path("hello", () ->
            get(() ->
                complete("<h1>Say hello to akka-http</h1>"))),
        // Akka HTTP routes can interact with actors.
        // This route contains a request-response interaction with an actor.
        // (The resulting response is rendered as JSON and returned when the response arrives from the actor.)
        //
        // Understanding the nested directives soup...
        // https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html
        //
        // https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/alphabetically.html
        // onSuccess: This method will be invoked once when/if a Future that this callback is registered on becomes successfully completed
        // rejectEmptyResponse: replaces a response with no content with an empty rejection.
        path("firstTest", () ->
            get(() ->
              onSuccess(tomsTestMethod(), theMessage ->
                complete(StatusCodes.OK, theMessage.theProof)
              )
            )
        )
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
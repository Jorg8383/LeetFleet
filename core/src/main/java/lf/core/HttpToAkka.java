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

public class HttpToAkka extends AllDirectives {
  private static final Logger log = LogManager.getLogger(HttpToAkka.class);

  public static void main(String[] args) throws Exception {
    // boot up server using the route as defined below
    ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes");

    final Http http = Http.get(system);

    //In order to access all directives we need an instance where the routes are define.
    HttpToAkka app = new HttpToAkka();

    final CompletionStage<ServerBinding> binding =
      http.newServerAt("core", 8080)
          .bind(app.createRoute());

    System.out.println("Server online at http://core:8080/ on the Docker Network.\nPress RETURN to stop...");

    // *** WE NEED TO FIGURE OUT HOW TO SHUT IT DOWN - EASIEST WAY (INSECURE!!) IS WITH URL
    // System.in.read(); // let it run until user presses return

    // binding
    //     .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
    //     .thenAccept(unbound -> system.terminate()); // and shutdown when done
  }

  private Route createRoute() {
    return concat(
        path("hello", () ->
            get(() ->
                complete("<h1>Say hello to akka-http</h1>"))));
  }
}
package service.actor;

import akka.actor.ActorRef;
import akka.remote.WireFormats;
import akka.util.Timeout;
//import org.apache.logging.log4j.core.appender.routing.Route;
import scala.concurrent.duration.Duration;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.Http;

public class CarServer extends HttpApp {

  private final ActorRef carActor;

  Timeout timeout = new Timeout(Duration.create(5, WireFormats.TimeUnit.SECONDS));

  public CarServer(ActorRef carActor) {
    this.carActor = carActor;
  }

  @Override
  public Route routes() {
    return path("car", this::postCar)
      .orElse(path(segment("cars").slash(longSegment()), id -> route(getCar(id))));
  }

  private Route getCar(Long id) {
    return get(() -> {
      CompletionStage<Optional<Car>> car = 
        PatternsCS.ask(CarActor, new GetCarMessage(id), timeout)
          .thenApply(obj -> (Optional<Car>) obj);

      return onSuccess(() -> car, performed -> {
        if (performed.isPresent())
          return complete(StatusCodes.OK, performed.get(), Jackson.marshaller());
        else
          return complete(StatusCodes.NOT_FOUND);
      });
    });
  }

  private Route postCar() {
    return route(post(() -> entity(Jackson.unmarshaller(User.class), car -> {
      CompletionStage<ActionPerformed> carCreated = 
        PatternsCS.ask(carActor, new CreateCarMessage(car), timeout)
          .thenApply(obj -> (ActionPerformed) obj);

      return onSuccess(() -> carCreated, performed -> {
        return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
      });
    })));
  }
}
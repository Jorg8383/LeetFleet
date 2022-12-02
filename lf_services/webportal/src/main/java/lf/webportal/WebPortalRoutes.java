package lf.webportal;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;

import static akka.http.javadsl.server.Directives.*;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatcher0;
import akka.http.javadsl.server.PathMatcher1;
import akka.http.javadsl.server.PathMatchers;
import static akka.http.javadsl.server.PathMatchers.segment;
import akka.http.javadsl.server.Route;
import lf.actor.WebPortalGuardian;
import lf.message.WebPortal;
import lf.model.Vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes can be defined in separated classes like shown in here
 */
// #user-routes-class
public class WebPortalRoutes extends WebPortal {
    // #user-routes-class
    private final static Logger log = LoggerFactory.getLogger(WebPortalRoutes.class);

    private final ActorRef<WebPortalGuardian.Message> webPortalGuardianRef; // We can use this actor to spawn more
                                                                            // actors...
    private final Duration askTimeout;
    private final Scheduler scheduler;

    // Constructor...
    public WebPortalRoutes(ActorSystem<?> system, ActorRef<WebPortalGuardian.Message> webPortalGuardianRef) {
        this.webPortalGuardianRef = webPortalGuardianRef;
        this.scheduler = system.scheduler();
        this.askTimeout = system.settings().config().getDuration("akka.routes.ask-timeout");
    }

    // ------------------------------------------------------------

    // ROUTE METHODS

    // private CompletionStage<WebPortalMessages.FirstMessageToWebPortal>
    // firstTest() {
    // NOTES: The "context" is "The actor context" - the view of the actor 'cell'
    // from the actor.
    // It exposes contextual information for the actor and the current message.
    //
    // The ask-pattern implements the initiator side of a request–reply protocol.
    // The party that asks may be within or without an Actor, since the
    // implementation will fabricate
    // a (hidden) ActorRef that is bound to a CompletableFuture. This ActorRef will
    // need to be
    // injected in the message that is sent to the target Actor in order to function
    // as a reply-to
    // address, therefore the argument to the ask method is not the message itself
    // but a function
    // that given the reply-to address will create the message.

    // We send a message to the Guardian (it's our gateway into the actor system).
    // NOTE: What is the type of ActorRef 'ref' you might ask? Ask it's hidden and
    // all that?
    // It appears to be type matched to the return type of the CompletionStage.
    // return AskPattern.ask(webPortalGuardianRef,
    // ref -> new WebPortalGuardian.ForwardToHandler("One small step for one
    // man...", ref), askTimeout, scheduler);
    // }

    private CompletionStage<WebPortal.ResponseVehicleToWebPortal> vehicleToHandlerViaGuardian(Vehicle vehicle) {
        return AskPattern.ask(webPortalGuardianRef,
                ref -> new WebPortalGuardian.ForwardToHandler(vehicle, ref), askTimeout, scheduler);
    }

    // private CompletionStage<UserRegistry.GetUserResponse> getUser(String name) {
    // return AskPattern.ask(webPortalGuardianRef, ref -> new
    // UserRegistry.GetUser(name, ref), askTimeout, scheduler);
    // }

    // We describe HTTP “routes” and how they should be handled.
    // Each route is composed of one or more level of Directives that narrows down
    // to handling "one specific type of request"
    //
    // One route might start with matching the path of the request, only matching if
    // it is “/hello”
    // ...then narrowing it down to only handle HTTP get requests
    // ...then complete those with a string literal, which will be sent back as an
    // HTTP OK with the string as response body.
    //
    // Another could start with the 'get' and then add on bits of path.
    // It's completely up to you - akka doesn't care how you define the routes.
    // You can define many routes by putting them all in a list and 'concat'ing them
    // together.
    //
    // Another could start with 'multiple' and then you could concat a number of
    // different
    // items for the next matching phase (again using concat)
    //
    // Theres 'promise' style stuff:
    // CompletionStage<Optional<Item>> futureMaybeItem = fetchItem(id);
    // return onSuccess(futureMaybeItem, maybeItem ->
    // maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
    //
    // Methods for grabbing uri content:
    // - Choose between put/get : put( / get(
    // - Sections of path (s1/s2): segment("segment-one").slash(longSegment()),
    // segment-two-a-value)
    // - Long number in the path : longSegment(), (Long varName)
    // - Json sent as param : Jackson.unmarshaller(Order.class), order
    // - Integer parameters : parameter(StringUnmarshallers.INTEGER, "varName"
    // - optional parameters : parameterOptional("version", version ->
    //
    // Some important definitions:
    // CompletionStage: A stage of a possibly asynchronous computation, that
    // performs an action or
    // computes a value *** when another CompletionStage completes ***.
    // A stage completes upon termination of its computation, but this may in turn
    // trigger other dependent stages.
    //
    // If stuck, low level Http API's are available where you can use
    // request/response, see:
    // -
    // https://doc.akka.io/docs/akka-http/current/introduction.html#low-level-http-server-apis
    //
    // Might we need the HttpClient API to talk to a WoT Thing? Hopefully not, but
    // (in case) see:
    // -
    // https://doc.akka.io/docs/akka-http/current/introduction.html#http-client-api
    //
    // The Route created using java is then “bound” to a port to start serving HTTP
    // requests:

    /**
     * This method creates one route (of possibly many more that will be part of
     * your Web App)
     */
    // #all-routes

    PathMatcher1<String> milagePath = PathMatchers
            .segment("wot").slash(segment("total_mileage").concat(segment()));

    // PathMatcher1<String> milagePath = PathMatchers
    // .segment("wot").slash(parameter("total_mileage", milage ->));

    public Route vehicleEventRoutes() {
        // We are using an 'actor per request' pattern. So:
        // For every single request we spawn a 'VehicleEvent' actor.
        // - This actor talks to the AKKA system and does whatever we need
        // asynchronously.
        // - We *EXPECT* a response from it (we block here until we get a response)
        // - Then when it responds we send that back to the user as the HTTP response
        return concat(
                path("hello", () -> get(() -> complete("<h1>Say hello to akka-http</h1>"))),
                // Akka HTTP routes can interact with actors.
                // This route contains a request-response interaction with an actor.
                // (The resulting response is rendered as JSON and returned when the response
                // arrives from the actor.)
                //
                // Understanding the nested directives soup...
                // https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html
                //
                // https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/alphabetically.html
                // onSuccess: This method will be invoked once when/if a Future that this
                // callback is registered on becomes successfully completed
                // rejectEmptyResponse: replaces a response with no content with an empty
                // rejection.
                // path("firstTest", () -> get(() -> onSuccess(firstTest(),
                // theMessage -> complete(StatusCodes.OK, theMessage.theProof)))),
                path("wot",
                        () -> post(() -> entity(Jackson.unmarshaller(Vehicle.class),
                                vehicle -> onSuccess(vehicleToHandlerViaGuardian(vehicle),
                                        theMessage -> complete(StatusCodes.OK, theMessage.vehicle,
                                                Jackson.marshaller()))))),

                // get(() -> path(milagePath, id -> complete("id is: " + id))));

                // get(() -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                // fleetId -> complete("Fleetid is" + fleetId + "vehicleid is " +
                // vehicleId)))));

                // MAKE FLEET ID OPTIONAL??
                concat(pathPrefix("wot",
                        () -> concat(
                                pathPrefix("total_mileage",
                                        () -> parameter("vehicle_id",
                                                vehicleId -> parameter("fleet_id",
                                                        fleet_id -> parameter("total_mileage",
                                                                total_mileage -> onSuccess(
                                                                        vehicleToHandlerViaGuardian(Vehicle.createForMileage(
                                                                                vehicleId, fleet_id,
                                                                                Float.valueOf(total_mileage))),
                                                                        theMessage -> complete(StatusCodes.OK,
                                                                                theMessage.vehicle,
                                                                                Jackson.marshaller()))))))))),
                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("next_service_distance",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete(
                                                "next_service_distance - Fleetid is " + fleetId + " vehicleid is "
                                                        + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("door_status",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete("door_status request - Fleetid is " + fleetId
                                                + " vehicleid is " + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("maintenance_needed",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete(
                                                "maintenance_needed request - Fleetid is " + fleetId + " vehicleid is "
                                                        + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("lock_door",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete("lock_door request - Fleetid is " + fleetId
                                                + " vehicleid is " + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("unlock_door",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete("unlock_door request - Fleetid is " + fleetId
                                                + " vehicleid is " + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("low_on_oil",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete("low_on_oil request - Fleetid is " + fleetId
                                                + " vehicleid is " + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("low_tire_pressure",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete(
                                                "low_tire_pressure - Fleetid is " + fleetId + " vehicleid is "
                                                        + vehicleId))))))),

                concat(pathPrefix("wot",
                        () -> concat(pathPrefix("maintenance_need",
                                () -> parameter("vehicle_id", vehicleId -> parameter("fleet_id",
                                        fleetId -> complete(
                                                "maintenance_need request - Fleetid is " + fleetId + " vehicleid is "
                                                        + vehicleId)))))))

        );
        // parameter("vehicle_id", vehicleId -> parameter("fleet_id",
        // fleetId -> complete("Fleetid is" + fleetId + "vehicleid is " +
        // vehicleId)))));

        /// wot/total_mileage?vehicle_id=sssssss&fleet_id=ssssss
        // #all-routes
    }

}
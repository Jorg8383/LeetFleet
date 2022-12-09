package lf.actor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.typed.javadsl.ActorFlow;
import lf.core.VehicleIdRange;
import lf.message.FleetManagerMsg;
import lf.message.FleetManagerMsg.ListVehiclesJson;
import lf.message.FleetManagerMsg.Message;
import lf.message.FleetManagerMsg.ProcessVehicleWotUpdate;
import lf.message.FleetManagerMsg.ProcessVehicleWebUpdate;
import lf.message.FleetManagerMsg.RegistrationSuccess;
import lf.message.VehicleEventMsg;
import lf.message.WebPortalMsg;
import lf.model.Vehicle;

/**
 * A Fleet Manager. This time for the Notional Careless Fleet. Could suit any
 * abstraction.
 */
public class CarelessFleetManager extends AbstractBehavior<Message> {

    // ENCAPSULATION:
    public long MANAGER_ID; // The Registry assigns an ID on registration. Subject to change.

    // We need some way to have vehicles 'belong' in a fleet. In this toy example
    // we just use a range. In a real system this would be some set of values
    // (perhaps a hashmap) persisted in a db...
    private VehicleIdRange carelessFleetIdRange = new VehicleIdRange(0, 2499);

    public ActorRef<Registry.Message> REGISTRY_REF = null;

    private Duration timeout = Duration.ofMillis(2500);

    // Track the VehicleTwin actors we have "live" (active actor refs in the
    // cluster).
    // ?!IF!? we had gotten the java WoT working - this may well have been WoT
    // "consumed thing" that was ALSO an akka actor. That... would have been sweet.
    private static HashMap<Long, ActorRef<VehicleTwin.Message>> vehicles = new HashMap<Long, ActorRef<VehicleTwin.Message>>();

    // CREATE THIS ACTOR
    public static Behavior<Message> create() {
        return Behaviors.setup(
                // Register this actor with the receptionist
                context -> {
                    context
                            .getSystem()
                            .receptionist()
                            .tell(Receptionist.register(FleetManagerMsg.fleetManagerServiceKey, context.getSelf()));

                    return Behaviors.setup(CarelessFleetManager::new);
                });
    }

    // ADD TO CONTEXT
    protected CarelessFleetManager(ActorContext<Message> context) {
        super(context);
        // send a message to the registry to register!!!! FFS

        // akka://my-sys@host.example.com:5678/user/service-b
        // registry.tell(new Registry.RegisterFleetManager(getContext().getSelf()));
    }

    // =========================================================================

    // MESSAGE HANDLING:
    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(RegistrationSuccess.class, this::onRegistrationSuccess)
                .onMessage(ProcessVehicleWotUpdate.class, this::onProcessVehicleWotUpdate)
                .onMessage(ProcessVehicleWebUpdate.class, this::onProcessVehicleWebUpdate)
                .onMessage(ListVehiclesJson.class, this::onListVehiclesJson)
                .build();
    }

    private Behavior<Message> onRegistrationSuccess(RegistrationSuccess message) {
        // Store the unique id assigned to this FleetManager. We'll need it if
        // we want to 'DeRegister' on shutdown...
        MANAGER_ID = message.mgrId;
        REGISTRY_REF = message.registryRef;
        getContext().getLog().info("FleetManager Registration Confirmed.");

        // Send manager name to registry
        REGISTRY_REF.tell(new Registry.SetFleetManagerName(MANAGER_ID, "Careless"));
        return this;
    }

    /**
     *
     * @param message
     * @return
     */
    private Behavior<Message> onProcessVehicleWotUpdate(ProcessVehicleWotUpdate message) {
        // Each VehicleId is in the format 'WoT-ID-Mfr-VIN-nnnn' in our Toy system
        // We extract the 'nnnn' (id) part to see if this vehicle belongs to this
        // fleet manager:
        Vehicle vehicle = message.vehicle;
        long vehicleIdLong = vehicle.getVehicleIdLong();

        if (vehicleIdLong != 0) {
            if (carelessFleetIdRange.contains(vehicleIdLong)) {
                getContext().getLog().info("Vehicle Web Event for CareleesFleet received.");

                // This should NOT be the first communication for this vehicle.
                // But... why assume anything. Just stamp it with this fleetId
                // every time.
                vehicle.setFleetManager(Long.toString(MANAGER_ID));

                ActorRef<VehicleTwin.Message> vehicleTwinRef;

                // First - if the VehicleTwin for this vehicle doesn't exist, we
                // must create an actor for it:
                if (!vehicles.keySet().contains(vehicleIdLong)) {
                    // Create an (anonymous) VehicleTwin actor to represent this vehicle on the
                    // actor system
                    vehicleTwinRef = getContext()
                            .spawnAnonymous(VehicleTwin.create(vehicle.getVehicleId())); // 'anonymous' actor
                    vehicles.put(vehicleIdLong, vehicleTwinRef);
                } else {
                    vehicleTwinRef = vehicles.get(vehicleIdLong);
                }

                // Update the VehicleTwin with the 'vehicle' pojo we have been
                // sent
                vehicleTwinRef.tell(new VehicleTwin.WotUpdate(vehicle));

                // We message the VehicleWotEvent handler immediately to say we're
                // done. There's no confirmation etc.. Worst case - we lose one
                // message and the client reporting is one transaction out of date.
                // A real system might take a different approach here, depending
                // on the designers goals.
                message.vehicleWotEventRef.tell(new VehicleEventMsg.EventComplete(vehicle));

            } else {
                getContext().getLog().info(
                        "Vehicle Event for non-fleet vehicle received (" + String.valueOf(vehicleIdLong)
                                + "). Ignoring.");
            }
        }

        return this;
    }

    /**
     *
     * @param message
     * @return
     */
    private Behavior<Message> onProcessVehicleWebUpdate(ProcessVehicleWebUpdate message) {
        // Each VehicleId is in the format 'WoT-ID-Mfr-VIN-nnnn' in our Toy system
        // We extract the 'nnnn' (id) part to see if this vehicle belongs to this
        // fleet manager:
        Vehicle vehicle = message.vehicle;
        long vehicleIdLong = vehicle.getVehicleIdLong();

        if (vehicleIdLong != 0) {
            if (carelessFleetIdRange.contains(vehicleIdLong)) {
                getContext().getLog().info("Vehicle Event for CareleesFleet received.");

                // This might be the first communication for this vehicle. It
                // might not. Just stamp it with this fleetId every time.
                vehicle.setFleetManager(Long.toString(MANAGER_ID));

                ActorRef<VehicleTwin.Message> vehicleTwinRef;

                // First - if the VehicleTwin for this vehicle doesn't exist, then
                // something has gone wrong. The client should only be able to
                // modify the state on active vehicles. If we don't find the
                // vehicle in our active actor list we return a failure message,
                // in this case an empty vehicle object with the description set
                // to a warning message.
                if (!vehicles.keySet().contains(vehicleIdLong)) {
                    vehicle.setVehicleId("ERROR: Vehicle Not Found. It may have been switched off...");
                } else {
                    vehicleTwinRef = vehicles.get(vehicleIdLong);

                    // Ask the VehicleTwin to review the current update. If it
                    // finds a changed value we support - it will update the
                    // model AND attempt to change the state of the WoT using
                    // the stored URI
                    vehicleTwinRef.tell(new VehicleTwin.WebUpdate(vehicle));
                }

                // We message the VehicleWebEvent handler immediately to say we're
                // done. There's no confirmation etc..
                // On the client we inform the user the state change has been
                // requested - to check for updates soon.
                message.vehicleWebEventRef.tell(new VehicleEventMsg.EventComplete(vehicle));

            } else {
                getContext().getLog().info(
                        "Vehicle Event for non-fleet vehicle received (" + String.valueOf(vehicleIdLong)
                                + "). Ignoring.");
            }
        }

        return this;
    }

    /**
     * Return a list of active registered vehicles in JSON format
     * @param message
     * @return
     */
    private Behavior<Message> onListVehiclesJson(ListVehiclesJson message) {
        ArrayList<Vehicle> vehModels = new ArrayList<Vehicle>();

        // Loop over the manager names now and generate the content for the manager
        // list..
        try {
          for (Map.Entry<Long, ActorRef<VehicleTwin.Message>> entry : vehicles.entrySet())
          {
            // Flows... more on these here:
            // https://doc.akka.io/docs/akka/current/stream/stream-flows-and-basics.html

            // Request-response pattern with ask:
            // https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html
            getContext().ask(
                VehicleTwin.VehicleModel.class,
                entry.getValue(),
                timeout,
                // construct the outgoing message
                (ActorRef<VehicleTwin.VehicleModel> ref) -> new VehicleTwin.RequestVehicleModel(ref),
                // adapt the response (or failure to respond)
                (response, throwable) -> {
                    if (response != null) {
                        return new AdaptedResponse(response.message);
                    } else {
                        return new AdaptedResponse("Request failed");
                    }
                });

// CHRRIST - do I add a completion stage??? or what????

            getContext().getLog().info(man.getValue() + " will be sent to the client.");
          }
        } catch (Exception e) {
          getContext().getLog().error("", e);
        }

        message.portalRef.tell(new WebPortalMsg.VehicleListToWebP(vehModels));

        return this;
      }

}
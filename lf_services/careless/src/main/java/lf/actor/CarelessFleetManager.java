package lf.actor;

import java.util.HashMap;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import lf.core.VehicleIdRange;
import lf.message.FleetManagerMsg;
import lf.message.FleetManagerMsg.Message;
import lf.message.FleetManagerMsg.RegistrationSuccess;
import lf.model.Vehicle;
import lf.message.FleetManagerMsg.ProcessVehicleUpdate;

/**
 * A Fleet Manager. This time for the Notional Careless Fleet. Could suit any
 * abstraction.
 */
public class CarelessFleetManager extends AbstractBehavior<Message> {

    // ENCAPSULATION:
    public long MANAGER_ID = 1;
    // We need some way to have vehicles 'belong' in a fleet. In this toy example
    // we just use a range. In a real system this would be some set of values
    // (perhaps a hashmap) persisted in a db...
    private VehicleIdRange carelessFleetIdRange = new VehicleIdRange(0, 2499);

    public ActorRef<Registry.Message> REGISTRY_REF = null;

    // Track the VehicleTwin actors we have "live" (active actor refs in the
    // cluster).
    // ?!IF!? we had gotten the java WoT working - this may well have been WoT
    // "consumed thing" that was ALSO an akka actor. That... would have been sweet.
    private static HashMap<Long, ActorRef<FleetManagerMsg.Message>> vehicles = new HashMap<Long, ActorRef<FleetManagerMsg.Message>>();

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
                .onMessage(ProcessVehicleUpdate.class, this::onProcessVehicleUpdate)
                .build();
    }

    private Behavior<Message> onRegistrationSuccess(RegistrationSuccess message) {
        // Store the unique id assigned to this FleetManager. We'll need it if
        // we want to 'DeRegister' on shutdown...
        MANAGER_ID = message.mgrId;
        REGISTRY_REF = message.registryRef;
        getContext().getLog().info("FleetManager Registration Confirmed.");
        return this;
    }

    private Behavior<Message> onProcessVehicleUpdate(ProcessVehicleUpdate message) {
        // Each VehicleId is in the format 'WoT-ID-Mfr-VIN-nnnn' in our Toy system
        // We extract the 'nnnn' (id) part to see if this vehicle belongs to this
        // fleet manager:
        Vehicle vehicle = message.vehicle;

        boolean validVehicleId = false;
        long vehicleId = 0;
        try {
            // vehicleId =
            // Long.parseLong(vehicle.getVehicleId().substring(message.vehicle.getVehicleId()));
            // // <= HARD CODED ID EXTRACTION

            validVehicleId = true;
        } catch (Exception e) {
            // Not concerned with the nature of the exception
            // Toy system: not valid => ignore.
        }

        if (validVehicleId) {
            if (carelessFleetIdRange.contains(vehicleId)) {
                getContext().getLog().info("Vehicle Event for CareleesFleet received.");

                // First if the VehicleTwin for this vehicle doesn't exist, we
                // must create it.
                if (!vehicles.keySet().contains(vehicleId)) {
                    // Create an (anonymous) VehicleTwin actor to represent this vehicle on the
                    // actor system
                    ActorRef<VehicleTwin.Message> vehicleTwinRef = getContext()
                            .spawnAnonymous(VehicleTwin.create(vehicleId)); // 'anonymous' actor
                    // store
                }

                // MORE STUFF
                // PERFORM THE UDATE ON THE VEHCILE ACTOR
                // MESSAGE THE VehicleEvent to say we're done. message.vehicleEventRef
            } else {
                getContext().getLog().info(
                        "Vehicle Event for non-fleet vehicle received (" + String.valueOf(vehicleId) + "). Ignoring.");
            }
        }

        return this;
    }

}
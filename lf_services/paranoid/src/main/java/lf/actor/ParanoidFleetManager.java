package lf.actor;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;

import com.typesafe.config.Config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.receptionist.Receptionist;
import lf.core.VehicleIdRange;
import lf.core.VehicleQuery;
import lf.message.FleetManagerMsg;
import lf.message.FleetManagerMsg.ListVehiclesJson;
import lf.message.FleetManagerMsg.Message;
import lf.message.FleetManagerMsg.ProcessVehicleWotUpdate;
import lf.message.FleetManagerMsg.QueryTimeout;
import lf.message.FleetManagerMsg.ProcessVehicleWebUpdate;
import lf.message.FleetManagerMsg.RegistrationSuccess;
import lf.message.FleetManagerMsg.VehicleModelResponse;
import lf.message.VehicleEventMsg;
import lf.message.WebPortalMsg;
import lf.model.Vehicle;

/**
 * A Fleet Manager. This time for the Notional Paranoid Fleet. Could suit any
 * abstraction.
 */
public class ParanoidFleetManager extends AbstractBehavior<Message> {

    // ENCAPSULATION:
    public long MANAGER_ID; // The Registry assigns an ID on registration. Subject to change.

    private VehicleIdRange paranoidFleetIdRange = new VehicleIdRange(7500, 9999);

    public ActorRef<Registry.Message> REGISTRY_REF = null;

    // Track the VehicleTwin actors we have "live" (active actor refs in the
    // cluster).
    // ?!IF!? we had gotten the java WoT working - this may well have been WoT
    // "consumed thing" that was ALSO an akka actor. That... would have been sweet.
    private static HashMap<Long, ActorRef<VehicleTwin.Message>> vehicles = new HashMap<Long, ActorRef<VehicleTwin.Message>>();

    public long SEED_QUERY_ID = 1; // The FleetManager assigns an ID on messages 'in play'
    private Duration timeout;

    // Track the vehicle queries from the web client.
    private static HashMap<Long, VehicleQuery> queries = new HashMap<Long, VehicleQuery>();

    private final TimerScheduler<Message> timers;

    // CREATE THIS ACTOR
    public static Behavior<Message> create() {
        return Behaviors.withTimers(
            timers -> {
            return Behaviors.setup(
                    // Register this actor with the receptionist
                    context -> {
                        context
                            .getSystem()
                            .receptionist()
                            .tell(Receptionist.register(FleetManagerMsg.fleetManagerServiceKey, context.getSelf()));

                        return new ParanoidFleetManager(timers, context);
                    });
        });
    }

    // ADD TO CONTEXT
    protected ParanoidFleetManager(TimerScheduler<Message> timers, ActorContext<Message> context) {
        super(context);

        this.timers = timers;

        Config config = context.getSystem().settings().config().getConfig("akka.fleet-manager");
        // The fleetmanager can at times gather information from its fleet. When
        // it does so in response to a web request it must - of needs - timeout
        // if some expected responses do not arrive. This is configured in the
        // application config so that it can be updated without altering code.
        this.timeout = config.getDuration("query-timeout");  // config string specifies the Duration units.
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
                .onMessage(VehicleModelResponse.class, this::onVehicleModelResponse)
                .onMessage(QueryTimeout.class, this::onQueryTimeout)
                .build();
    }

    private Behavior<Message> onRegistrationSuccess(RegistrationSuccess message) {
        // Store the unique id assigned to this FleetManager. We'll need it if
        // we want to 'DeRegister' on shutdown...
        MANAGER_ID = message.mgrId;
        REGISTRY_REF = message.registryRef;
        getContext().getLog().debug("FleetManager Registration Confirmed.");

        // Send manager name to registry
        REGISTRY_REF.tell(new Registry.SetFleetManagerName(MANAGER_ID, "Paranoid"));
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
            if (paranoidFleetIdRange.contains(vehicleIdLong)) {
                getContext().getLog().debug("Vehicle Event for CareleesFleet received.");

                // Is this the first communication for this vehicle?
                if (vehicle.getFleetId().equalsIgnoreCase("not_defined")) {
                    vehicle.setWotFltIdUpdateRqd(true);
                }
                // Why assume anything. Just stamp it with this fleetId every time.
                vehicle.setFleetId(Long.toString(MANAGER_ID));

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
                getContext().getLog().info("Vehicle Event for non-fleet vehicle received ("
                        + String.valueOf(vehicleIdLong) + "). Ignoring.");
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
            if (paranoidFleetIdRange.contains(vehicleIdLong)) {
                getContext().getLog().debug("Vehicle Event for ParanoidFleet received.");

                // Is this the first communication for this vehicle?
                if (vehicle.getFleetId().equalsIgnoreCase("not_defined")) {
                    vehicle.setWotFltIdUpdateRqd(true);
                }
                // Why assume anything. Just stamp it with this fleetId every time.
                vehicle.setFleetId(Long.toString(MANAGER_ID));

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
                getContext().getLog().info("Vehicle Event for non-fleet vehicle received ("
                        + String.valueOf(vehicleIdLong) + "). Ignoring.");
            }
        }

        return this;
    }

    /**
     * Return a list of active registered vehicles in JSON format
     *
     * @param message
     * @return
     */
    private Behavior<Message> onListVehiclesJson(ListVehiclesJson message) {
        long query_id = SEED_QUERY_ID++;

        Collection<ActorRef<VehicleTwin.Message>> vehicleTwinRefs = vehicles.values();

        // Each timer has a key and if a new timer with same key is started the previous is cancelled
        // and it’s guaranteed that a message from the previous timer is not received, even though it
        // might already be enqueued in the mailbox when the new timer is started
        // ** WE NEED THE TIMER_KEY ** so we can cancel it if all the vehicles respond before the
        // timeout.
        Object timer_key = new Object();

        VehicleQuery thisQuery = new VehicleQuery(
            query_id, message.portalRef, timer_key, vehicleTwinRefs.size());

        // Loop over the Vehicle twins. Request the content for the vehicle list..
        try {
            for (ActorRef<VehicleTwin.Message> vehicleTwinRef : vehicleTwinRefs)
            {
                vehicleTwinRef.tell(
                    new VehicleTwin.RequestVehicleModel(query_id, getContext().getSelf())
                    );
            }
        } catch (Exception e) {
            getContext().getLog().error("", e);
        }

        // Send a future message to self with the timeout for this query...
        timers.startSingleTimer(timer_key, new FleetManagerMsg.QueryTimeout(query_id), timeout);

        // Store the query in the local map for tracking:
        queries.put(query_id, thisQuery);

        return this;
    }

    /**
     * Accept a Vehicle model from one of the vehicle twins. Store it in the
     * relevant query. If the query is complete - return it.
     *
     * @param message
     * @return
     */
    private Behavior<Message> onVehicleModelResponse(VehicleModelResponse message) {
        VehicleQuery thisQuery = queries.get(message.query_id);

        thisQuery.vehicles.add(message.vehicle);

        // Is the query complete?
        if (thisQuery.expected_query_size == thisQuery.vehicles.size()) {
            thisQuery.portalRef.tell(new WebPortalMsg.VehicleListToWebP(thisQuery.vehicles));

            // Cancel the timer...
            timers.cancel(thisQuery.timer_key);

            queries.remove(thisQuery.query_id);
        }

        return this;
    }

    /**
     * Query timeout. We take the view we have gathered all the information we
     * can and return the query as it.  It is possible one of the queried vehicles
     * has gone offline while the query was in progress.
     *
     * @param message
     * @return
     */
    private Behavior<Message> onQueryTimeout(QueryTimeout message) {
        VehicleQuery thisQuery = queries.get(message.query_id);

        // The query is now complete (timed out)
        thisQuery.portalRef.tell(new WebPortalMsg.VehicleListToWebP(thisQuery.vehicles));
        queries.remove(thisQuery.query_id);

        return this;
    }

}
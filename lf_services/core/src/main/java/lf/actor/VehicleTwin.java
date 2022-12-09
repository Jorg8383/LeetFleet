package lf.actor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONPropertyIgnore;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.message.LFSerialisable;
import lf.model.Vehicle;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.providers.PooledConnectionProvider;

public class VehicleTwin extends AbstractBehavior<VehicleTwin.Message> {

    // MESSAGES:
    //
    public interface Message {
    }

    public final static class WotUpdate implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public WotUpdate(Vehicle vehicle) {
          this.vehicle = vehicle;
        }
    }

    public final static class WebUpdate implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public WebUpdate(Vehicle vehicle) {
          this.vehicle = vehicle;
        }
    }

    /**
     * Return a copy of the current vehicle state, for reporting etc. elsewhere.
     */
    public final static class RequestVehicleModel implements Message, LFSerialisable {
        // NOTE: the return type here. We define our own response message!
        //       designed for the 'akka ask pattern'
        public final ActorRef<VehicleModel> respondTo;
        public RequestVehicleModel(@JsonProperty("respondTo") ActorRef<VehicleModel> respondTo) {
            this.respondTo = respondTo;
        }
    }

    // Now... here's an interesting little message. Notice is *does not*
    // implement the Message interface.  That's because this message is not
    // handled here. "So why not declare it where it is handled?" Well... it
    // could be handled anywhere. It will be used as the response to an 'ask'
    // pattern request.
    public final static class VehicleModel implements LFSerialisable {
        public final Vehicle vehicle;
        public VehicleModel(@JsonProperty("vehicle") Vehicle vehicle) {
          this.vehicle = vehicle;
        }
    }

    public final static class GracefulShutdown implements Message, LFSerialisable {
        public final String note;
        public GracefulShutdown(String note) {
          this.note = note;
        }
    }

    // ENCAPSULATION:
    // This is a compromise (storing a vehicle, inside the VehicleTwin). But for
    // this toy system it will suffice. The initial ideology (of a WoT consumed
    // thing, that was also an AKKA actor, living in the akka cluster would have
    // been fun to explore).
    private Vehicle vehicle;
    private UnifiedJedis jedis;

    // CREATE THIS ACTOR
    public static Behavior<Message> create(String vehicleId) {
        return Behaviors.setup(
                context -> new VehicleTwin(vehicleId, context));
    }

    // ADD TO CONTEXT
    // NOTE: This constructor expects the 'WoT' String VehicleId.
    private VehicleTwin(String vehicleId, ActorContext<Message> context) {
        super(context);

        // Interesting question... where will VehicleTwins get this information?
        // Perhaps passed down from the WebPortal?

        // Read the Redis setting from the System Config.
        Config config = context.getSystem().settings().config().getConfig("akka.redis");
        String redisHostname = config.getString("hostname");
        int redisPort = config.getInt("port"); // Probably... 6379

        // We are doing one connection per VehicleTwin actor lifecycle - this is poor practice - address?

        // JedisPooled jedis = new JedisPooled("host.docker.internal", 6379);
        // Protocol.DEFAULT_HOST redisHostname
        HostAndPort hostAndPort = new HostAndPort(redisHostname, redisPort);
        PooledConnectionProvider provider = new PooledConnectionProvider(hostAndPort);
        this.jedis = new UnifiedJedis(provider);
        // JedisPool pool = new JedisPool("localhost", 6379);
        // jedis.set("clientName", "Jedis");

        long vehicleIdLong = Vehicle.wotIdToLongId(vehicleId);
        String key = "vehicle:" + vehicleIdLong;
        // Check if the key exists in jedis...
        if (jedis.exists(key)) {
            // key exists, retrieve the object
            //
            getContext().getLog().info("JEDIS KJEY EXISTS BLOCK ################################################");
            try {
                String vehicleAsJSON = jedis.get(key);
                getContext().getLog().info("\t value returned from redis -> " + vehicleAsJSON);
                // Unmarshall the JSON into a Vehicle object using the Jackson object mapper
                vehicle = new ObjectMapper().readValue(vehicleAsJSON, Vehicle.class);
                getContext().getLog().info("\t Test Attribute -> " + vehicle.getVehicleId());
            }
            catch (Exception e) {
                getContext().getLog().error("Vehicle returned from Store could not be unmarshalled");
            }
        }
        else {
            try {
                vehicle = Vehicle.createTemplate(vehicleId);
                // Marshall the object using a Jackson object mapper
                String vehicleAsJSON = new ObjectMapper().writeValueAsString(vehicle);
                jedis.set(key, vehicleAsJSON);
            }
            catch (Exception e) {
                getContext().getLog().error("Vehicle for Storage could not be marshalled");
            }
        }

    }

    // =========================================================================

    // MESSAGE HANDLING:

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(WotUpdate.class, this::onWotUpdate)
                .onMessage(WebUpdate.class, this::onWebUpdate)
                .onMessage(RequestVehicleModel.class, this::onRequestVehicleModel)
                .onMessage(GracefulShutdown.class, this::onGracefulShutdown)
                .build();
    }

    private Behavior<Message> onWotUpdate(WotUpdate message) {
        // We just completely overwrite state.  Cause... why not... toy system.
        this.vehicle = message.vehicle;

        updateRedisModel(message.vehicle.getVehicleIdLong());
        // Toy system - no action take if key does not exist.

        return this;
    }

    /**
     * The web client has requested a change in state.  Update the vehicle model
     * in redis and request a change to WoT by calling the WoT URL directly.
     */
    private Behavior<Message> onWebUpdate(WebUpdate message) {
        Vehicle requestedState = message.vehicle;

        // Supported Actions:
        // LOCK /UNLOCK DOORS:
        if (!vehicle.getDoorStatus().equals(requestedState.getDoorStatus())) {
            // Update the VehicleTwin:
            vehicle.setDoorStatus(requestedState.getDoorStatus());

            // Update the WoT Exposed Thing
            // We are not proud. We planned to do this on a java WoT object.
            // But this approach will suffice for the toy system:
            // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
            try {
                URL url = new URL(vehicle.getTdURL() + "/LOCK_THE_DOORS_BIT_WHAT_SHOULD_THIS_BE");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // success... can we just comment this out?  Do we care about the WoT response!?!?!?!?!?!?!?!?
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    System.out.println(response.toString());
                } else {
                    System.out.println("GET request did not work.");
                }
            }
            catch (IOException ioe) {

            }
        }

        updateRedisModel(message.vehicle.getVehicleIdLong());
        // Toy system - no action take if key does not exist.

        return this;
    }

    // Graceful shutdown:
    // ??? SCHEDULE THIS?? DRIVEN BY SOMETHING ELSE????
    private Behavior<Message> onGracefulShutdown(GracefulShutdown message) {
        getContext().getSystem().log().info("Initiating graceful shutdown...");

        // Shut down our redis connection...
        jedis.close();

        // Here it can perform graceful stop (possibly asynchronous) and when completed
        // return `Behaviors.stopped()` here or after receiving another message.
        return Behaviors.stopped();
    }

    /**
     * Update the redis store with the current values in the vehicle model
     * @param vehicleIdLong
     */
    private void updateRedisModel(long vehicleIdLong) {
        // Update the redis store...
        String key = "vehicle:" + vehicleIdLong;
        // Check if the key exists in jedis...
        if (jedis.exists(key)) {
            getContext().getLog().info("UPDATE METHOD ENTRY  #################");
            try {
                // Marshall the object using a Jackson object mapper
                String vehicleAsJSON = new ObjectMapper().writeValueAsString(vehicle);
                jedis.set(key, vehicleAsJSON);
            }
            catch (Exception e) {
                getContext().getLog().error("Vehicle returned from Store could not be unmarshalled");
            }
        }
    }

    /**
     * When a copy of the vehicle modelis requested, supply it!
     * @param message
     * @return
     */
    private Behavior<Message> onRequestVehicleModel(RequestVehicleModel message) {
        message.respondTo.tell(new VehicleModel(this.vehicle));
        return this;
    }

}
package lf.actor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

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

    public final static class Update implements Message, LFSerialisable {
        public final Vehicle vehicle;
        public Update(Vehicle vehicle) {
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
                .onMessage(Update.class, this::onUpdate)
                .onMessage(GracefulShutdown.class, this::onGracefulShutdown)
                .build();
    }

    private Behavior<Message> onUpdate(Update message) {
        // We just completely overwrite state.  Cause... why not... toy system.
        this.vehicle = message.vehicle;

        // Update the redis store...
        String key = "vehicle:" + message.vehicle.getVehicleIdLong();
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

}
package lf.actor;

import com.typesafe.config.Config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.model.Vehicle;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.providers.PooledConnectionProvider;

public class VehicleTwin extends AbstractBehavior<VehicleTwin.Message> {

    // MESSAGES:
    //
    public interface Message {
    }

    // public final static class FleetManagerList implements Message {
    // public final Collection<ActorRef<FleetManager.Message>> fleetManagerRefs;
    // public final ActorRef<Registry.Message> registryRef;
    // public FleetManagerList(Collection<ActorRef<FleetManager.Message>>
    // fleetManagerRefs, ActorRef<Registry.Message> registryRef) {
    // this.fleetManagerRefs = fleetManagerRefs;
    // this.registryRef = registryRef;
    // }
    // }

    // ENCAPSULATION:
    // This is a compromise (storing a vehicle, inside the VehicleTwin). But for
    // this toy system it will suffice. The initial ideology (of a WoT consumed
    // thing, that was also an AKKA actor, living in the akka cluster would have
    // been fun to explore).
    private Vehicle vehicle;

    // CREATE THIS ACTOR
    public static Behavior<Message> create(long vehicleId) {
        return Behaviors.setup(
                context -> new VehicleTwin(vehicleId, context));
    }

    // ADD TO CONTEXT
    private VehicleTwin(long vehicleId, ActorContext<Message> context) {
        super(context);

        // Interesting question... where will VehicleTwins get this information?
        // Perhaps passed down from the WebPortal?

        // Read the Redis setting from the System Config.
        Config config = context.getSystem().settings().config().getConfig("akka.redis");
        String redisHostname = config.getString("hostname");
        int redisPort = config.getInt("port"); // Probably... 6379

        // Testing jedis connection - to be moved to vehicle actor

        // JedisPooled jedis = new JedisPooled("host.docker.internal", 6379);
        // Protocol.DEFAULT_HOST redisHostname
        HostAndPort hostAndPort = new HostAndPort(redisHostname, redisPort);
        PooledConnectionProvider provider = new PooledConnectionProvider(hostAndPort);
        UnifiedJedis client = new UnifiedJedis(provider);
        // JedisPool pool = new JedisPool("localhost", 6379);
        // jedis.set("clientName", "Jedis");
        String key = "vehicle:" + vehicleId;

        if (client.exists(key)) {
            Vehicle vehicle = (Vehicle) client.jsonGet(key);
        } else {
            Vehicle vehicle = Vehicle.createTemplate(vehicleId);
            client.jsonSetLegacy(key, vehicle);
        }
        this.vehicle = vehicle;

        // jedis..jsonSet("vehicle:111", truck);
        // log.info("client ->", client);
        // Object fred = jedis.jsonGet("111");
        // log.info("fred ->", fred);

        // System.out.println(client.jsonGet("vehicle:111"));
        // client.set("planets", "Venus");
        // System.out.println(client.get("planets"));

        client.close();
        // pool.close();
    }

    // =========================================================================

    // MESSAGE HANDLING:

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                // .onMessage(FleetManagerList.class, this::onFleetManagerList)
                .build();
    }

    // private Behavior<Message>
    // onFirstMessageFromWebPortal(FirstMessageFromWebPortal message) {
    // message.portalRef.tell(new
    // WebPortal.FirstMessageToWebPortal(message.theProof, getContext().getSelf()));

    // return this;
    // }

}
package lf.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.model.Vehicle;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.providers.PooledConnectionProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class VehicleTwin extends AbstractBehavior<VehicleTwin.Message> {

    // MESSAGES:
    //
    public interface Message {}

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

    // CREATE THIS ACTOR
    public static Behavior<Message> create() {
        return Behaviors.setup(VehicleTwin::new);
    }

    // ADD TO CONTEXT
    private VehicleTwin(ActorContext<Message> context) {
        super(context);
        // constructor stuff here

        // Interesting question... where will VehicleTwins get this information?
        // Perhaps passed down from the WebPortal?
        String redisHostname = "localhost";
        int    redisPort = 6379;

        // ==========================================================================
        // REDIS TESTING
        // ==========================================================================

        // Testing jedis connection - to be moved to vehicle actor
        Vehicle truck = new Vehicle("v1", "f1");

        // JedisPooled jedis = new JedisPooled("host.docker.internal", 6379);
        // Protocol.DEFAULT_HOST redisHostname
        HostAndPort config = new HostAndPort(redisHostname, redisPort);
        PooledConnectionProvider provider = new PooledConnectionProvider(config);
        UnifiedJedis client = new UnifiedJedis(provider);
        // JedisPool pool = new JedisPool("localhost", 6379);
        // jedis.set("clientName", "Jedis");
        client.jsonSetLegacy("vehicle:111", truck);
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
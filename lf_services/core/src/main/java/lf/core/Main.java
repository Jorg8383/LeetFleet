package lf.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import lf.model.Vehicle;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import redis.clients.jedis.providers.PooledConnectionProvider;

public class Main  {

 public static void main(String[] args) {
        // Interesting question... where will VehicleTwins get this information?
        // Perhaps passed down from the WebPortal?

        // We are doing one connection per request - this is extremely bad practice - address?

        // JedisPooled jedis = new JedisPooled("host.docker.internal", 6379);
        // Protocol.DEFAULT_HOST redisHostname
        HostAndPort hostAndPort = new HostAndPort("localhost", 6379);
        PooledConnectionProvider provider = new PooledConnectionProvider(hostAndPort);
        UnifiedJedis jedis = new UnifiedJedis(provider);

        String vehicleId = "WoT-ID-Mfr-VIN-1234";

        Vehicle vehicle;

        long vehicleIdLong = Vehicle.wotIdToLongId(vehicleId);
        String key = "vehicle:" + vehicleIdLong;
        // Check if the key exists in jedis...
        if (jedis.exists(key)) {
            // key exists, retrieve the object
            //
            //vehicle = (Vehicle) jedis.jsonGet(key);
            System.out.println("JEDIS KJEY EXISTS BLOCK ################################################");
            String vehicleAsJSON = jedis.get(key);
            System.out.println("\t value returned from redis -> " + vehicleAsJSON);
            try {
                vehicle = new ObjectMapper().readValue(vehicleAsJSON, Vehicle.class);
                //vehicle = new Gson().fromJson(vehicleAsJSON, Vehicle.class);
                System.out.println("\t Test Attribute -> " + vehicle.getVehicleId());
            }
            catch (Exception e) {
                //
            }
        } else {
            vehicle = Vehicle.createTemplate(vehicleId);
            try {
                String vehicleAsJSON = new ObjectMapper().writeValueAsString(vehicle);
                //jedis.jsonSetLegacy(key, vehicle);  // This appears to marshall the vehicle, for storage
                jedis.set(key, vehicleAsJSON);  // This appears to marshall the vehicle, for storage
            }
            catch (Exception e) {
                //
            }
        }

        // jedis..jsonSet("vehicle:111", truck);
        // log.info("client ->", client);
        // Object fred = jedis.jsonGet("111");
        // log.info("fred ->", fred);

        // System.out.println(client.jsonGet("vehicle:111"));
        // client.set("planets", "Venus");
        // System.out.println(client.get("planets"));

        jedis.close();
        // pool.close();
    }

}
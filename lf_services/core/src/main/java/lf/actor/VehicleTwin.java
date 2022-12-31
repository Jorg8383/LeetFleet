package lf.actor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lf.message.FleetManagerMsg;
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
     * Return a copy of the current vehicle state to the FleetManager
     */
    public final static class RequestVehicleModel implements Message, LFSerialisable {
        public long query_id;
        public final ActorRef<FleetManagerMsg.Message> replyTo;
        public RequestVehicleModel(
            @JsonProperty("query_id") long query_id,
            @JsonProperty("replyTo") ActorRef<FleetManagerMsg.Message> replyTo)
        {
            this.query_id = query_id;
            this.replyTo = replyTo;
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

    private char WDQ = 34;  // Double Quotes (decimal)
    //private char WDQ = '\u0022';  // Double Quotes (unicode)

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
            try {
                String vehicleAsJSON = jedis.get(key);
                getContext().getLog().debug("\t value returned from redis -> " + vehicleAsJSON);
                // Unmarshall the JSON into a Vehicle object using the Jackson object mapper
                vehicle = new ObjectMapper().readValue(vehicleAsJSON, Vehicle.class);
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
        Vehicle newState = message.vehicle;

if (vehicle != null) { getContext().getLog().info("on WoT update current door status" + vehicle.getDoorStatus()); }
getContext().getLog().info("on WoT update new door status" + newState.getDoorStatus());

        // If this is a brand new actor... then this.vehicle will be null at this point
        newState = updateExposedThing(newState);

        // Update the vehicle in this actor (we just completely overwrite state.
        // 'Cause... why not... toy system.)...
        this.vehicle = newState;

        // ... then update the REDIS model
        updateRedisModel(this.vehicle.getVehicleIdLong());
        // Toy system - no action take if key does not exist.

        return this;
    }

    /**
     * The web client has requested a change in state.  Update the vehicle model
     * in redis and request a change to WoT by calling the WoT URL directly.
     */
    private Behavior<Message> onWebUpdate(WebUpdate message) {
        Vehicle newState = message.vehicle;

        newState = updateExposedThing(newState);

        // Update the vehicle in this actor (we just completely overwrite state.
        // 'Cause... why not... toy system.)...
        this.vehicle = newState;

        // ... then update the REDIS model
        updateRedisModel(this.vehicle.getVehicleIdLong());
        // Toy system - no action take if key does not exist.

        return this;
    }

    /**
     * We can receive vehicle state messages from the WoT or from the web Client.
     * Those state messages might include a request to update the state of the WoT
     * Thing itself (for example if we assign it a fleet Id, or if the client has
     * requested that we lock the vehicle doors).  Those updates are performed
     * here (part of the workaround as we don't have direct access to the consumed
     * thing in the AKKA actor system).
     * @param newState
     */
    private Vehicle updateExposedThing(Vehicle newState) {
        // Supported Actions:
        // UPDATE FLEET MANAGER ID:
        if (newState.isWotFltIdUpdateRqd()) {
            // Toggle the update flag off again. Not a requirement.
            newState.setWotFltIdUpdateRqd(false);

            // Update the WoT Exposed Thing
            // We are not proud. We planned to do this on a java WoT object.
            // But this approach will suffice for the toy system:
            try {
                getContext().getLog().info(
                    "LeetFleet: Remotely setting the FleetId for vehicle "
                    + newState.getVehicleId() + " to " + newState.getFleetId());

                String urlAsString = newState.getTdURL() + "properties/propFleetId";
                getContext().getLog().debug("URL to update Fleet Manager ID is -> " + urlAsString);
                URL url = new URL(urlAsString);
                String method = "PUT";
                String requestBody = WDQ + newState.getFleetId() + WDQ;  // JSON value must be quote delimited
                setWoTPropertyDirectly(url, method, requestBody);
            }
            catch (IOException ioe) {
                getContext().getLog().error("Fleet ID Update failure: " + ioe.getLocalizedMessage());
            }
        }
        // LOCK /UNLOCK DOORS:
        if (vehicle != null && !vehicle.getDoorStatus().equals(newState.getDoorStatus())) {
            // Update the WoT Exposed Thing
            // We are not proud. We planned to do this on a java WoT object.
            // But this approach will suffice for the toy system:
            try {
                getContext().getLog().info(
                    "LeetFleet: Remotely changing the doors for vehicle "
                    + newState.getVehicleId() + " to: " + newState.getDoorStatus());

                String urlAsString;
                if (newState.getDoorStatus().equalsIgnoreCase("LOCKED") ) {
                    // New door status is locked...
                    urlAsString = newState.getTdURL() + "actions/actionLockDoor";
                }
                else {
                    // New door status is Unlocked...
                    urlAsString = newState.getTdURL() + "actions/actionUnlockDoor";
                }
                getContext().getLog().debug("URL to for Door State Toggle is -> " + urlAsString);
                URL url = new URL(urlAsString);
                String method = "POST";
                setWoTPropertyDirectly(url, method, null);

            }
            catch (IOException ioe) {
                getContext().getLog().error("Door Lock Toggle failure: " + ioe.getLocalizedMessage());
            }
        }

        return newState;
    }

    /**
     * Call the appropriate action URL on the WoT object directly
     *
     * @param url
     * @throws IOException
     * @throws ProtocolException
     */
    private void setWoTPropertyDirectly(URL url, String method, String requestBody)
    throws IOException, ProtocolException
    {
        // Create a HttpURLConnection with the URL object - a new connection is
        // opened every time by calling the openConnection method of the protocol
        // handler for this URL *** this is where the connection is opened ***
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        // We send using method="POST" for actions, method="PUT" for setting properties.
        con.setRequestMethod(method);
        if (requestBody != null) {
            getContext().getLog().debug("** requestBody for transmission is:" + requestBody);

            // Enable output on the connection so we can write the JSON payload.
            con.setDoOutput(true);

            // Define the request body content.
            // This is where we find out if the connection was successfully
            // established. If an I/O error occurs we'll see an IOException.
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
                // Close the output stream (releasing any resources held)
                // We've now sent all the data.
                // Only the outputStream is closed at this point, not the actual connection
                os.close();
            }
            catch (Exception e) {
                getContext().getLog().error("Failed writing output stream to WoT: " + e.getLocalizedMessage());
            }
        }

        int responseCode = con.getResponseCode();
        getContext().getLog().debug("Response Code from Exposed Thing was: " + responseCode);

        String line;
        StringBuffer response = new StringBuffer();

        List<Integer> successCodes = new ArrayList<Integer>(){{
                add(HttpURLConnection.HTTP_OK);
                add(HttpURLConnection.HTTP_CREATED);
                add(HttpURLConnection.HTTP_ACCEPTED);
                add(HttpURLConnection.HTTP_NO_CONTENT);  // Setting a property returns '204'
            }};

        // if there is a response code AND that response code is an OK '200' range code, do stuff...
        if (successCodes.contains(responseCode)) {
            getContext().getLog().debug("Response Code was HTTP_OK!!");
            // We don't really do much with the response content. But logging it
            // can be informative.
            BufferedReader inStrm = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = inStrm.readLine()) != null) {
                response.append(line);
            }
            inStrm.close();
        }
        else {
            // Exposed Thing returned HTTP error code - log it:
            getContext().getLog().error("Attempt to access Exposed Thing over HTTP Failed.");
            BufferedReader errStrm = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            while ((line = errStrm.readLine()) != null) {
                response.append(line);
            }
            errStrm.close();
        }
        getContext().getLog().debug("Response Body: " + response.toString());
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
     * When a copy of the vehicle model is requested, supply it!
     * @param message
     * @return
     */
    private Behavior<Message> onRequestVehicleModel(RequestVehicleModel message) {
        getContext().getLog().debug("Vehicle response to state query (query ID: " + message.query_id + ")");
        message.replyTo.tell(new FleetManagerMsg.VehicleModelResponse(message.query_id, this.vehicle));
        return this;
    }

}
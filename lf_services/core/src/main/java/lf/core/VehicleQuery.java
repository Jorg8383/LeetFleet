package lf.core;

import java.util.ArrayList;
import java.util.List;

import akka.actor.typed.ActorRef;
import lf.message.WebPortalMsg;
import lf.model.Vehicle;

/**
 * Basic (serialisable) representation of Fleet.
 */
public class VehicleQuery {

    public long query_id;  // query_id
    public ActorRef<WebPortalMsg.VehicleListToWebP> portalRef;  // query_id

    public Object timer_key;  // key for this queries timer
    public long expected_query_size;  // no of expected responses to this query

    public List<Vehicle> vehicles = new ArrayList<Vehicle>();

    public VehicleQuery(
        long query_id, ActorRef<WebPortalMsg.VehicleListToWebP> portalRef,
        Object timer_key, long expected_query_size)
    {
        this.query_id = query_id;
        this.portalRef = portalRef;
        this.timer_key = timer_key;
        this.expected_query_size = expected_query_size;
    }

}
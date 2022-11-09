import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
//import akka.testkit.TestKit;
import akka.testkit.javadsl.TestKit;
import service.actor.Quoter;
import service.girlpower.GPQService;
import service.core.ClientInfo;
import service.message.InitQuotationService;
import service.message.QuotationRequest;
import service.message.QuotationResponse;

public class GirlpowerTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testQuoter() {
        // Typically, you create an actor by calling getContext().actorOf(). Rather than creating a
        // “freestanding” actor, this injects the new actor as a child into an already existing tree:
        // the creator actor becomes the parent of the newly created child actor.
        // New actor instances can be created under the 'User guardian' actor using system.actorOf()
        //
        // Props is a configuration object using in creating an Actor;
        // it is immutable, so it is thread-safe and fully shareable

        ActorRef quoterRef = system.actorOf(Props.create(Quoter.class), "test");

        TestKit probe = new TestKit(system);
        quoterRef.tell(new InitQuotationService(new GPQService()), null);
        quoterRef.tell(new QuotationRequest(1,
            new ClientInfo("Niki Collier", ClientInfo.FEMALE, 43, 0, 5, "PQR254/1")),
            probe.getRef());
        probe.awaitCond(probe::msgAvailable);
        probe.expectMsgClass(QuotationResponse.class);
    }

}
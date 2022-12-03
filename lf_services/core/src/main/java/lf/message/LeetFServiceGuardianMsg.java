package lf.message;

/**
 * Messages understood by the LeetFleet Service Guardians
 */
public class LeetFServiceGuardianMsg {

    // MESSAGES:
    //
    // It is a good practice to put an actor’s associated messages as static
    // classes in the AbstractBehavior’s class. This makes it easier to understand
    // what type of messages the actor expects and handles.
    //
    // BUT! For our Guardians we wont a common BootStrap message to avoid
    //      duplicated code
    public final static class BootStrap {
        public final String note;

        public BootStrap(String note) {
            this.note = note;
        }
    }

}
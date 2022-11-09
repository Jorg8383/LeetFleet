package service.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuotationHarnessConfig {
    private static final Logger log = LogManager.getLogger(QuotationHarnessConfig.class);

    // Give host/port sensible defaults...
    public String brokerPath = "akkap://default@127.0.0.1:2551/user/broker";

    public QuotationHarnessConfig(String[] args) {
        configFromArgs(args);
    }

    /**
     *
     * @param args
     * @return hostPortConfig - a populated HostPortConfig object
     */
    public void configFromArgs(String[] args) {
        // Check the command line args for manual host/port configuration
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-b":
                    brokerPath = "akka://default@" + args[++i] + ":2551/user/broker";
                    break;
                default:
                    System.out.println("Unknown flag: " + args[i] + "\n");
                    System.out.println("Valid flags are:");
                    System.out.println("\t-b <broker-host>\tSpecify the hostname for the broker service");
                    System.exit(0);
            }
        }
        log.info("BrokerConfig:: brokerPath:" + brokerPath);

        return;
    }

}

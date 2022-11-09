package service.core;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractQuotationService implements QuotationService {
    private static final Logger log = LogManager.getLogger(AbstractQuotationService.class);

    private int counter = 1000;
    private Random random = new Random();

    protected String generateReference(String prefix) {
        String ref = prefix;
        int length = 100000;
        while (length > 1000) {
            if (counter / length == 0) ref += "0";
            length = length / 10;
        }
        return ref + counter++;
    }

    protected double generatePrice(double min, int range) {
        double price = min + (double) random.nextInt(range);
        log.debug("Random price generated: " + String.valueOf(price));
        return price;
    }
}

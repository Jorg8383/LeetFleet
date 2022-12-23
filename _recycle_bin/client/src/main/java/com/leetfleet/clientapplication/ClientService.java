package com.leetfleet.clientapplication;


import com.leetfleet.models.DeliveryRequest;
import com.leetfleet.models.DeliveryResponse;
import com.leetfleet.models.Vehicle;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class ClientService {

    // TODO Sort out url later - just mocking what it might look like
    public static DeliveryResponse makeDelivery(DeliveryRequest deliveryRequest) {

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<DeliveryRequest> request = new HttpEntity<>(deliveryRequest);
        DeliveryResponse response = restTemplate.postForObject("http://localhost:8080" +
                "/delivery",
                request,
                DeliveryResponse.class);
        return response;
    }

    public static int mileage() {

        RestTemplate restTemplate = new RestTemplate();
        Vehicle responseVehicle = restTemplate.getForObject("http://localhost:8080" +
                "/mileage", Vehicle.class);

        return responseVehicle.getMileage();
    }

    public static Map<String, Integer> vehicleStatus() {

        RestTemplate restTemplate = new RestTemplate();
        Vehicle responseVehicle = restTemplate.getForObject("http://localhost:8080" +
                "/status", Vehicle.class);
        Map<String, Integer> fieldValues = new HashMap<>();
        fieldValues.put("Mileage", responseVehicle.getMileage());
        fieldValues.put("Oil Level", responseVehicle.getOilLevel());
        fieldValues.put("Tyre Pressure", responseVehicle.getTyrePressure());
        return fieldValues;
    }

    public static void doorLocks() {

        RestTemplate restTemplate = new RestTemplate();
        Vehicle responseVehicle = restTemplate.getForObject("http://localhost:8080" +
                "/doorLock", Vehicle.class);
        responseVehicle.changeDoorsLocked();
    }
}

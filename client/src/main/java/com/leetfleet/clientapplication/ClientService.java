package com.leetfleet.clientapplication;


import com.leetfleet.models.DeliveryRequest;
import com.leetfleet.models.DeliveryResponse;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

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
}

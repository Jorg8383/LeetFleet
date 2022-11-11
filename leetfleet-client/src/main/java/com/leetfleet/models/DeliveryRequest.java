package com.leetfleet.models;

public class DeliveryRequest {

    public DeliveryRequest() {}

    public DeliveryRequest(long reference) {
        this.reference = reference;
    }

    private long reference;

    public long getReference() {
        return reference;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }
}

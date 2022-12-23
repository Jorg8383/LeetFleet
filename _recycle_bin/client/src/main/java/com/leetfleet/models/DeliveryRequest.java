package com.leetfleet.models;

public class DeliveryRequest {

    public DeliveryRequest() {}

    public DeliveryRequest(long reference) {
        this.reference = reference;
    }

    private long reference;
    private String address;
    private String content;

    public long getReference() {
        return reference;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }
}

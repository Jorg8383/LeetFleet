package com.leetfleet.models;

public class DeliveryResponse {

    private long reference;
    private String address;
    private String content;
    private boolean successful;

    public DeliveryResponse() {}

    public DeliveryResponse(long reference) {
        this.reference = reference;
    }

    public DeliveryResponse(long reference,
                            String address,
                            String content,
                            boolean successful) {
        this.reference = reference;
        this.address = address;
        this.content = content;
        this.successful = successful;
    }

    public long getReference() {
        return reference;
    }

    public void setReference(long reference) {
        this.reference = reference;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}

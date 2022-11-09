package service.message;

import service.core.ClientInfo;

public class QuotationRequest implements MySerialisable {
    private long id;
    private ClientInfo clientInfo;

    public QuotationRequest() {
    }

    public QuotationRequest(long id, ClientInfo clientInfo) {
        this.id = id;
        this.clientInfo = clientInfo;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

}
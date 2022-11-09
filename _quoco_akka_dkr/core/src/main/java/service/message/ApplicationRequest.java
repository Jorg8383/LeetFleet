package service.message;

import service.core.ClientInfo;

public class ApplicationRequest implements MySerialisable {
    private ClientInfo clientInfo;

    public ApplicationRequest() {
    }

    public ApplicationRequest(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

}
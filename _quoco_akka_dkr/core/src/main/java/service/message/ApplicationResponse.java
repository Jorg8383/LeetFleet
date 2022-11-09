package service.message;

import java.util.ArrayList;
import java.util.List;

import service.core.ClientInfo;
import service.core.Quotation;

public class ApplicationResponse implements MySerialisable {
    private ClientInfo clientInfo;
    private List<Quotation> quotations;

    public ApplicationResponse() {
    }

    public ApplicationResponse(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
        this.quotations = new ArrayList<Quotation>();
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public List<Quotation> getQuotations() {
        return quotations;
    }
    public void setQuotations(List<Quotation> quotations) {
        this.quotations = quotations;
    }

}
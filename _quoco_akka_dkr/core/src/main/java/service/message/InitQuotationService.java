package service.message;

import service.core.QuotationService;

public class InitQuotationService implements MySerialisable {
    private QuotationService service;

    public InitQuotationService() {
    }

    // When this message is constructed, it will contain a valid quotation service.
    public InitQuotationService(QuotationService quotationService) {
        this.service = quotationService;
    }

    public QuotationService getService() {
        return service;
    }
    public void setService(QuotationService service) {
        this.service = service;
    }

}
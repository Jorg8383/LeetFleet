package service.message;

import service.core.Quotation;

public class QuotationResponse implements MySerialisable {
    private long id;
    private Quotation quotation;

    public QuotationResponse() {
    }

    public QuotationResponse(long id, Quotation quotation) {
        this.id = id;
        this.quotation = quotation;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public Quotation getQuotation() {
        return quotation;
    }
    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }

}
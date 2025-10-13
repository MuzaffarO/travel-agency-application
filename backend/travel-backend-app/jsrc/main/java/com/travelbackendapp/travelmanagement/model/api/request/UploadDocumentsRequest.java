package com.travelbackendapp.travelmanagement.model.api.request;

import java.util.List;

public class UploadDocumentsRequest {

    public List<PaymentDocument> payments;
    public List<GuestDocuments> guestDocuments;

    public static class PaymentDocument {
        public String fileName;                // e.g. "Payment receipt 1.pdf"
        public String type;                    // e.g. "pdf", "jpg", "png"
        public String base64encodedDocument;   // base64 data (NO data: prefix)
    }

    public static class GuestDocuments {
        public String userName;                // e.g. "John Doe"
        public List<GuestDoc> documents;
    }

    public static class GuestDoc {
        public String fileName;
        public String type;
        public String base64encodedDocument;
    }
}

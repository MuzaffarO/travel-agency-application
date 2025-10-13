package com.travelbackendapp.travelmanagement.model.entity;

public class DocumentRecord {
    private String bookingId;        // PK
    private String docId;            // SK: e.g. 1695848312345#PAYMENT#sha8#file
    private String s3Key;

    private String category;         // PAYMENT | PASSPORT
    private String guestName;        // nullable (for PAYMENT)
    private String fileName;         // original filename
    private String contentType;
    private Long   sizeBytes;
    private String sha256;

    private Long   uploadedAtEpoch;  // ms
    private String uploadedBy;       // CUSTOMER | TRAVEL_AGENT

    public DocumentRecord() {}

    // ---- getters & setters ----
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public Long getUploadedAtEpoch() { return uploadedAtEpoch; }
    public void setUploadedAtEpoch(Long uploadedAtEpoch) { this.uploadedAtEpoch = uploadedAtEpoch; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}

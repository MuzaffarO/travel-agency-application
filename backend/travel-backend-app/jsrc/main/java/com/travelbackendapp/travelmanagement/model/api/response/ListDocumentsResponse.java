package com.travelbackendapp.travelmanagement.model.api.response;

import java.util.List;

public class ListDocumentsResponse {

    public List<FileRef> payments;
    public List<GuestGroup> guestDocuments;

    public static class FileRef {
        public String id;
        public String fileName;
        public String fileUrl;

        public FileRef() {}

        public FileRef(String id, String fileName) {
            this.id = id;
            this.fileName = fileName;
        }

        public FileRef(String id, String fileName, String fileUrl) {
            this.id = id;
            this.fileName = fileName;
            this.fileUrl = fileUrl;
        }
    }

    public static class GuestGroup {
        public String userName;
        public List<FileRef> documents;

        public GuestGroup() {}

        public GuestGroup(String userName, List<FileRef> documents) {
            this.userName = userName;
            this.documents = documents;
        }
    }

    public ListDocumentsResponse() {}

    public ListDocumentsResponse(List<FileRef> payments, List<GuestGroup> guestDocuments) {
        this.payments = payments;
        this.guestDocuments = guestDocuments;
    }
}

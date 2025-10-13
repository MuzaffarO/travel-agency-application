package com.travelbackendapp.travelmanagement.repository;

import com.travelbackendapp.travelmanagement.model.entity.DocumentRecord;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DocumentsRepository {

    private final DynamoDbClient ddb;
    private final String table;

    @Inject
    public DocumentsRepository(DynamoDbClient dynamoDbClient,
                               @Named("DOCUMENTS_TABLE") String tableName) {
        this.ddb = dynamoDbClient;
        this.table = tableName;
    }

    public void put(DocumentRecord r) {
        Map<String, AttributeValue> item = new HashMap<>();

        // PK + SK
        item.put("bookingId", AttributeValue.builder().s(r.getBookingId()).build());
        item.put("docId",     AttributeValue.builder().s(r.getDocId()).build());

        if (r.getS3Key() != null)           item.put("s3Key", AttributeValue.builder().s(r.getS3Key()).build());
        if (r.getCategory() != null)        item.put("category", AttributeValue.builder().s(r.getCategory()).build());
        if (r.getGuestName() != null)       item.put("guestName", AttributeValue.builder().s(r.getGuestName()).build());
        if (r.getFileName() != null)        item.put("fileName", AttributeValue.builder().s(r.getFileName()).build());
        if (r.getContentType() != null)     item.put("contentType", AttributeValue.builder().s(r.getContentType()).build());
        if (r.getSizeBytes() != null)       item.put("sizeBytes", AttributeValue.builder().n(Long.toString(r.getSizeBytes())).build());
        if (r.getSha256() != null)          item.put("sha256", AttributeValue.builder().s(r.getSha256()).build());
        if (r.getUploadedAtEpoch() != null) item.put("uploadedAtEpoch", AttributeValue.builder().n(Long.toString(r.getUploadedAtEpoch())).build());
        if (r.getUploadedBy() != null)      item.put("uploadedBy", AttributeValue.builder().s(r.getUploadedBy()).build());

        ddb.putItem(PutItemRequest.builder().tableName(table).item(item).build());
    }

    /** Query all docs for a booking, newest first (docId begins with epoch millis). */
    public List<DocumentRecord> listByBookingId(String bookingId) {
        QueryRequest qr = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("bookingId = :b")
                .expressionAttributeValues(Map.of(":b", AttributeValue.builder().s(bookingId).build()))
                .scanIndexForward(false) // descending by SK (docId)
                .build();

        QueryResponse resp = ddb.query(qr);
        if (resp.items() == null || resp.items().isEmpty()) return List.of();

        return resp.items().stream().map(DocumentsRepository::toEntity).collect(Collectors.toList());
    }

    public DocumentRecord get(String bookingId, String docId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("bookingId", AttributeValue.builder().s(bookingId).build());
        key.put("docId",     AttributeValue.builder().s(docId).build());

        GetItemResponse res = ddb.getItem(GetItemRequest.builder()
                .tableName(table).key(key).consistentRead(true).build());
        if (res.item() == null || res.item().isEmpty()) return null;

        Map<String, AttributeValue> m = res.item();
        DocumentRecord r = new DocumentRecord();
        r.setBookingId(bookingId);
        r.setDocId(docId);
        if (m.containsKey("s3Key"))            r.setS3Key(m.get("s3Key").s());
        if (m.containsKey("category"))         r.setCategory(m.get("category").s());
        if (m.containsKey("guestName"))        r.setGuestName(m.get("guestName").s());
        if (m.containsKey("fileName"))         r.setFileName(m.get("fileName").s());
        if (m.containsKey("contentType"))      r.setContentType(m.get("contentType").s());
        if (m.containsKey("sizeBytes"))        r.setSizeBytes(Long.parseLong(m.get("sizeBytes").n()));
        if (m.containsKey("sha256"))           r.setSha256(m.get("sha256").s());
        if (m.containsKey("uploadedAtEpoch"))  r.setUploadedAtEpoch(Long.parseLong(m.get("uploadedAtEpoch").n()));
        if (m.containsKey("uploadedBy"))       r.setUploadedBy(m.get("uploadedBy").s());
        return r;
    }

    public void delete(String bookingId, String docId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("bookingId", AttributeValue.builder().s(bookingId).build());
        key.put("docId",     AttributeValue.builder().s(docId).build());

        ddb.deleteItem(DeleteItemRequest.builder().tableName(table).key(key).build());
    }

    private static DocumentRecord toEntity(Map<String, AttributeValue> m) {
        DocumentRecord r = new DocumentRecord();
        r.setBookingId(getS(m,"bookingId"));
        r.setDocId(getS(m,"docId"));
        r.setS3Key(getS(m,"s3Key"));
        r.setCategory(getS(m,"category"));
        r.setGuestName(getS(m,"guestName"));
        r.setFileName(getS(m,"fileName"));
        r.setContentType(getS(m,"contentType"));
        r.setSizeBytes(getN(m,"sizeBytes"));
        r.setSha256(getS(m,"sha256"));
        r.setUploadedAtEpoch(getN(m,"uploadedAtEpoch"));
        r.setUploadedBy(getS(m,"uploadedBy"));
        return r;
    }

    private static String getS(Map<String, AttributeValue> m, String k) {
        AttributeValue v = m.get(k);
        return v == null ? null : v.s();
    }

    private static Long getN(Map<String, AttributeValue> m, String k) {
        AttributeValue v = m.get(k);
        try {
            return v == null ? null : Long.parseLong(v.n());
        } catch (Exception e) {
            return null;
        }
    }
}

package com.travelbackendapp.travelmanagement.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface BookingsService {
    APIGatewayProxyResponseEvent create(APIGatewayProxyRequestEvent event);
    APIGatewayProxyResponseEvent view(APIGatewayProxyRequestEvent event);
    APIGatewayProxyResponseEvent update(APIGatewayProxyRequestEvent event, String bookingId);
    APIGatewayProxyResponseEvent cancel(APIGatewayProxyRequestEvent event, String bookingId);
    APIGatewayProxyResponseEvent confirm(APIGatewayProxyRequestEvent event, String bookingId);
    APIGatewayProxyResponseEvent uploadDocuments(APIGatewayProxyRequestEvent event, String bookingId);
    APIGatewayProxyResponseEvent listDocuments(APIGatewayProxyRequestEvent event, String bookingId);
    APIGatewayProxyResponseEvent deleteDocument(APIGatewayProxyRequestEvent event, String bookingId, String documentId);

}

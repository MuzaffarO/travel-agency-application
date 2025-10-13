package com.travelbackendapp.travelmanagement.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface ToursService {
    APIGatewayProxyResponseEvent getAvailableTours(APIGatewayProxyRequestEvent event);
    APIGatewayProxyResponseEvent getDestinations(APIGatewayProxyRequestEvent event);
    APIGatewayProxyResponseEvent getTourDetails(APIGatewayProxyRequestEvent event, String tourId);
    APIGatewayProxyResponseEvent getTourReviews(APIGatewayProxyRequestEvent event, String tourId);
    APIGatewayProxyResponseEvent postTourReview(APIGatewayProxyRequestEvent event, String tourId);
}

package com.travelbackendapp.travelmanagement.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface TravelAgentsService {
    APIGatewayProxyResponseEvent createTravelAgent(APIGatewayProxyRequestEvent event);
    APIGatewayProxyResponseEvent listTravelAgents(APIGatewayProxyRequestEvent event);
    APIGatewayProxyResponseEvent deleteTravelAgent(APIGatewayProxyRequestEvent event, String email);
}


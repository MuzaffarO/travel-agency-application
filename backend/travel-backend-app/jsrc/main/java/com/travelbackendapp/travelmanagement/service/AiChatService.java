// service/AiChatService.java
package com.travelbackendapp.travelmanagement.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public interface AiChatService {
    APIGatewayProxyResponseEvent chat(APIGatewayProxyRequestEvent event);
}

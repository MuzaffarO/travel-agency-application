package com.travelbackendapp;


import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.model.RetentionSetting;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.travelbackendapp.travelmanagement.di.DaggerAppComponent;
import com.travelbackendapp.travelmanagement.routing.RequestRouter;

import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID;
import static com.syndicate.deployment.model.environment.ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;


import javax.inject.Inject;

@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${pool_name}")
@LambdaHandler(
        lambdaName = "travel-api-handler",
        roleName = "travel-api-handler-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables({
        @EnvironmentVariable(key = "table_name", value = "${target_table}"),
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "reviews_table", value = "${reviews_table}"),
        @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${pool_name}", valueTransformer = USER_POOL_NAME_TO_USER_POOL_ID),
        @EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${pool_name}", valueTransformer = USER_POOL_NAME_TO_CLIENT_ID),
        @EnvironmentVariable(key = "travel_agent_table_name", value = "${travel_agent_table_name}"),
        @EnvironmentVariable(key = "bookings_table", value = "${bookings_table}"),
        @EnvironmentVariable(key = "BOOKING_EVENTS_QUEUE_URL", value = "${booking_events_queue_url}"),
        @EnvironmentVariable(key = "BOOKING_DOCS_BUCKET", value = "${booking-documents-bucket}"),
        @EnvironmentVariable(key = "documents_table", value = "${documents_table}"),
        @EnvironmentVariable(key = "GEMINI_API_KEY", value = "${gemini_api_key}"),
        @EnvironmentVariable(key = "GEMINI_MODEL", value = "${gemini_model}"),
        @EnvironmentVariable(key = "AVATARS_BUCKET", value = "${avatars_bucket}")

})

public class TravelApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger log = LoggerFactory.getLogger(TravelApiHandler.class);

    @Inject RequestRouter router;

    public TravelApiHandler() {
        DaggerAppComponent.create().inject(this);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String reqId = context != null ? context.getAwsRequestId() : "n/a";
        try {
            log.info("REQ {} path={} stage={} qs={}",
                    reqId,
                    event != null ? event.getPath() : "null",
                    event != null && event.getRequestContext()!=null ? event.getRequestContext().getStage() : "null",
                    event != null ? event.getQueryStringParameters() : null
            );

            APIGatewayProxyResponseEvent resp = router.route(event, context);

            log.info("RES {} status={}", reqId, resp != null ? resp.getStatusCode() : -1);
            return resp;
        } catch (Exception e) {
            log.error("UNHANDLED {}: {}", reqId, e.getMessage(), e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"internal server error\"}");
        }
    }
}





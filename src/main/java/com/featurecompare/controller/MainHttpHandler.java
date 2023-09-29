package com.featurecompare.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featurecompare.config.MainInjector;
import com.featurecompare.exception.ControllerException;
import com.featurecompare.exception.NotFoundException;
import com.featurecompare.exception.RequiredFieldException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


public class MainHttpHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public final Injector INJECTOR;
    public final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final ObjectMapper objectMapper;
    private final ItemController ruleController;

    public MainHttpHandler() {
        this(Guice.createInjector(new MainInjector.MainModule()));
    }

    public MainHttpHandler(Injector injector) {
        INJECTOR = injector;
        ruleController = INJECTOR.getInstance(ItemController.class);
        objectMapper = INJECTOR.getInstance(ObjectMapper.class);
        dynamoDbEnhancedClient = INJECTOR.getInstance(DynamoDbEnhancedClient.class);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            String resource = event.getResource(); // /widgets   /widgets{id}

            if (event.getHttpMethod().equals("OPTIONS")) {
                return createResponse("OK", 200);
            }

            String methodAndResource = event.getHttpMethod() + " " + resource;
            logger.log("Received [%s]".formatted(methodAndResource));

            return switch (methodAndResource) {
                case "POST /items" -> createResponse(ruleController.post(event), 201);
                case "GET /items/{locale}/{id}" -> createResponse(ruleController.get(event), 200);
                case "PATCH /items/{locale}/{id}" -> createResponse(ruleController.patch(event), 200);
                case "DELETE /items/{locale}/{id}" -> createResponse(ruleController.delete(event), 204);
                case "GET /items/{locale}" -> createResponse(ruleController.getAll(event), 200);
                case "GET /debug" -> createResponse(Map.of("methodAndResource", methodAndResource, "event", event), 200);
                default -> throw new NotFoundException(methodAndResource + " not defined in " + this.getClass());
            };
        } catch (Throwable e) {
            logger.log("ERROR %s".formatted(e));

            int code = 500;
            if (e instanceof IllegalArgumentException) {
                code = 400;
            }
            if (e instanceof RequiredFieldException) {
                code = 400;
            }
            if (e instanceof NotFoundException) {
                code = 404;
            }
            if (e instanceof ControllerException ce) {
                code = ce.getHTTPCode();
            }

            String stackTraceClassAndLine = Arrays.stream(e.getStackTrace())
                    .map(traceItem -> traceItem.getClassName() + ":" + traceItem.getLineNumber()).limit(5)
                    .collect(Collectors.joining("\n"));

            logger.log(stackTraceClassAndLine);

            try {
                return createResponse(Map.of(
                        "error", e.getMessage(),
                        "errorString", e,
                        "stacktrace", stackTraceClassAndLine
                ), code);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    private APIGatewayProxyResponseEvent createResponse(Object body, int code) throws JsonProcessingException {
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.setHeaders(Collections.singletonMap("timeStamp", String.valueOf(System.currentTimeMillis())));
        apiGatewayProxyResponseEvent.setStatusCode(200);

        apiGatewayProxyResponseEvent.setBody(objectMapper.writeValueAsString(body));
        apiGatewayProxyResponseEvent.setStatusCode(code);
        apiGatewayProxyResponseEvent.setHeaders(Map.of("Content-Type", "application/json", "Access-Control-Allow-Methods", "DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT", "Access-Control-Allow-Headers", "Content-Type,Authorization", "Access-Control-Allow-Origin", "*", "Access-Control-Max-Age", "600"));

        return apiGatewayProxyResponseEvent;
    }

}

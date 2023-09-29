package io.github.elvisciotti.common.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.HashMap;

public class ApiGatewayRequestMock extends APIGatewayProxyRequestEvent {

    public ApiGatewayRequestMock() {
        setHeaders(new HashMap<>());
        setPathParameters(new HashMap<>());
        setQueryStringParameters(new HashMap<>());
    }

    public ApiGatewayRequestMock(String httpMethod, String resource) {
        this();
        setHttpMethod(httpMethod);
        setResource(resource);
    }

    public ApiGatewayRequestMock setBodyFluent(String body) {
        setBody(body);
        return this;
    }

    public ApiGatewayRequestMock addBearerToken(String authToken) {
        getHeaders().put("Authorization", "Bearer " + authToken);

        return this;
    }

    public ApiGatewayRequestMock addPathParameter(String k, String v) {
        getPathParameters().put(k, v);
        return this;
    }

    public ApiGatewayRequestMock addQueryParameter(String k, String v) {
        getQueryStringParameters().put(k, v);
        return this;
    }
}

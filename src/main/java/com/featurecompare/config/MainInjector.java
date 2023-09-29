package com.featurecompare.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.net.http.HttpClient;

public class MainInjector {
    public static class MainModule extends AbstractModule {
        private final InjectorOptions options;

        public MainModule() {
            this(new InjectorOptions(false, -1));
        }

        public MainModule(InjectorOptions options) {
            this.options = options;
        }


        @Provides
        @Singleton
        public DynamoDbClient dynamoDbClient() {
            DynamoDbClientBuilder builder = DynamoDbClient.builder()
                    .region(Constants.DYNAMO_REGION);
            if (!options.useProdEnv()) {
                URI uri = URI.create("http://127.0.0.1:%d".formatted(options.dynamoPort()));
                builder.endpointOverride(uri);
            }

            return builder.build();
        }

        @Provides
        @Singleton
        public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
            return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
        }

        @Provides
        @Singleton
        public HttpClient httpClient() {
            return HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();
        }

        @Provides
        @Singleton
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .registerModule(new JavaTimeModule());
        }

    }
}

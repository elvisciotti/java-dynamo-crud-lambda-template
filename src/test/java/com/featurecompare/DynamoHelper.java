package com.featurecompare;

import com.featurecompare.dao.Item;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Slf4j
public class DynamoHelper {
    public static final int DYNAMO_PORT = 8000;
    public static int dynamoMappedPort;
    public static GenericContainer<?> container = new GenericContainer<>("amazon/dynamodb-local:latest")
            .withExposedPorts(DYNAMO_PORT);

    static {
        container.start();
        dynamoMappedPort = container.getMappedPort(8000);
    }

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbClient dynamoDbClient;

    public DynamoHelper(DynamoDbEnhancedClient enhancedClient, DynamoDbClient dynamoDbClient) {
        this.enhancedClient = enhancedClient;
        this.dynamoDbClient = dynamoDbClient;
    }

    void recreateTable(String tableName, Class<Item> modelClass) {
        DynamoDbTable<Item> table = enhancedClient.table(tableName, TableSchema.fromBean(modelClass));

        try {
            table.deleteTable();
        } catch (RuntimeException e) {
            log.warn("already deleted");
        }
        table
                .createTable(builder -> builder.provisionedThroughput(b -> b
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                );

        try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) { // DynamoDbWaiter is Autocloseable
            ResponseOrException<DescribeTableResponse> response = waiter
                    .waitUntilTableExists(builder -> builder.tableName(tableName).build())
                    .matched();
            response.response()
                    .orElseThrow(
                            () -> new RuntimeException("Table was not created."));
        }

    }
}

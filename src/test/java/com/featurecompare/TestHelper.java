package com.featurecompare;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.featurecompare.config.Constants;
import com.featurecompare.config.InjectorOptions;
import com.featurecompare.config.MainInjector;
import com.featurecompare.controller.MainHttpHandler;
import com.featurecompare.dao.Item;
import com.featurecompare.dao.ItemService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.elvisciotti.common.lambda.ApiGatewayRequestMock;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Setter
public class TestHelper {
    public static final InjectorOptions INJECTOR_OPTIONS = InjectorOptions.builder()
            .useProdEnv(false)
            .dynamoPort(DynamoHelper.dynamoMappedPort)
            .build();
    private static final Injector INJECTOR_FOR_TESTS = Guice.createInjector(new MainInjector.MainModule(INJECTOR_OPTIONS));
    private final Context context;
    private final LambdaLogger logger = mock(LambdaLogger.class);
    private DynamoHelper dynamoHelper;

    public TestHelper() {
        context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        dynamoHelper = new DynamoHelper(
                INJECTOR_FOR_TESTS.getInstance(DynamoDbEnhancedClient.class),
                INJECTOR_FOR_TESTS.getInstance(DynamoDbClient.class)
        );
        dynamoHelper.recreateTable(Constants.FC_EXT_SIMILAR_TABLE, Item.class);
    }

    public <T> T getService(Class<T> c) {
        return INJECTOR_FOR_TESTS.getInstance(c);
    }

    public APIGatewayProxyResponseEvent handleRequest(ApiGatewayRequestMock req) throws Exception {
        return new MainHttpHandler(INJECTOR_FOR_TESTS).handleRequest(req, context);
    }

    public void wipeAllByLocale(String locale) {
        ItemService itemService = getService(ItemService.class);

        itemService.findByLocaleOrderIdDesc(locale)
                .forEach(itemService::delete);
    }


}

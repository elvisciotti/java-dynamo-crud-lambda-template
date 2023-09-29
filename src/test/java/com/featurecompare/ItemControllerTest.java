package com.featurecompare;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.featurecompare.dao.Item;
import com.featurecompare.dao.ItemService;
import com.featurecompare.exception.NotFoundException;
import io.github.elvisciotti.common.lambda.ApiGatewayRequestMock;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemControllerTest {
    public static final String LOCALE = "en";
    public static final String ID = "asin1";

    private final TestHelper testHelper = new TestHelper();
    private final  ItemService ruleService = testHelper.getService(ItemService.class);
    private String token = "currentToken";
    private Item existingItem;

    @BeforeEach
    void setUp() {
        existingItem = Item.builder()
                .locale(LOCALE)
                .id(ID)
                .extMode("amazon")
                .title("title")
                .url("url")
                .price("$123")
                .starsCount(123)
                .stars(4.5f)
                .updatedAt(LocalDateTime.now())
                .addedAt(LocalDateTime.now())
                .build();

        ruleService.create(existingItem);
    }


    @Test
    void post() throws Exception {
        ApiGatewayRequestMock req = new ApiGatewayRequestMock("POST", "/items")
                .addBearerToken(token).setBodyFluent("""
                        {
                            "locale": "%s",
                            "id": "asin2",
                            "url": "url2",
                            "title": "title2",
                            "price": "$ 4.5",
                            "stars": 4.5,
                            "starsCount": 123
                        }
                        """.formatted(LOCALE));
        var response = testHelper.handleRequest(req);

        assertEquals(201, response.getStatusCode());
        assertThat(response.getBody(),
                jsonEquals("""
                        {"id": "asin2", "locale": "%s"}
                        """.formatted(LOCALE)).when(Option.IGNORING_EXTRA_FIELDS)
                        .when(Option.IGNORING_EXTRA_ARRAY_ITEMS));


        Item fromDb = testHelper.getService(ItemService.class).findOneOrThrowException(LOCALE, "asin2");

        assertEquals("url2", fromDb.getUrl());
        assertEquals("$ 4.5", fromDb.getPrice());
        assertEquals(4.5f, fromDb.getStars());
        assertEquals(123, fromDb.getStarsCount());

        testHelper.wipeAllByLocale(LOCALE);
    }


    @Test
    void get() throws Exception {
        ApiGatewayRequestMock req = new ApiGatewayRequestMock("GET", "/items/{locale}/{id}")
                .addBearerToken(token)
                .addPathParameter("locale", LOCALE)
                .addPathParameter("id", ID);


        APIGatewayProxyResponseEvent response = testHelper.handleRequest(req);
        assertEquals(200, response.getStatusCode());

        assertThat(response.getBody(),
                jsonEquals("""
                        {"id": "asin1", "locale": "%s"}
                        """.formatted(LOCALE)).when(Option.IGNORING_EXTRA_FIELDS)
                        .when(Option.IGNORING_EXTRA_ARRAY_ITEMS));

    }

    @Test
    void patch() throws Exception {
        String newrUrl = "url-changed";
        ApiGatewayRequestMock req = new ApiGatewayRequestMock("PATCH", "/items/{locale}/{id}").addBearerToken(token)
                .setBodyFluent("""
                        {"url": "%s"}
                        """.formatted(newrUrl))
                .addPathParameter("locale", LOCALE)
                .addPathParameter("id", ID);
        APIGatewayProxyResponseEvent apiGatewayResponse = testHelper.handleRequest(req);
        assertEquals(200, apiGatewayResponse.getStatusCode());

        Item refreshedItem = ruleService.findOneOrThrowException(existingItem);
        assertEquals(newrUrl, refreshedItem.getUrl());
    }


    @Test
    void getAll() throws Exception {
        ApiGatewayRequestMock req = new ApiGatewayRequestMock("GET", "/items/{locale}")
                .addPathParameter("locale", LOCALE)
                .addBearerToken(token);
        APIGatewayProxyResponseEvent response = testHelper.handleRequest(req);
        assertEquals(200, response.getStatusCode());

        assertThat(response.getBody(),
                jsonEquals("""
                        [{"id": "asin1"}]
                        """).when(Option.IGNORING_EXTRA_FIELDS)
                        .when(Option.IGNORING_EXTRA_ARRAY_ITEMS));
    }


    @Test
    void delete() throws Exception {
        assertEquals(existingItem.getId(), ruleService.findOneOrThrowException(existingItem.getLocale(), existingItem.getId()).getId());
        ApiGatewayRequestMock req = new ApiGatewayRequestMock("DELETE", "/items/{locale}/{id}")
                .addBearerToken(token)
                .addPathParameter("locale", LOCALE)
                .addPathParameter("id", ID);
        APIGatewayProxyResponseEvent apiGatewayResponse = testHelper.handleRequest(req);
        assertEquals(204, apiGatewayResponse.getStatusCode());
        assertThrows(NotFoundException.class, () -> ruleService.findOneOrThrowException(existingItem));
    }

    @AfterEach
    void tearDown() {
        testHelper.wipeAllByLocale(LOCALE);
    }


}
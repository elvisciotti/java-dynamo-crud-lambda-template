package com.featurecompare.controller;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featurecompare.dao.Item;
import com.featurecompare.dao.ItemService;
import com.featurecompare.exception.ControllerException;
import com.featurecompare.exception.NotFoundException;
import com.featurecompare.exception.RequiredFieldException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ItemController {

    private final ItemService itemService;
    private final ObjectMapper objectMapper;

    @Inject
    public ItemController(ItemService itemService, ObjectMapper objectMapper) {
        this.itemService = itemService;
        this.objectMapper = objectMapper;
    }


    public Object post(APIGatewayProxyRequestEvent input) throws IOException, ControllerException, RequiredFieldException {

        Item newItem = objectMapper.readValue(input.getBody(), Item.class);

        return itemService.create(newItem);
    }

    public Object patch(APIGatewayProxyRequestEvent input) throws NotFoundException, RequiredFieldException, JsonProcessingException {

        String locale = Optional.ofNullable(input.getPathParameters().get("locale")).orElseThrow(() -> new RequiredFieldException("locale not set"));
        String id = Optional.ofNullable(input.getPathParameters().get("id")).orElseThrow(() -> new RequiredFieldException("id not set"));

        Item existingItem = itemService.findOneOrThrowException(locale, id);

        Map<String, String> map = objectMapper.readValue(input.getBody(), Map.class);
        String url = map.getOrDefault("url", null);
        if (url != null) {
            existingItem.setUrl(url);
        }


        return itemService.patch(existingItem);
    }


    public Object get(APIGatewayProxyRequestEvent input) throws IOException, ControllerException, NotFoundException, RequiredFieldException {
        String locale = Optional.ofNullable(input.getPathParameters().get("locale")).orElseThrow(() -> new RequiredFieldException("locale not set"));
        String id = Optional.ofNullable(input.getPathParameters().get("id")).orElseThrow(() -> new RequiredFieldException("id not set"));

        return itemService.findOneOrThrowException(locale, id);
    }

    public List<Item> getAll(APIGatewayProxyRequestEvent input) throws RequiredFieldException {
        String locale = Optional.ofNullable(input.getPathParameters().get("locale")).orElseThrow(() -> new RequiredFieldException("locale not set"));

        return itemService.findByLocaleOrderIdDesc(locale);
    }

    public String delete(APIGatewayProxyRequestEvent input) throws IOException, ControllerException, RequiredFieldException {
        String locale = Optional.ofNullable(input.getPathParameters().get("locale")).orElseThrow(() -> new RequiredFieldException("locale not set"));
        String id = Optional.ofNullable(input.getPathParameters().get("id")).orElseThrow(() -> new RequiredFieldException("id not set"));

        itemService.delete(locale, id);

        return "create";
    }

}

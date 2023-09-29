package com.featurecompare;

import com.featurecompare.dao.Item;
import com.featurecompare.dao.ItemRepository;
import org.junit.jupiter.api.Test;

class ItemRepositoryTest {
    TestHelper testHelper = new TestHelper();
    ItemRepository sut = testHelper.getService(ItemRepository.class);

    @Test
    void testCrud() {
        String locale = "en";

        testHelper.wipeAllByLocale(locale);

        Item item = Item.builder().locale("en").id("asin1").build();
        sut.upsert(item);

        // here custom assertions can be done
    }
}
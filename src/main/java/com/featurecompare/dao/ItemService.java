package com.featurecompare.dao;

import com.featurecompare.exception.NotFoundException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;

    @Inject
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item findOneOrThrowException(Item item) throws NotFoundException {
        return itemRepository.findOneByPkAndSk(item.getLocale(), item.getId()).orElseThrow(NotFoundException::new);
    }

    public Item findOneOrThrowException(String locale, String id) throws NotFoundException {
        return itemRepository.findOneByPkAndSk(locale, id).orElseThrow(NotFoundException::new);
    }

    public List<Item> findByLocaleOrderIdDesc(String locale) {
        return itemRepository.findByPkOrderedSkDesc(locale)
                .items().stream()
                .collect(Collectors.toList());
    }

    public Item create(Item record) {
        itemRepository.upsert(record);

        return record;
    }

    public Item patch(Item record) {
        return itemRepository.patch(record);
    }

    public void put(Item record) {
        prePersistChecksAndDefaultValues(record);

        itemRepository.upsert(record);
    }

    private void prePersistChecksAndDefaultValues(Item record) {
        if (record.getLocale() == null || record.getId() == null) {
            throw new IllegalArgumentException("Cannot update a widget without PK and SK defined");
        }
    }


    public void delete(Item item) {
        delete(item.getLocale(), item.getId());
    }

    public void delete(String locale, String id) {
        itemRepository.deleteByPkAndSk(locale, id);
    }

}

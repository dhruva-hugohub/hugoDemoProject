package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.dateItemPrice.DateItemPriceEntity;

public interface DayItemPriceDAO {

    DateItemPriceEntity addRecord(DateItemPriceEntity dayItemPriceEntity);

    DateItemPriceEntity editRecord(DateItemPriceEntity dayItemPriceEntity);

    Optional<DateItemPriceEntity> getRecord(String metalId, String date, long providerId);

    List<DateItemPriceEntity> fetchRecords(DateItemPriceEntity dayItemPriceEntity,
                                           String sortField, String sortOrder, Integer page, Integer size);

}

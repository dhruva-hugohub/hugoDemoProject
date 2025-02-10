package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.constants.ResourceConstants;
import com.hugo.demo.dao.DayItemPriceDAO;
import com.hugo.demo.dateItemPrice.DateItemPriceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheManager = ResourceConstants.BEAN_CACHE_MANAGER_REDIS, cacheNames = ResourceConstants.CACHE_NAME_DATE_ITEM)
public class DateItemPriceFacade {

    private final DayItemPriceDAO dayItemPriceDAO;

    @Autowired
    public DateItemPriceFacade(DayItemPriceDAO dayItemPriceDAO) {
        this.dayItemPriceDAO = dayItemPriceDAO;
    }

    @Cacheable(value = "dateItemPrice", key = "#dayItemPriceEntity.metalId + #dayItemPriceEntity.providerId", unless="#result == null")
    public DateItemPriceEntity addRecord(DateItemPriceEntity dayItemPriceEntity) {
        return dayItemPriceDAO.addRecord(dayItemPriceEntity);
    }

    @Cacheable(value = "dateItemPrice", key = "#dayItemPriceEntity.metalId + #dayItemPriceEntity.providerId", unless="#result == null")
    public DateItemPriceEntity editRecord(DateItemPriceEntity dayItemPriceEntity) {
        return dayItemPriceDAO.editRecord(dayItemPriceEntity);
    }

    @Cacheable(value = "dateItemPrice", key = "#metalId + #providerId + #date", unless="#result == null")
    public Optional<DateItemPriceEntity> getRecord(String metalId, String date, long providerId) {
        return dayItemPriceDAO.getRecord(metalId, date, providerId);
    }

    @Cacheable(value = "dateItemPrice", key = "#dayItemPriceEntity.metalId + #dayItemPriceEntity.providerId", unless="#result == null")
    public List<DateItemPriceEntity> fetchRecords(DateItemPriceEntity dayItemPriceEntity,
                                                  String sortField, String sortOrder, Integer page, Integer size) {
        return dayItemPriceDAO.fetchRecords(dayItemPriceEntity,
            sortField, sortOrder, page, size);
    }
}

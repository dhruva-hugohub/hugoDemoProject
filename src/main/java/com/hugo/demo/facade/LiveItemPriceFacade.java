package com.hugo.demo.facade;

import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.constants.ResourceConstants;
import com.hugo.demo.dao.LiveItemPriceDAO;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;
import com.hugo.demo.liveItemPrice.LiveItemPriceFilter;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheManager = ResourceConstants.BEAN_CACHE_MANAGER_REDIS, cacheNames = ResourceConstants.CACHE_NAME_LIVE_ITEM)
public class LiveItemPriceFacade {

    private final LiveItemPriceDAO liveItemPriceDAO;

    @Autowired
    public LiveItemPriceFacade(LiveItemPriceDAO liveItemPriceDAO) {
        this.liveItemPriceDAO = liveItemPriceDAO;
    }

    @Cacheable(value = "liveItemPrice", key = "#entity.metalId + #entity.providerId", unless="#result == null")
    public LiveItemPriceEntity addItemRecord(LiveItemPriceEntity entity) {
        return liveItemPriceDAO.addItemRecord(entity);
    }


    @CachePut(value = "liveItemPrice", key = "#entity.metalId + #entity.providerId + #entity.dateTime", unless="#result == null")
    public LiveItemPriceEntity editItemRecord(LiveItemPriceEntity entity) {
        return liveItemPriceDAO.editItemRecord(entity);
    }

    @Cacheable(value = "liveItemPrice", key = "#id + #providerId + #dateTime", unless="#result == null")
    public Optional<LiveItemPriceEntity> fetchItemRecordTime(String id, Timestamp dateTime, long providerId) {
        return liveItemPriceDAO.fetchItemRecordTime(id, dateTime, providerId);
    }

    @Cacheable(value = "liveItemPrice", key = "#id + #providerId + #dateTime", unless="#result == null")
    public Optional<LiveItemPriceEntity> fetchItemRecordByDate(String id, Timestamp dateTime, long providerId) {
        return liveItemPriceDAO.fetchItemRecordByDate(id, dateTime, providerId);
    }

    public PaginatedLiveItemPrice fetchLiveItemPrices(LiveItemPriceFilter filter){
        return liveItemPriceDAO.fetchLiveItemPrices(filter);
    }
}

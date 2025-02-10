package com.hugo.demo.dao;

import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;
import com.hugo.demo.liveItemPrice.LiveItemPriceFilter;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;

public interface LiveItemPriceDAO {

    LiveItemPriceEntity addItemRecord(LiveItemPriceEntity entity);

    LiveItemPriceEntity editItemRecord(LiveItemPriceEntity entity);

    Optional<LiveItemPriceEntity> fetchItemRecordTime(String id, Timestamp dateTime, long providerId);

    Optional<LiveItemPriceEntity> fetchItemRecordByDate(String id, Timestamp dateTime, long providerId);

    PaginatedLiveItemPrice fetchLiveItemPrices(LiveItemPriceFilter filter);
}

package com.hugo.demo.dao;

import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;

public interface LiveItemPriceDAO {

    LiveItemPriceEntity addItemRecord(LiveItemPriceEntity entity);

    LiveItemPriceEntity editItemRecord(LiveItemPriceEntity entity);

    Optional<LiveItemPriceEntity> fetchItemRecordTime(String id, Timestamp dateTime);

    Optional<LiveItemPriceEntity> fetchItemRecordByDate(String id, Timestamp dateTime);
}

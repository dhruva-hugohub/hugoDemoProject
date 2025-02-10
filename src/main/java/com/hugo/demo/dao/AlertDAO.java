package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.alert.AlertEntity;

public interface AlertDAO {

    Optional<AlertEntity> fetchItemDetails(long userId, String metalId, long providerId);

    AlertEntity editItemRecord(AlertEntity alertEntity);

    AlertEntity addItemRecord(AlertEntity alertEntity);

    List<AlertEntity> getAlertByUserId(long userId);

    List<AlertEntity> getAlertByProviderId(long providerId);

    List<AlertEntity> getAlertByMetalId(String metalId);
}

package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.alert.AlertEntity;
import com.hugo.demo.dao.AlertDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertFacade {

    private final AlertDAO alertDAO;

    @Autowired
    public AlertFacade(AlertDAO alertDAO) {
        this.alertDAO = alertDAO;
    }

    public Optional<AlertEntity> fetchItemDetails(long userId, String metalId, long providerId) {
        return alertDAO.fetchItemDetails(userId, metalId, providerId);
    }

    public AlertEntity editItemRecord(AlertEntity alertEntity) {
        return alertDAO.editItemRecord(alertEntity);
    }

    public AlertEntity addItemRecord(AlertEntity alertEntity) {
        return alertDAO.addItemRecord(alertEntity);
    }

    public List<AlertEntity> getAlertByUserId(long userId) {
        return alertDAO.getAlertByUserId(userId);
    }

    public List<AlertEntity> getAlertByProviderId(long providerId) {
        return alertDAO.getAlertByProviderId(providerId);
    }

    public List<AlertEntity> getAlertByMetalId(String metalId) {
        return alertDAO.getAlertByMetalId(metalId);
    }
}

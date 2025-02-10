package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.hugo.demo.alert.AlertEntity;
import com.hugo.demo.dao.AlertDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.enums.alertType.TypeOfAlert;
import com.hugo.demo.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AlertDAOImpl implements AlertDAO {
    private static final Logger logger = LoggerFactory.getLogger(AlertDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public AlertDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public AlertEntity addItemRecord(AlertEntity alertEntity) {
        try {

            String sqlInsert = SQLQueryConstants.ADD_ALERT;
            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", alertEntity.getUserId())
                .addValue("email", alertEntity.getEmail())
                .addValue("FcmToken", alertEntity.getFcmToken())
                .addValue("minPrice", alertEntity.getMinPrice())
                .addValue("maxPrice", alertEntity.getMaxPrice())
                .addValue("typeOfAlert", alertEntity.getTypeOfAlert().name())
                .addValue("expirationDate", alertEntity.getDate())
                .addValue("providerId", alertEntity.getProviderId())
                .addValue("metalId", alertEntity.getMetalId());

            executeUpdate(sqlInsert, params, "Alert Couldn't be created");
            return fetchItemDetails(alertEntity.getUserId(), alertEntity.getMetalId(), alertEntity.getProviderId())
                .orElseThrow(() -> new RuntimeException("Item not found after adding"));
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Alert Couldn't be created", e);
        }
    }


    @Override
    public AlertEntity editItemRecord(AlertEntity alertEntity) {
        StringBuilder query = buildUpdateUserBaseQuery();
        boolean hasPrevious = false;

        if (!alertEntity.getEmail().isEmpty()) {
            query.append("email = :email");
            hasPrevious = true;
        }
        if (!alertEntity.getFcmToken().isEmpty()) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("FcmToken = :FcmToken");
            hasPrevious = true;
        }
        if (alertEntity.getMinPrice() != 0.00) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("minPrice = :minPrice");
        }
        if (alertEntity.getMaxPrice() != 0.00) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("maxPrice = :maxPrice");
        }
        if (!alertEntity.getTypeOfAlert().toString().isEmpty()) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("typeOfAlert = :typeOfAlert");
        }
        query.append(" WHERE userId = :userId AND metalId = :metalId AND providerId = :providerId");

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", alertEntity.getUserId())
            .addValue("email", alertEntity.getEmail())
            .addValue("FcmToken", alertEntity.getFcmToken())
            .addValue("minPrice", alertEntity.getMinPrice())
            .addValue("maxPrice", alertEntity.getMaxPrice())
            .addValue("typeOfAlert", alertEntity.getTypeOfAlert().name())
            .addValue("providerId", alertEntity.getProviderId())
            .addValue("metalId", alertEntity.getMetalId());

        try {
            executeUpdate(query.toString(), params, "Failed to edit item");

            return fetchItemDetails(alertEntity.getUserId(), alertEntity.getMetalId(), alertEntity.getProviderId())
                .orElseThrow(() -> new RuntimeException("Item not found after editing"));
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to edit item", e);
        }
    }

    @Override
    public Optional<AlertEntity> fetchItemDetails(long userId, String metalId, long providerId) {
        StringBuilder sql = buildFindUserBaseQuery();
        sql.append(" userId = :userId AND ");
        sql.append(" metalId = :metalId AND ");
        sql.append(" providerId = :providerId");

        return getAlertEntity(userId, metalId, providerId, sql.toString());
    }


    @Override
    public List<AlertEntity> getAlertByUserId(long userId) {
        StringBuilder sql = buildFindUserBaseQuery();
        sql.append(" userId = :userId");
        sql.append(" AND expirationDate >= :expirationDate");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("expirationDate", System.currentTimeMillis());
        return fetchAlertEntityList(sql.toString(), params);
    }

    @Override
    public List<AlertEntity> getAlertByProviderId(long providerId) {
        StringBuilder sql = buildFindUserBaseQuery();
        sql.append(" providerId = :providerId");
        sql.append(" AND expirationDate >= :expirationDate");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("providerId", providerId)
            .addValue("expirationDate", System.currentTimeMillis());
        return fetchAlertEntityList(sql.toString(), params);
    }

    @Override
    public List<AlertEntity> getAlertByMetalId(String metalId) {
        StringBuilder sql = buildFindUserBaseQuery();
        sql.append(" metalId = :metalId");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", metalId);
        return fetchAlertEntityList(sql.toString(), params);
    }

    private List<AlertEntity> fetchAlertEntityList(String query, MapSqlParameterSource params) {
        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> mapRowToAlertEntity(rs));
    }


    private StringBuilder buildFindUserBaseQuery() {
        return new StringBuilder(SQLQueryConstants.FETCH_ALERT_BASE_QUERY);
    }

    private StringBuilder buildUpdateUserBaseQuery() {
        return new StringBuilder(SQLQueryConstants.UPDATE_ALERT_BASE_QUERY);
    }

    private Optional<AlertEntity> getAlertEntity(long userId, String metalId, long providerId, String sql) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("metalId", metalId)
            .addValue("providerId", providerId);

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToAlertEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching Alert", e);
            return Optional.empty();
        }
    }

    private void executeUpdate(String sql, MapSqlParameterSource params, String errorMessage) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected == 0) {
                throw new InternalServerErrorException(errorMessage);
            }

        } catch (Exception e) {
            throw new InternalServerErrorException(errorMessage, e);
        }
    }


    private AlertEntity mapRowToAlertEntity(ResultSet rs) throws SQLException {
        return AlertEntity.newBuilder()
            .setUserId(rs.getInt("userId"))
            .setEmail(rs.getString("email"))
            .setProviderId(rs.getInt("providerId"))
            .setMetalId(rs.getString("metalId"))
            .setTypeOfAlert(TypeOfAlert.valueOf(rs.getString("typeOfAlert")))
            .setDate(rs.getString("expirationDate"))
            .setMaxPrice(rs.getDouble("maxPrice"))
            .setMinPrice(rs.getDouble("minPrice"))
            .setFcmToken(rs.getString("FcmToken"))
            .build();
    }

}

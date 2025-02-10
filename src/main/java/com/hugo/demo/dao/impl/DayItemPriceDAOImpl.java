package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.hugo.demo.dao.DayItemPriceDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.dateItemPrice.DateItemPriceEntity;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DayItemPriceDAOImpl implements DayItemPriceDAO {

    private static final Logger logger = LoggerFactory.getLogger(DayItemPriceDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DayItemPriceDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public DateItemPriceEntity addRecord(DateItemPriceEntity dayItemPriceEntity) {
        String sql = SQLQueryConstants.ADD_DATE_ITEM_RECORD;

        MapSqlParameterSource params =
            new MapSqlParameterSource().addValue("metalId", dayItemPriceEntity.getMetalId())
                .addValue("providerId", dayItemPriceEntity.getProviderId())
                .addValue("date", dayItemPriceEntity.getDate())
                .addValue("openPrice", dayItemPriceEntity.getOpen())
                .addValue("closePrice", dayItemPriceEntity.getClose())
                .addValue("highPrice", dayItemPriceEntity.getHigh())
                .addValue("lowPrice", dayItemPriceEntity.getLow());

        try {
            executeUpdate(sql, params, "Failed to add item");

            return getRecord(dayItemPriceEntity.getMetalId(), dayItemPriceEntity.getDate(), dayItemPriceEntity.getProviderId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to add item", e);
        }
    }

    @Override
    public DateItemPriceEntity editRecord(DateItemPriceEntity dayItemPriceEntity) {
        StringBuilder query = buildUpdateDayItemBaseQuery();
        boolean hasPrevious = false;

        MapSqlParameterSource params =
            new MapSqlParameterSource();
        if (dayItemPriceEntity.getOpen() != 0.00) {
            query.append("openPrice = :openPrice");
            params.addValue("openPrice", dayItemPriceEntity.getOpen());
            hasPrevious = true;
        }
        if (dayItemPriceEntity.getClose() != 0.00) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("closePrice = :closePrice");
            params.addValue("closePrice", dayItemPriceEntity.getClose());
            hasPrevious = true;
        }
        if (dayItemPriceEntity.getLow() != 0.00) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("lowPrice = :lowPrice");
            params.addValue("lowPrice", dayItemPriceEntity.getLow());
            hasPrevious = true;
        }
        if (dayItemPriceEntity.getHigh() != 0.00) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append("highPrice = :highPrice");
            params.addValue("highPrice", dayItemPriceEntity.getHigh());
        }
        query.append(" WHERE metalId = :metalId AND providerId = :providerId AND date = :date");


        params.addValue("providerId", dayItemPriceEntity.getProviderId());
        params.addValue("date", dayItemPriceEntity.getDate());
        params.addValue("metalId", dayItemPriceEntity.getMetalId());

        try {
            executeUpdate(query.toString(), params, "Failed to edit item");

            return getRecord(dayItemPriceEntity.getMetalId(), dayItemPriceEntity.getDate(), dayItemPriceEntity.getProviderId())
                .orElseThrow(() -> new RuntimeException("Item not found after editing"));
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to edit item", e);
        }
    }

    @Override
    public Optional<DateItemPriceEntity> getRecord(String metalId, String date, long providerId) {
        StringBuilder sql = buildFindDayItemBaseQuery();
        sql.append(" AND metalId = :metalId AND providerId = :providerId AND date = :date");
        return getDateItemPriceEntity(metalId, date, providerId, sql.toString());
    }

    private Optional<DateItemPriceEntity> getDateItemPriceEntity(String metalId, String date, long providerId, String sql) {
        MapSqlParameterSource params =
            new MapSqlParameterSource().addValue("metalId", metalId)
                .addValue("date", date)
                .addValue("providerId", providerId);

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToDateItemPriceEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching item record for metalId: {} and date: {}", metalId, date, e);
            return Optional.empty();
        }
    }

    @Override
    public List<DateItemPriceEntity> fetchRecords(DateItemPriceEntity dateItemPriceEntity, String sortField, String sortOrder, Integer page,
                                                  Integer size) {
        StringBuilder baseQuery = new StringBuilder(SQLQueryConstants.FETCH_DAY_ITEMS);

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (!dateItemPriceEntity.getMetalId().isEmpty()) {
            baseQuery.append(" AND metalId = :metalId");
            params.addValue("metalId", dateItemPriceEntity.getMetalId());
        }
        if (!dateItemPriceEntity.getDate().isEmpty()) {
            baseQuery.append(" AND date = :date");
            params.addValue("date", dateItemPriceEntity.getMetalId());
        }
        if (dateItemPriceEntity.getOpen() != 0.00) {
            baseQuery.append(" AND openPrice = :openPrice");
            params.addValue("openPrice", dateItemPriceEntity.getOpen());
        }
        if (dateItemPriceEntity.getClose() != 0.00) {
            baseQuery.append(" AND closePrice = :closePrice");
            params.addValue("closePrice", dateItemPriceEntity.getClose());
        }
        if (dateItemPriceEntity.getHigh() != 0.00) {
            baseQuery.append(" AND highPrice = :highPrice");
            params.addValue("highPrice", dateItemPriceEntity.getHigh());
        }
        if (dateItemPriceEntity.getLow() != 0.00) {
            baseQuery.append(" AND lowPrice = :lowPrice");
            params.addValue("lowPrice", dateItemPriceEntity.getLow());
        }

        return fetchRecordsWithPaginationAndSorting(baseQuery.toString(), params, sortField, sortOrder, page, size);
    }

    private List<DateItemPriceEntity> fetchRecordsWithPaginationAndSorting(String baseQuery, MapSqlParameterSource additionalParams, String sortField,
                                                                           String sortOrder, Integer page, Integer size) {
        sortField = (sortField == null || sortField.isEmpty()) ? "metalId" : sortField;
        sortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "ASC" : sortOrder.toUpperCase();

        String limitClause = "";
        if (page != null && size != null) {
            limitClause = "LIMIT :limit OFFSET :offset";
        }

        String query = String.format("%s ORDER BY %s %s %s", baseQuery, sortField, sortOrder, limitClause);

        if (!limitClause.isEmpty()) {
            int offset = (page - 1) * size;
            additionalParams.addValue("limit", size).addValue("offset", offset);
        }

        return namedParameterJdbcTemplate.query(query, additionalParams, (rs, rowNum) -> mapRowToDateItemPriceEntity(rs));
    }

    private StringBuilder buildFindDayItemBaseQuery() {
        return new StringBuilder(SQLQueryConstants.FETCH_DATE_ITEM_RECORD_BASE_QUERY);
    }

    private StringBuilder buildUpdateDayItemBaseQuery() {
        return new StringBuilder(SQLQueryConstants.UPDATE_DATE_ITEM_RECORD_BASE_QUERY);
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

    private DateItemPriceEntity mapRowToDateItemPriceEntity(ResultSet rs) throws SQLException {
        return DateItemPriceEntity.newBuilder().setMetalId(rs.getString("metalId"))
            .setProviderId(rs.getInt("providerId"))
            .setDate(rs.getString("date"))
            .setOpen(rs.getDouble("openPrice"))
            .setClose(rs.getDouble("closePrice"))
            .setHigh(rs.getDouble("highPrice"))
            .setLow(rs.getDouble("lowPrice"))
            .setCreatedAt(DateUtil.convertToProtoTimestamp(rs.getTimestamp("create_ts")))
            .setUpdatedAt(DateUtil.convertToProtoTimestamp(rs.getTimestamp("update_ts"))).build();
    }
}
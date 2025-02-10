package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.dao.LiveItemPriceDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;
import com.hugo.demo.liveItemPrice.LiveItemPriceFilter;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;
import com.hugo.demo.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LiveItemPriceDAOImpl implements LiveItemPriceDAO {
    private static final Logger logger = LoggerFactory.getLogger(LiveItemPriceDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public LiveItemPriceDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public LiveItemPriceEntity addItemRecord(LiveItemPriceEntity item) {
        try {
            logger.info("addItemRecord {}", item.getDateTime());

            Optional<LiveItemPriceEntity> existingItem = fetchItemRecordByDate(item.getMetalId(), item.getDateTime(), item.getProviderId());


            if (existingItem.isPresent()) {
                return editItemRecord(item);
            }

            String sqlInsert = SQLQueryConstants.ADD_LIVE_ITEM_PRICE;
            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                .addValue("metalId", item.getMetalId())
                .addValue("performance", item.getPerformance())
                .addValue("askValue", item.getAskValue())
                .addValue("bidValue", item.getBidValue())
                .addValue("value", item.getValue())
                .addValue("providerId", item.getProviderId())
                .addValue("dateTime", DateUtil.convertTimestampToString(item.getDateTime()));

            namedParameterJdbcTemplate.update(sqlInsert, insertParams);

            return fetchItemRecordByDate(item.getMetalId(), item.getDateTime(), item.getProviderId())
                .orElseThrow(() -> new RuntimeException("Item not found after adding"));
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Failed to add or edit item record", e);
        }
    }


    @Override
    public LiveItemPriceEntity editItemRecord(LiveItemPriceEntity item) {
        String sql = SQLQueryConstants.UPDATE_LIVE_ITEM_PRICE;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("performance", item.getPerformance())
            .addValue("askValue", item.getAskValue())
            .addValue("bidValue", item.getBidValue())
            .addValue("value", item.getValue())
            .addValue("metalId", item.getMetalId())
            .addValue("providerId", item.getProviderId())
            .addValue("dateTime", DateUtil.convertTimestampToString(item.getDateTime()));

        try {
            namedParameterJdbcTemplate.update(sql, params);

            return fetchItemRecordByDate(item.getMetalId(), item.getDateTime(), item.getProviderId())
                .orElseThrow(() -> new RuntimeException("Item not found after editing"));
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Failed to edit item record", e);
        }
    }


    @Override
    public Optional<LiveItemPriceEntity> fetchItemRecordTime(String metalId, Timestamp dateTime, long providerId) {
        String sql = SQLQueryConstants.FETCH_LIVE_ITEM_PRICE_BY_TIME;

        return getLiveItemPriceEntity(metalId, dateTime, providerId, sql);
    }

    @Override
    public Optional<LiveItemPriceEntity> fetchItemRecordByDate(String metalId, Timestamp dateTime, long providerId) {
        String sql = SQLQueryConstants.FETCH_LIVE_ITEM_PRICE_BY_DATE;

        return getLiveItemPriceEntity(metalId, dateTime, providerId, sql);
    }

    private Optional<LiveItemPriceEntity> getLiveItemPriceEntity(String metalId, Timestamp dateTime, long providerId, String sql) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", metalId)
            .addValue("providerId", providerId)
            .addValue("dateTime", DateUtil.convertTimestampToString(dateTime));

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToLiveItemPriceEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching item record for metalId: {} and dateTime: {}", metalId, dateTime, e);
            return Optional.empty();
        }
    }

    @Override
    public PaginatedLiveItemPrice fetchLiveItemPrices(LiveItemPriceFilter filter) {

        Map<String, Object> params = new HashMap<>();
        StringBuilder query = buildBaseQuery();
        StringBuilder countQuery = buildBaseCountQuery();

        query = applyChecksToFilter(filter, params, query);
        countQuery = applyChecksToFilter(filter, params, countQuery);

        Integer totalItems = getTotalItems(countQuery.toString(), params);

        if (filter.getPageSize() != 0 && filter.getPage() != 0) {
            query.append(" LIMIT :pageSize OFFSET :offset");
            params.put("pageSize", filter.getPageSize());
            params.put("offset", (filter.getPage() - 1) * filter.getPageSize());
        }

        int totalPages = calculateTotalPages(totalItems, filter.getPageSize());
        boolean hasPreviousPage = filter.getPage() > 1;
        boolean hasNextPage = filter.getPage() < totalPages;

        List<LiveItemPriceEntity> liveItemPrices = fetchLiveItemPricesFromDatabase(query.toString(), params);

        return PaginatedLiveItemPrice.newBuilder()
            .setTotalPages(totalPages)
            .setTotalItems(totalItems)
            .setHasPreviousPage(hasPreviousPage)
            .setHasNextPage(hasNextPage)
            .addAllLiveItemPrices(liveItemPrices)
            .build();
    }

    private StringBuilder applyChecksToFilter(LiveItemPriceFilter filter, Map<String, Object> params, StringBuilder query) {
        applyMetalFilter(filter.getMetalId(), query, params);
        applyProviderIdFilter(filter.getProviderId(), query, params);
        applyDateFilter(filter.getStartDate(), filter.getEndDate(), query, params);
        applySorting(filter.getSortBy(), query);
        return query;
    }
    private void applyMetalFilter(String metalId, StringBuilder query, Map<String, Object> params) {
        if (metalId != null && !metalId.isEmpty()) {
            query.append(" AND metal_id = :metalId");
            params.put("metalId", metalId);
        }
    }

    private void applyProviderIdFilter(Long providerId, StringBuilder query, Map<String, Object> params) {
        if (providerId != null && providerId > 0) {
            query.append(" AND provider_id = :providerId");
            params.put("providerId", providerId);
        }
    }


    private void applySorting(String sortBy, StringBuilder query) {
        if (sortBy != null && !sortBy.isEmpty()) {
            query.append(" ORDER BY ").append(sortBy);
        } else {
            query.append(" ORDER BY dateTime DESC");  // Default sorting by dateTime
        }
    }

    private void applyDateFilter(Timestamp startDate, Timestamp endDate, StringBuilder query, Map<String, Object> params) {
        if (startDate != null) {
            query.append(" AND dateTime >= :startDate");
            params.put("startDate", startDate);
        }
        if (endDate != null) {
            query.append(" AND dateTime <= :endDate");
            params.put("endDate", endDate);
        }
    }

    private List<LiveItemPriceEntity> fetchLiveItemPricesFromDatabase(String query, Map<String, Object> params) {
        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> mapRowToLiveItemPriceEntity(rs)
        );
    }

    private int getTotalItems(String countQuery, Map<String, Object> params) {
        return namedParameterJdbcTemplate.queryForObject(countQuery, params, Integer.class);
    }

    private int calculateTotalPages(int totalItems, int pageSize) {
        return (totalItems + pageSize - 1) / pageSize;
    }

    private StringBuilder buildBaseQuery() {
        return new StringBuilder("SELECT * FROM LiveItemPrice WHERE 1=1");
    }

    private StringBuilder buildBaseCountQuery() {
        return new StringBuilder("SELECT COUNT(*) FROM LiveItemPrice WHERE 1=1");
    }


    private LiveItemPriceEntity mapRowToLiveItemPriceEntity(ResultSet rs) throws SQLException {
        return LiveItemPriceEntity.newBuilder()
            .setMetalId(rs.getString("metalId"))
            .setProviderId(rs.getLong("providerId"))
            .setPerformance(rs.getDouble("performance"))
            .setAskValue(rs.getDouble("askValue"))
            .setBidValue(rs.getDouble("bidValue"))
            .setValue(rs.getDouble("value"))
            .setDateTime(DateUtil.convertToProtoTimestamp(rs.getTimestamp("dateTime")))
            .setCreatedAt(DateUtil.convertToProtoTimestamp(rs.getTimestamp("create_ts")))
            .setUpdatedAt(DateUtil.convertToProtoTimestamp(rs.getTimestamp("update_ts")))
            .build();
    }

}

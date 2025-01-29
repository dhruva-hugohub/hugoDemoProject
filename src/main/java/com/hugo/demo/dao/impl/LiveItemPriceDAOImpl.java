package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.dao.LiveItemPriceDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;
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

            Optional<LiveItemPriceEntity> existingItem = fetchItemRecordByDate(item.getMetalId(), item.getDateTime());


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
                .addValue("dateTime", DateUtil.convertTimestampToString(item.getDateTime()));

            namedParameterJdbcTemplate.update(sqlInsert, insertParams);

            return fetchItemRecordByDate(item.getMetalId(), item.getDateTime())
                .orElseThrow(() -> new RuntimeException("Item not found after adding"));
        } catch (DataAccessException e) {
            logger.error("Error occurred while adding or editing item record: {}", item, e);
            throw new RuntimeException("Failed to add or edit item record", e);
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
            .addValue("dateTime", DateUtil.convertTimestampToString(item.getDateTime()));

        try {
            namedParameterJdbcTemplate.update(sql, params);

            return fetchItemRecordByDate(item.getMetalId(), item.getDateTime())
                .orElseThrow(() -> new RuntimeException("Item not found after editing"));
        } catch (DataAccessException e) {
            logger.error("Error occurred while editing item record: {}", item, e);
            throw new RuntimeException("Failed to edit item record", e);
        }
    }


    @Override
    public Optional<LiveItemPriceEntity> fetchItemRecordTime(String metalId, Timestamp dateTime) {
        String sql = SQLQueryConstants.FETCH_LIVE_ITEM_PRICE_BY_TIME;

        return getLiveItemPriceEntity(metalId, dateTime, sql);
    }

    public Optional<LiveItemPriceEntity> fetchItemRecordByDate(String metalId, Timestamp dateTime) {
        String sql = SQLQueryConstants.FETCH_LIVE_ITEM_PRICE_BY_DATE;

        return getLiveItemPriceEntity(metalId, dateTime, sql);
    }

    private Optional<LiveItemPriceEntity> getLiveItemPriceEntity(String metalId, Timestamp dateTime, String sql) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("metalId", metalId)
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

    private LiveItemPriceEntity mapRowToLiveItemPriceEntity(ResultSet rs) throws SQLException {
        return LiveItemPriceEntity.newBuilder()
            .setMetalId(rs.getString("metalId"))
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

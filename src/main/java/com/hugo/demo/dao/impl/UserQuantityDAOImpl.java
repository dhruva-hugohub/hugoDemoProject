package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.dao.UserQuantityDAO;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.userquantity.UserQuantityEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserQuantityDAOImpl implements UserQuantityDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserQuantityDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserQuantityDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    @Override
    public Optional<UserQuantityEntity> findByMetalAndUserId(long userId, String metalId) {
        StringBuilder sql = buildBaseQuery();
        Map<String, Object> parameters = new HashMap<>();
        sql.append(" AND userId =:userId");
        sql.append(" AND metalId =:metalId");
        sql.append(" ORDER BY metalId DESC");
        parameters.put("userId", userId);
        parameters.put("metalId", metalId);
        return fetchUserQuantitiesFromDatabase(sql.toString(), parameters).stream().findFirst();
    }

    @Override
    public List<UserQuantityEntity> fetchQuantitesByUserId(long userId) {
        StringBuilder sql = buildBaseQuery();
        Map<String, Object> parameters = new HashMap<>();
        sql.append(" AND userId =:userId");
        sql.append(" ORDER BY userId DESC");
        parameters.put("userId", userId);
        return fetchUserQuantitiesFromDatabase(sql.toString(), parameters);
    }


    private StringBuilder buildBaseQuery() {
        return new StringBuilder(SQLQueryConstants.GET_USER_QUANTITY_BASE_QUERY);
    }

    private List<UserQuantityEntity> fetchUserQuantitiesFromDatabase(String query, Map<String, Object> params) {
        return namedParameterJdbcTemplate.query(query, params, (rs, rowNum) -> mapRowToUserQuantityEntity(rs));
    }

    @Override
    public boolean checkExistsByUser(String field, String value) {
        StringBuilder sql = new StringBuilder(SQLQueryConstants.COUNT_WALLET_BY_FIELD);
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (Objects.equals(field, "metalId")) {
            sql.append("u.metalId = :metalId");
            parameters.addValue("metalId", value);
        } else if (Objects.equals(field, "userId")) {
            sql.append("u.userId = :userId");
            parameters.addValue("userId", value);
        }

        String query = sql.toString();

        logger.info(query);
        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
            return count != null && count > 0;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Error occurred while trying to check " + field, e);
        }
    }

    @Override
    public UserQuantityEntity save(UserQuantityEntity userQuantityEntity) {
        String sql = SQLQueryConstants.INSERT_USER_QUANTITY;
        logger.info(sql);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userQuantityEntity.getUserId())
            .addValue("metalId", userQuantityEntity.getMetalId())
            .addValue("quantity", userQuantityEntity.getQuantity());

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return findByMetalAndUserId(userQuantityEntity.getUserId(), userQuantityEntity.getMetalId()).orElse(userQuantityEntity);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to save UserQuantity", e);
        }
    }

    @Override
    public UserQuantityEntity updateQuantity(UserQuantityEntity userQuantityEntity) {
        String sql = SQLQueryConstants.UPDATE_USER_QUANTITY;
        logger.info(sql);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userQuantityEntity.getUserId())
            .addValue("metalId", userQuantityEntity.getMetalId())
            .addValue("quantity", userQuantityEntity.getQuantity());

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return findByMetalAndUserId(userQuantityEntity.getUserId(), userQuantityEntity.getMetalId()).orElse(userQuantityEntity);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to save UserQuantity", e);
        }
    }

    private UserQuantityEntity mapRowToUserQuantityEntity(ResultSet rs) throws SQLException {
        return UserQuantityEntity.newBuilder()
            .setUserId(rs.getLong("userId"))
            .setMetalId(rs.getString("metalId"))
            .setQuantity(rs.getDouble("quantity"))
            .build();
    }
}

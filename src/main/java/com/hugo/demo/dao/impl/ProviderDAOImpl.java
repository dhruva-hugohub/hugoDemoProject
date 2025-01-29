package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.provider.ProviderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderDAOImpl implements ProviderDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProviderDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ProviderDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public ProviderEntity addProviderDetails(ProviderEntity provider) {
        String sql = SQLQueryConstants.ADD_PROVIDER;

        if (fetchProviderDetailsByName(provider.getProviderName()).isPresent()) {
            throw new RecordAlreadyExistsException(
                CommonStatusCode.BAD_REQUEST_ERROR,"Provider with name '" + provider.getProviderName() + "' already exists.");
        }

        MapSqlParameterSource params =
            new MapSqlParameterSource().addValue("providerName", provider.getProviderName()).addValue("providerAPIUrl", provider.getProviderAPIUrl());

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return provider;
        } catch (DataAccessException e) {
            logger.error("Error occurred while adding provider details: {}", provider, e);
            throw new RuntimeException("Failed to add provider details", e);
        }
    }

    @Override
    public Optional<ProviderEntity> fetchProviderDetails(int providerId) {
        String sql = SQLQueryConstants.FETCH_PROVIDER_DETAILS_BY_ID;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("providerId", providerId);

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToProviderEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching provider details for providerId: {}", providerId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProviderEntity> fetchProviderDetailsByName(String providerName) {
        String sql = SQLQueryConstants.FETCH_PROVIDER_DETAILS_BY_NAME;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("providerName", providerName);

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToProviderEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching provider details for providerName: {}", providerName, e);
            return Optional.empty();
        }
    }

    @Override
    public ProviderEntity editProviderDetails(ProviderEntity provider) {
        String sql = SQLQueryConstants.EDIT_PROVIDER_DETAILS;

        MapSqlParameterSource params =
            new MapSqlParameterSource().addValue("providerId", provider.getProviderId()).addValue("providerName", provider.getProviderName())
                .addValue("providerAPIUrl", provider.getProviderAPIUrl());

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected == 0) {
                throw new RuntimeException("No provider found with id: " + provider.getProviderId());
            }
            return provider;
        } catch (DataAccessException e) {
            logger.error("Error occurred while editing provider details: {}", provider, e);
            throw new RuntimeException("Failed to edit provider details", e);
        }
    }

    @Override
    public List<ProviderEntity> fetchAllProviderDetails() {
        String sql = SQLQueryConstants.FETCH_ALL_PROVIDERS;

        try {
            return namedParameterJdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProviderEntity(rs));
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching all provider details", e);
            throw new RuntimeException("Failed to fetch all provider details", e);
        }
    }

    @Override
    public boolean deleteProviderDetails(int providerId) {
        String sql = SQLQueryConstants.DELETE_PROVIDER;

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("providerId", providerId);

        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            logger.error("Error occurred while deleting provider details for providerId: {}", providerId, e);
            return false;
        }
    }

    private ProviderEntity mapRowToProviderEntity(ResultSet rs) throws SQLException {
        return ProviderEntity.newBuilder().setProviderId(rs.getInt("providerId")).setProviderName(rs.getString("providerName"))
            .setProviderAPIUrl(rs.getString("providerAPIUrl")).build();
    }

}

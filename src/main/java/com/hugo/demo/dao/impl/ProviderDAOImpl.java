package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.exception.RecordNotFoundException;
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

        if (isProviderExistsByName(provider.getProviderName())) {
            throw new RecordAlreadyExistsException(
                CommonStatusCode.DUPLICATE_RECORD_ERROR,
                "Provider with name '" + provider.getProviderName() + "' already exists."
            );
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("providerName", provider.getProviderName())
            .addValue("providerAPIUrl", provider.getProviderAPIUrl())
            .addValue("schedulerTimePeriod", provider.getSchedulerTimePeriod());

        try {
            executeUpdate(sql, params, "Failed to add provider details");
            return fetchProviderDetailsByName(provider.getProviderName()).orElse(provider);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to add provider details", e);
        }
    }

    @Override
    public ProviderEntity editProviderDetails(ProviderEntity provider) {
        String sql = SQLQueryConstants.EDIT_PROVIDER_DETAILS;

        if (!isProviderExistsById(provider.getProviderId())) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"No provider found with id: " + provider.getProviderId());
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("providerId", provider.getProviderId())
            .addValue("providerName", provider.getProviderName())
            .addValue("providerAPIUrl", provider.getProviderAPIUrl()).addValue("schedulerTimePeriod", provider.getSchedulerTimePeriod());

        try {
            executeUpdate(sql, params, "Failed to update provider details");
            return fetchProviderDetails(provider.getProviderId()).orElse(provider);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Unexpected error while updating provider details", e);
        }
    }

    private boolean isProviderExistsById(int providerId) {
        return fetchProviderDetails(providerId).isPresent();
    }

    private boolean isProviderExistsByName(String providerName) {
        return fetchProviderDetailsByName(providerName).isPresent();
    }

    @Override
    public Optional<ProviderEntity> fetchProviderDetails(int providerId) {
        return fetchProviderDetails(providerId, null);
    }

    @Override
    public Optional<ProviderEntity> fetchProviderDetailsByName(String providerName) {
        return fetchProviderDetails(null, providerName);
    }

    private Optional<ProviderEntity> fetchProviderDetails(Integer providerId, String providerName) {
        String sql;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (providerId != null) {
            sql = SQLQueryConstants.FETCH_PROVIDER_DETAILS_BY_ID;
            params.addValue("providerId", providerId);
        } else if (providerName != null) {
            sql = SQLQueryConstants.FETCH_PROVIDER_DETAILS_BY_NAME;
            params.addValue("providerName", providerName);
        } else {
            throw new IllegalArgumentException("Either providerId or providerName must be provided.");
        }

        try {
            return namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToProviderEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching provider details - ID: {}, Name: {}", providerId, providerName, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteProviderDetails(int providerId) {
        String sql = SQLQueryConstants.DELETE_PROVIDER;

        if (!isProviderExistsById(providerId)) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"No provider found with id: " + providerId);
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("providerId", providerId);

        try {
            return executeDelete(sql, params);
        } catch (Exception e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"No provider found with id: " + providerId);
        }
    }


    @Override
    public PaginatedProviders fetchProviders(String providerName, String sortBy, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("providerName", providerName != null ? "%" + providerName + "%" : null)
            .addValue("sortBy", sortBy != null ? sortBy : "providerId")
            .addValue("pageSize", pageSize)
            .addValue("offset", (page - 1) * pageSize);

        List<ProviderEntity> providers = fetchProvidersWithPagination(params);

        int totalItems = fetchTotalProviderCount(params);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        boolean hasNextPage = page < totalPages;
        boolean hasPreviousPage = page > 1;

        return PaginatedProviders.newBuilder()
            .addAllProviders(providers)
            .setHasNextPage(hasNextPage)
            .setHasPreviousPage(hasPreviousPage)
            .setTotalItems(totalItems)
            .setTotalPages(totalPages)
            .build();
    }


    private List<ProviderEntity> fetchProvidersWithPagination(MapSqlParameterSource params) {
        String sql = SQLQueryConstants.FETCH_PROVIDERS;

        try {
            return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> mapRowToProviderProto(rs));
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to fetch paginated providers", e);
        }
    }

    @Override
    public List<ProviderEntity> fetchAllProviders() {
        String sql = SQLQueryConstants.FETCH_ALL_PROVIDERS;

        try {
            return namedParameterJdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProviderProto(rs));
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to fetch paginated providers", e);
        }
    }


    private ProviderEntity mapRowToProviderProto(ResultSet rs) throws SQLException {
        return ProviderEntity.newBuilder()
            .setProviderId(rs.getInt("providerId"))
            .setProviderName(rs.getString("providerName"))
            .setProviderAPIUrl(rs.getString("providerAPIUrl"))
            .setSchedulerTimePeriod(rs.getString("schedulerTimePeriod"))
            .build();
    }


    private int fetchTotalProviderCount(MapSqlParameterSource params) {
        String sql = SQLQueryConstants.FETCH_PROVIDER_COUNT;

        try {
            return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to fetch provider count", e);
        }
        catch (NullPointerException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"Provider not found");
        }
    }


    private boolean executeDelete(String sql, MapSqlParameterSource params) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to delete provider", e);
        }
    }

    private ProviderEntity mapRowToProviderEntity(ResultSet rs) throws SQLException {
        return ProviderEntity.newBuilder().setProviderId(rs.getInt("providerId")).setProviderName(rs.getString("providerName"))
            .setProviderAPIUrl(rs.getString("providerAPIUrl")).setSchedulerTimePeriod(rs.getString("schedulerTimePeriod")).build();
    }

    private void executeUpdate(String sql, MapSqlParameterSource params, String errorMessage) {
        try {
            int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
            if (rowsAffected == 0) {
                throw new InternalServerErrorException(errorMessage);
            }
        } catch (Exception e) {
            logger.error("{} - SQL: {}, Params: {}", errorMessage, sql, params, e);
            throw new InternalServerErrorException(errorMessage, e);
        }
    }
}

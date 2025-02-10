package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.hugo.demo.currency.CurrencyEntity;
import com.hugo.demo.dao.CurrencyDAO;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CurrencyDAOImpl implements CurrencyDAO {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Autowired
    public CurrencyDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public CurrencyEntity addCurrency(CurrencyEntity currencyEntity) {
        String sql = SQLQueryConstants.ADD_CURRENCY;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("currencyName", currencyEntity.getCurrencyName());
        params.addValue("currencyCode", currencyEntity.getCurrencyCode());
        params.addValue("value", currencyEntity.getValue());

        try {
            executeUpdate(sql, params, "Failed to add currency");
            return currencyEntity;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to add product", e);
        }
    }

    @Override
    public CurrencyEntity updateCurrency(CurrencyEntity currencyEntity) {
        StringBuilder sql = new StringBuilder(SQLQueryConstants.UPDATE_CURRENCY_BASE_QUERY);
        boolean hasPrevious = false;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (!currencyEntity.getCurrencyName().isEmpty()) {
            hasPrevious = true;
            sql.append(" currencyName = :currencyName");
            params.addValue("currencyName", currencyEntity.getCurrencyName());
        }
        if (currencyEntity.getValue() <= 0.00) {
            if (hasPrevious) {
                sql.append(", ");
            }
            sql.append(" value = :value");
            params.addValue("value", currencyEntity.getValue());
        }

        sql.append("WHERE currencyCode =: currencyCode");
        params.addValue("currencyCode", currencyEntity.getCurrencyCode());

        try {
            executeUpdate(sql.toString(), params, "Failed to add currency");
            return currencyEntity;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to add product", e);
        }
    }

    @Override
    public CurrencyEntity fetchCurrencyDetails(String currencyCode) {
        String sql = SQLQueryConstants.FETCH_DETAILS_BY_CURRENCY_CODE;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("currencyCode", currencyCode);

        try {
            Optional<CurrencyEntity> currencyEntity = namedParameterJdbcTemplate.query(sql, params, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToCurrencyEntity(rs));
                }
                return Optional.empty();
            });

           if(currencyEntity != null && currencyEntity.isPresent()) {
               return currencyEntity.get();
           }
           else{
               throw new InternalServerErrorException("Error occurred while fetching currency Details");
           }

        } catch (DataAccessException e) {
           throw new InternalServerErrorException("Error occurred while fetching currency Details", e);
        }
    }

    @Override
    public void deleteCurrency(String currencyCode) {
        String sql = SQLQueryConstants.DELETE_CURRENCY;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("currencyCode", currencyCode);

        try {
            executeUpdate(sql, params, "Failed to delete currency");
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to delete currency", e);
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

    @Override
    public List<CurrencyEntity> fetchAllCurrencies() {
        String sql = SQLQueryConstants.FETCH_ALL_CURR;
        try {
            return namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> mapRowToCurrencyEntity(rs));
        } catch (DataAccessException e) {
            throw new InternalServerErrorException("Error occurred while fetching all currencies", e);
        }
    }

    private CurrencyEntity mapRowToCurrencyEntity(ResultSet rs) throws SQLException {
        return CurrencyEntity.newBuilder().setCurrencyCode(rs.getString("currencyCode"))
            .setCurrencyName(rs.getString("currencyName")).setValue(rs.getDouble("value"))
            .build();
    }

}

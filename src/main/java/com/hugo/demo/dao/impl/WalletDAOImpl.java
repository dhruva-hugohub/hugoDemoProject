package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.dao.WalletDAO;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.wallet.WalletEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WalletDAOImpl implements WalletDAO {

    private static final Logger logger = LoggerFactory.getLogger(WalletDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public WalletDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<WalletEntity> findByWalletId(long walletId) {
        String sql = SQLQueryConstants.FIND_WALLET_BY_ID;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("walletId", walletId);
        return getUserEntity(sql, parameters);
    }

    @Override
    public Optional<WalletEntity> findByUserId(long userId) {
        String sql = SQLQueryConstants.FIND_WALLET_BY_USER_ID;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        return getUserEntity(sql, parameters);
    }

    private Optional<WalletEntity> getUserEntity(String sql, MapSqlParameterSource parameters) {
        logger.info(sql);

        try {
            return namedParameterJdbcTemplate.query(sql, parameters, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToWalletEntity(rs));
                }
                return Optional.empty();
            });
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Error occurred while trying to fetch User Details", e);
        }
    }

    @Override
    public boolean checkExistsByUser(String field, String value) {
        StringBuilder sql = new StringBuilder(SQLQueryConstants.COUNT_WALLET_BY_FIELD);
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (Objects.equals(field, "walletId")) {
            sql.append("u.walletId = :walletId");
            parameters.addValue("walletId", value);
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
    public WalletEntity save(WalletEntity walletEntity) {
        String sql = SQLQueryConstants.ADD_WALLET;
        logger.info(sql);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", walletEntity.getUserId())
            .addValue("walletBalance", walletEntity.getWalletBalance());

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return findByUserId(walletEntity.getUserId()).orElse(walletEntity);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to save user", e);
        }
    }

    @Override
    public WalletEntity updateWallet(long userId, double walletBalance) {
        String sql = SQLQueryConstants.UPDATE_WALLET;
        logger.info(sql);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("walletBalance", walletBalance);

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return findByUserId(userId).orElse(null);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to save walletEntity", e);
        }
    }

    private WalletEntity mapRowToWalletEntity(ResultSet rs) throws SQLException {
        return WalletEntity.newBuilder()
            .setUserId(rs.getLong("userId"))
            .setWalletId(rs.getLong("walletId"))
            .setWalletBalance(rs.getDouble("walletBalance"))
            .build();
    }
}

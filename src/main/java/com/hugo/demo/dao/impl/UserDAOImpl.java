package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.user.UserEntity;
import com.hugo.demo.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserDAOImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<UserEntity> findByEmailAddress(String emailAddress) {
        String sql = SQLQueryConstants.FIND_BY_EMAIL_ADDRESS;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("emailAddress", emailAddress);

        try {
            return namedParameterJdbcTemplate.query(sql, parameters, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToUserEntity(rs));
                }
                return Optional.empty();
            });
        } catch (DataAccessException e) {
            logger.error("Error occurred while fetching user by email address: {}", emailAddress, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmailAddress(String emailAddress) {
        String sql = SQLQueryConstants.COUNT_USERS_BY_EMAIL_ADDRESS;
        try {
            Integer count = jdbcTemplate.queryForObject(sql,  Integer.class, emailAddress);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            logger.error("Error occurred while checking existence of email address: {}", emailAddress, e);
            return false;
        }
    }

    @Override
    public UserEntity save(UserEntity user) {
        String sql = SQLQueryConstants.ADD_USER;

        if (findByEmailAddress(user.getEmail()).isPresent()) {
            throw new RecordAlreadyExistsException(
                CommonStatusCode.BAD_REQUEST_ERROR, "User with email '" + user.getEmail() + "' already exists."
            );
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", user.getName())
            .addValue("phoneNumber", user.getPhoneNumber())
            .addValue("emailAddress", user.getEmail())
            .addValue("pinHash", user.getPinHash())
            .addValue("passwordHash", user.getPasswordHash());

        try {
            namedParameterJdbcTemplate.update(sql, params);
            return findByEmailAddress(user.getEmail()).orElse(user);
        } catch (DataAccessException e) {
            logger.error("Error occurred while saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public UserEntity updateToken(UserEntity user) {
        String sql = SQLQueryConstants.UPDATE_USER_TOKEN;

        try {
            jdbcTemplate.update(sql, user.getToken(), DateUtil.convertTimestampToString(user.getTokenExpiry()), user.getEmail());
            return findByEmailAddress(user.getEmail()).orElse(null);
        } catch (DataAccessException e) {
            logger.error("Error occurred while updating token for user: {}", user.getEmail(), e);
            return null;
        }
    }

    private UserEntity mapRowToUserEntity(ResultSet rs) throws SQLException {
        return UserEntity.newBuilder()
            .setUserId(rs.getLong("userId"))
            .setName(rs.getString("name"))
            .setPhoneNumber(rs.getString("phoneNumber"))
            .setEmail(rs.getString("emailAddress"))
            .setPinHash(rs.getString("pinHash"))
            .setPasswordHash(rs.getString("passwordHash"))
            .build();
    }
}

package com.hugo.demo.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.user.UserEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<UserEntity> findByEmailAddress(String emailAddress) {
        StringBuilder sql = buildFindUserBaseQuery();
        sql.append("AND emailAddress = :emailAddress");
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("emailAddress", emailAddress);
        return getUserEntity(sql.toString(), parameters);
    }

    @Override
    public Optional<UserEntity> findByUserId(long userId) {
        StringBuilder sql = buildFindUserBaseQuery();
        sql.append("AND userId = :userId");
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        return getUserEntity(sql.toString(), parameters);
    }


    @Override
    public boolean checkExistsByUser(String field, String value) {
        StringBuilder sql = new StringBuilder(SQLQueryConstants.COUNT_USERS_BY_FIELD);
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (Objects.equals(field, "emailAddress")) {
            sql.append("u.emailAddress = :emailAddress");
            parameters.addValue("emailAddress", value);
        } else if (Objects.equals(field, "userId")) {
            sql.append("u.userId = :userId");
            parameters.addValue("userId", value);
        }


        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(sql.toString(), parameters, Integer.class);
            return count != null && count > 0;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Error occurred while trying to check " + field, e);
        }
    }

    @Override
    @Transactional
    public UserEntity save(UserEntity user) {
        String sql = SQLQueryConstants.ADD_USER;
        logger.info(sql);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", user.getName())
            .addValue("phoneNumber", user.getPhoneNumber())
            .addValue("emailAddress", user.getEmail())
            .addValue("pinHash", user.getPinHash())
            .addValue("passwordHash", user.getPasswordHash())
            .addValue("profileImage", user.getProfileImage());

        try {
            executeUpdate(sql, params, "Failed to save user");
            return findByEmailAddress(user.getEmail()).orElse(user);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to save user", e);
        }
    }


    @Override
    @Transactional
    public UserEntity updateUser(UserEntity user) {
        StringBuilder query = buildUpdateUserBaseQuery();
        boolean hasPrevious = false;

        if (!user.getPhoneNumber().isEmpty()) {

            query.append("phoneNumber = :phoneNumber");
            hasPrevious = true;
        }
        if (!user.getName().isEmpty()) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append(" name =:name");
            hasPrevious = true;
        }
        if (!user.getProfileImage().isEmpty()) {
            if (hasPrevious) {
                query.append(", ");
            }
            query.append(" profileImage =:profileImage");
        }
        query.append(" WHERE userId = :userId");

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", user.getUserId())
            .addValue("name", user.getName())
            .addValue("phoneNumber", user.getPhoneNumber())
            .addValue("profileImage", user.getProfileImage());

        try {
            executeUpdate(query.toString(), params, "Failed to edit user");
            return findByUserId(user.getUserId()).orElse(user);
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to edit user", e);
        }
    }


    @Override
    @Transactional
    public boolean updateUserCredentials(UserEntity user) {
        StringBuilder sql = buildUpdateUserBaseQuery();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        boolean hasPrevious = false;

        user.getEmail();
        if (!user.getPasswordHash().isEmpty()) {
            sql.append("passwordHash = :passwordHash");
            parameters.addValue("passwordHash", user.getPasswordHash());
            hasPrevious = true;
        }

        if (!user.getPinHash().isEmpty()) {
            if (hasPrevious) {
                sql.append(", ");
            }
            sql.append("pinHash = :pinHash");
            parameters.addValue("pinHash", user.getPinHash());
        }

        sql.append(" WHERE emailAddress = :emailAddress");
        parameters.addValue("emailAddress", user.getEmail());

        try {
            executeUpdate(sql.toString(), parameters, "Failed to edit user");
            return true;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to edit user", e);
        }
    }

    @Override
    @Transactional
    public boolean deleteUser(long userId) {
        String sql = SQLQueryConstants.MARK_USER_DELETED;
        logger.info(sql);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId);

        try {
            executeUpdate(sql, params, "Failed to edit user");
            return true;
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Failed to save user", e);
        }
    }

    private StringBuilder buildFindUserBaseQuery() {
        return new StringBuilder(SQLQueryConstants.FIND_USER_BASE_QUERY);
    }

    private StringBuilder buildUpdateUserBaseQuery() {
        return new StringBuilder(SQLQueryConstants.UPDATE_USER_BASE_QUERY);
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

    private Optional<UserEntity> getUserEntity(String sql, MapSqlParameterSource parameters) {
        logger.info(sql);

        try {
            return namedParameterJdbcTemplate.query(sql, parameters, rs -> {
                if (rs.next()) {
                    return Optional.of(mapRowToUserEntity(rs));
                }
                return Optional.empty();
            });
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Error occurred while trying to fetch User Details", e);
        }
    }

    private UserEntity mapRowToUserEntity(ResultSet rs) throws SQLException {
        return UserEntity.newBuilder()
            .setUserId(rs.getLong("userId"))
            .setName(rs.getString("name"))
            .setPhoneNumber(rs.getString("phoneNumber"))
            .setProfileImage(rs.getString("profileImage"))
            .setEmail(rs.getString("emailAddress"))
            .setPinHash(rs.getString("pinHash"))
            .setPasswordHash(rs.getString("passwordHash"))
            .build();
    }
}

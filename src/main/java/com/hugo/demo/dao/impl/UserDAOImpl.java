package com.hugo.demo.dao.impl;


import static com.hugo.demo.util.DateUtil.convertToProtoTimestamp;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.protobuf.Timestamp;
import com.hugo.demo.dao.SQLQueryConstants;
import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.user.UserEntity;
import com.hugo.demo.util.DateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);
    private final EntityManager entityManager;

    @Autowired
    public UserDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<UserEntity> findByEmailAddress(String emailAddress) {
        Query findByEmailAddress =
            entityManager.createNativeQuery(SQLQueryConstants.FIND_BY_EMAIL_ADDRESS).setParameter("emailAddress", emailAddress);

        List<Object[]> results = findByEmailAddress.getResultList();
        return results.stream().findFirst().map(result ->
            UserEntity.newBuilder()
                .setUserId(((Number) (result)[0]).longValue())
                .setName((String) (result)[1])
                .setPhoneNumber((String) (result)[2])
                .setEmail((String) (result)[3])
                .setPinHash((String) (result)[4])
                .setPasswordHash((String) (result)[5])
                .setToken((String) (result)[9])
                .setTokenExpiry(convertToProtoTimestamp((java.sql.Timestamp) (result)[10]))
                .build()
        );
    }

    @Override
    public boolean existsByEmailAddress(String emailAddress) {
        try {
            Query findByEmailAddress =
                entityManager.createNativeQuery(SQLQueryConstants.COUNT_USER_BY_EMAIL_ADDRESS).setParameter("emailAddress", emailAddress);
            return findByEmailAddress.getSingleResult() != null && !Objects.equals(findByEmailAddress.getSingleResult().toString(), "0");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public UserEntity save(UserEntity user) {
        entityManager.createNativeQuery(SQLQueryConstants.ADD_USER)
            .setParameter("name", user.getName())
            .setParameter("phoneNumber", user.getPhoneNumber())
            .setParameter("emailAddress", user.getEmail())
            .setParameter("pinHash", user.getPinHash())
            .setParameter("passwordHash", user.getPasswordHash())
            .setParameter("token", user.getToken())
            .setParameter("tokenExpiry", tokenExpiryString(user.getTokenExpiry()))
            .executeUpdate();

        Optional<UserEntity> updatedUser = findByEmailAddress(user.getEmail());
        return updatedUser.orElse(user);
    }

    @Override
    @Transactional
    public UserEntity updateToken(UserEntity user) {
        entityManager.createNativeQuery(SQLQueryConstants.UPDATE_USER_TOKEN)
            .setParameter("token", user.getToken())
            .setParameter("tokenExpiry", tokenExpiryString(user.getTokenExpiry()))
            .setParameter("emailAddress", user.getEmail())
            .executeUpdate();

        Optional<UserEntity> updatedUser = findByEmailAddress(user.getEmail());

        return updatedUser.orElse(null);
    }

    private String tokenExpiryString(Timestamp timestamp) {
        return DateUtil.convertTimestampToString(timestamp);
    }


}

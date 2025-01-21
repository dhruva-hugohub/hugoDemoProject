package com.hugo.demo.dao;

import java.util.Optional;

import com.hugo.demo.user.UserEntity;

public interface UserDAO {
    Optional<UserEntity> findByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);

    UserEntity save(UserEntity userEntity);

    UserEntity updateToken(UserEntity userEntity);
}

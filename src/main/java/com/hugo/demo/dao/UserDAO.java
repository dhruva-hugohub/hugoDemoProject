package com.hugo.demo.dao;

import java.util.Optional;

import com.hugo.demo.user.UserEntity;

public interface UserDAO {
    Optional<UserEntity> findByEmailAddress(String emailAddress);

    Optional<UserEntity> findByUserId(long userId);

    boolean checkExistsByUser(String field, String value);

    UserEntity save(UserEntity userEntity);

    UserEntity updateUser(UserEntity user);

    boolean deleteUser(long userId);

    boolean updateUserCredentials(UserEntity user);
}

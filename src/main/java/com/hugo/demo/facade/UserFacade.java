package com.hugo.demo.facade;

import java.util.Optional;

import com.hugo.demo.constants.ResourceConstants;
import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.user.UserEntity;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheManager = ResourceConstants.BEAN_CACHE_MANAGER_REDIS, cacheNames = ResourceConstants.CACHE_NAME_USER)
public class UserFacade {

    private final UserDAO userDAO;

    public UserFacade(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Cacheable(value = "user", key = "#emailAddress")
    public Optional<UserEntity> findByEmailAddress(String emailAddress) {
        return userDAO.findByEmailAddress(emailAddress);
    }

    @Cacheable(value = "user", key = "#id")
    public Optional<UserEntity> findByUserId(Long id) {
        return userDAO.findByUserId(id);
    }

    public boolean checkExistsByUser(String field, String value) {
        return userDAO.checkExistsByUser(field, value);
    }

    @CachePut(value = "user", key = "#userEntity.userId")
    public UserEntity save(UserEntity userEntity) {
        return userDAO.save(userEntity);
    }


    @CachePut(value = "user", key = "#user.userId")
    public UserEntity updateUser(UserEntity user) {
        return userDAO.updateUser(user);
    }

    @CacheEvict(value = "user", key = "#userId")
    public boolean deleteUser(long userId) {
        return userDAO.deleteUser(userId);
    }

    public boolean updateUserCredentials(UserEntity user) {
        return userDAO.updateUserCredentials(user);
    }
}

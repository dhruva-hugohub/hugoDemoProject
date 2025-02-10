package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.userquantity.UserQuantityEntity;

public interface UserQuantityDAO {

    Optional<UserQuantityEntity> findByMetalAndUserId(long userId, String metalId);

    boolean checkExistsByUser(String field, String value);

    UserQuantityEntity save(UserQuantityEntity userQuantityEntity);

    UserQuantityEntity updateQuantity(UserQuantityEntity userQuantityEntity);

    List<UserQuantityEntity> fetchQuantitesByUserId(long userId);

}

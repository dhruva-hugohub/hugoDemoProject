package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.dao.UserQuantityDAO;
import com.hugo.demo.userquantity.UserQuantityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserQuantityFacade {

    private final UserQuantityDAO userQuantityDAO;

    @Autowired
    public UserQuantityFacade(UserQuantityDAO userQuantityDAO) {
        this.userQuantityDAO = userQuantityDAO;
    }

    public Optional<UserQuantityEntity> findByMetalAndUserId(long userId, String metalId){
        return this.userQuantityDAO.findByMetalAndUserId(userId, metalId);
    }

    public boolean checkExistsByUser(String field, String value){
        return this.userQuantityDAO.checkExistsByUser(field, value);
    }

    public UserQuantityEntity save(UserQuantityEntity userQuantityEntity){
        return this.userQuantityDAO.save(userQuantityEntity);
    }

    public UserQuantityEntity updateQuantity(UserQuantityEntity userQuantityEntity){
        return this.userQuantityDAO.updateQuantity(userQuantityEntity);
    }

    public List<UserQuantityEntity> fetchQuantitesByUserId(long userId){
        return this.userQuantityDAO.fetchQuantitesByUserId(userId);
    }
}

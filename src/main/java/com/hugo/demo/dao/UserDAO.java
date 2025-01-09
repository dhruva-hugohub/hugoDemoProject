package com.hugo.demo.dao;


import java.util.List;

import com.hugo.demo.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends CrudRepository<User, Long> {

    @Override
    List<User> findAll();
}

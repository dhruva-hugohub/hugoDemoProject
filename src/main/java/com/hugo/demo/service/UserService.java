package com.hugo.demo.service;

import java.util.List;

import com.hugo.demo.model.User;
import com.hugo.demo.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public User saveUser(User user){
        return userDAO.save(user);
    }

    public List<User> getUsers(){
        return userDAO.findAll();
    }

    public User getUserById(Long id){
        return userDAO.findById(id).orElse(new User());
    }

    public void deleteUser(Long id){
        userDAO.deleteById(id);
    }

    public User updateUser(User user, Long id){
        user.setId(id);
        return userDAO.save(user);
    }
}

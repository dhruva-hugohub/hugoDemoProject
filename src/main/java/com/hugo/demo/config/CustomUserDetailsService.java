package com.hugo.demo.config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.hugo.demo.dao.UserDAO;
import com.hugo.demo.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;


    @Autowired
    public CustomUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userDAO.findByEmailAddress(username);
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(userEntity.get().getEmail(), userEntity.get().getPasswordHash(),
            getAuthority(userEntity.get()));
    }

    private Set<SimpleGrantedAuthority> getAuthority(UserEntity userEntity) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (userEntity.getEmail().equals("admin@gmail.com")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + "ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + "USER"));
        }
        return authorities;
    }
}

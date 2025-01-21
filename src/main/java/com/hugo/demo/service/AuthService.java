package com.hugo.demo.service;

import com.hugo.demo.api.user.UserLoginRequestDTO;

public interface AuthService {

    String login(UserLoginRequestDTO userLoginRequestDTO);

}

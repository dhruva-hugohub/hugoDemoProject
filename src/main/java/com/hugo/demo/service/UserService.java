package com.hugo.demo.service;

import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserResponseDTO;

public interface UserService {

    UserResponseDTO userLogin(UserLoginRequestDTO userLoginRequestDTO);

    UserResponseDTO userRegister(UserRegisterRequestDTO userRegisterRequestDTO);
}

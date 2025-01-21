package com.hugo.demo.service;

import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserLoginResponseDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserRegisterResponseDTO;

public interface UserService {

    UserLoginResponseDTO userLogin(UserLoginRequestDTO userLoginRequestDTO);

    UserRegisterResponseDTO userRegister(UserRegisterRequestDTO userRegisterRequestDTO);
}

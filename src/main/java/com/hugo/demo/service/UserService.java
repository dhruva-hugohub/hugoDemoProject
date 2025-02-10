package com.hugo.demo.service;

import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.user.EditUserPasswordRequestDTO;
import com.hugo.demo.api.user.EditUserPinRequestDTO;
import com.hugo.demo.api.user.EditUserRequestDTO;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserResponseDTO;
import com.hugo.demo.api.user.UserVerifyPinRequestDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    UserResponseDTO userLogin(UserLoginRequestDTO userLoginRequestDTO, HttpServletRequest httpServletRequest);

    UserResponseDTO userRegister(UserRegisterRequestDTO userRegisterRequestDTO, HttpServletRequest httpServletRequest);

    PlainResponseDTO verifyPin(UserVerifyPinRequestDTO userVerifyPinRequestDTO);

    UserResponseDTO getUserDetails(long userId);

    UserResponseDTO editUserDetails(EditUserRequestDTO dto);

    PlainResponseDTO deleteUser(long userId);

    PlainResponseDTO editUserPassword(EditUserPasswordRequestDTO dto);

    PlainResponseDTO editUserPin(EditUserPinRequestDTO dto);

    void processUserRegistration();

    void processUserLogin();

    PlainResponseDTO logout(HttpServletRequest request);
}

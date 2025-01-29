package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.UserService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_AUTH_PATH)
@Tag(name = "Auth APIs", description = "Authentication APIs.")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public ApiResponse login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {

        UserResponseDTO responseDTO = userService.userLogin(userLoginRequestDTO);

        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);

    }

    @PostMapping("/signup")
    public ApiResponse signup(@RequestBody UserRegisterRequestDTO userRegisterRequestDTO) {

        UserResponseDTO responseDTO = userService.userRegister(userRegisterRequestDTO);

        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);

    }


}

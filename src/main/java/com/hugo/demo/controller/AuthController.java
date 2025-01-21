package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserLoginResponseDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserRegisterResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.UserService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_AUTH_PATH)
@Tag(name = "Auth APIs", description = "Authentication APIs.")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public ApiResponse login(@RequestBody UserLoginRequestDTO userLoginRequestDTO) {

        String validationError = validateRequest(userLoginRequestDTO);
        if (validationError != null) {
            return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
        }

        try {
            UserLoginResponseDTO responseDTO = userService.userLogin(userLoginRequestDTO);

            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);
        } catch (Exception e) {
            LOGGER.error("Error processing login", e);
            return ResponseUtil.buildEmptyResponse(CommonStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signup")
    public ApiResponse signup(@RequestBody UserRegisterRequestDTO userRegisterRequestDTO) {
        String validationError = validateRequest(userRegisterRequestDTO);
        if (validationError != null) {
            return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
        }

        try {
            UserRegisterResponseDTO responseDTO = userService.userRegister(userRegisterRequestDTO);

            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);
        } catch (Exception e) {
            LOGGER.error("Error processing signup", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    private String validateRequest(Object requestDTO) {
        if (requestDTO instanceof UserLoginRequestDTO loginRequest) {
            if (!StringUtils.hasText(loginRequest.getEmail())) {
                return "Please enter a valid email address";
            } else if (!StringUtils.hasText(loginRequest.getPassword())) {
                return "Please enter a valid password";
            }
        } else if (requestDTO instanceof UserRegisterRequestDTO registerRequest) {
            if (!StringUtils.hasText(registerRequest.getName())) {
                return "Please enter a valid name";
            } else if (!StringUtils.hasText(registerRequest.getPassword())) {
                return "Please enter a valid password";
            } else if (!StringUtils.hasText(registerRequest.getEmail())) {
                return "Please enter a valid email";
            } else if (!StringUtils.hasText(registerRequest.getPin())) {
                return "Please enter a valid pin";
            }
        }
        return null;
    }


}

package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.user.EditUserPasswordRequestDTO;
import com.hugo.demo.api.user.EditUserPinRequestDTO;
import com.hugo.demo.api.user.EditUserRequestDTO;
import com.hugo.demo.api.user.UserResponseDTO;
import com.hugo.demo.api.user.UserVerifyPinRequestDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.UserService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_USER_PATH)
@Tag(name = "User APIs", description = "User Profile APIs.")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/verify-pin")
    public ApiResponse verifyPin(@RequestBody UserVerifyPinRequestDTO userVerifyPinRequestDTO) {
        PlainResponseDTO plainResponseDTO = userService.verifyPin(userVerifyPinRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, plainResponseDTO);
    }

    @GetMapping("/{userId}")
    public ApiResponse getUserDetails(@PathVariable long userId) {
        UserResponseDTO userResponseDTO = userService.getUserDetails(userId);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, userResponseDTO);
    }

    @PutMapping("/edit")
    public ApiResponse editUserDetails(@RequestBody EditUserRequestDTO editUserRequestDTO) {
        UserResponseDTO userResponseDTO = userService.editUserDetails(editUserRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, userResponseDTO);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse deleteUser(@PathVariable long userId) {
        PlainResponseDTO plainResponseDTO = userService.deleteUser(userId);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, plainResponseDTO);
    }

    @PutMapping("/update-password")
    public ApiResponse updatePassword(@RequestBody EditUserPasswordRequestDTO editUserPasswordRequestDTO) {
        PlainResponseDTO plainResponseDTO = userService.editUserPassword(editUserPasswordRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, plainResponseDTO);
    }

    @PutMapping("/update-pin")
    public ApiResponse updatePin(@RequestBody EditUserPinRequestDTO editUserPinRequestDTO) {
        PlainResponseDTO plainResponseDTO = userService.editUserPin(editUserPinRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, plainResponseDTO);
    }

    @PostMapping("/logout")
    public ApiResponse logout(HttpServletRequest request) {
        PlainResponseDTO plainResponseDTO = userService.logout(request);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, plainResponseDTO);
    }


}


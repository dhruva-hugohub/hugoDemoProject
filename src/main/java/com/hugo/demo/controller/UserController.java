package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.LiveItemPriceService;
import com.hugo.demo.service.UserService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_USER_PATH)
@Tag(name="User APIs", description = "User Profile APIs.")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final LiveItemPriceService liveItemPriceService;
    public UserController(UserService userService, LiveItemPriceService liveItemPriceService) {
        this.userService = userService;
        this.liveItemPriceService = liveItemPriceService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> helloAdmin(){
        return ResponseEntity.ok("Hello Admin");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user")
    public ApiResponse helloUser(){
        LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO = liveItemPriceService.saveItemPrice("XAG", "USD", "g");
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, liveItemPriceAPIResponseDTO );
    }
}


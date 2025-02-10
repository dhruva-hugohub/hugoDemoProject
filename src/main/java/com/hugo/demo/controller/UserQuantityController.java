package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.userquantity.CreateUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.EditUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.UserQuantityResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.UserQuantityService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_USER_QUANTITY_PATH)
@Tag(name = "Wallet APIs", description = "Wallet APIs.")
public class UserQuantityController {

    private final UserQuantityService userQuantityService;

    public UserQuantityController(UserQuantityService userQuantityService) {
        this.userQuantityService = userQuantityService;
    }

    @PostMapping("/create-user-metal-quantity")
    public ApiResponse createUserMetalQuantity(@RequestBody CreateUserQuantityRequestDTO createUserQuantityRequestDTO) {
        UserQuantityResponseDTO userQuantityResponseDTO = userQuantityService.createUserQuantity(createUserQuantityRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, userQuantityResponseDTO);
    }

    @PutMapping("/update-user-metal-quantity")
    public ApiResponse updateUserMetalQuantity(@RequestBody EditUserQuantityRequestDTO editUserQuantityRequestDTO) {
        UserQuantityResponseDTO userQuantityResponseDTO = userQuantityService.updateUserMetalQuantity(editUserQuantityRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, userQuantityResponseDTO);
    }

    @GetMapping("/get-metal-user-id-quantity")
    public ApiResponse getMetalUserIdQuantity(@RequestParam Long userId, @RequestParam String metalId) {
        UserQuantityResponseDTO userQuantityResponseDTO = userQuantityService.fetchUserQuantitesByMetalAndUserId(userId, metalId);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, userQuantityResponseDTO);
    }
}

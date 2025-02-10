package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.alert.AlertResponseDTO;
import com.hugo.demo.api.alert.CreateAlertRequestDTO;
import com.hugo.demo.api.alert.EditAlertRequestDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.AlertService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_ALERT_PATH)
@Tag(name = "Alert APIs", description = "Alert APIs.")
public class AlertController {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping(value = "/create")
    public ApiResponse addProvider(@RequestBody CreateAlertRequestDTO createAlertRequestDTO) {
        AlertResponseDTO response = alertService.createAlert(createAlertRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @PutMapping("/edit")
    public ApiResponse updateProvider(@RequestBody EditAlertRequestDTO editAlertRequestDTO) {
        AlertResponseDTO alertResponseDTO = alertService.editAlert(editAlertRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, alertResponseDTO);
    }
}

package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.dateItemPrice.DateItemPriceAPIResponseDTO;
import com.hugo.demo.api.dateItemPrice.EditItemPriceRequestDTO;
import com.hugo.demo.api.dateItemPrice.HistoricalDateItemPriceAPIResponseDTO;
import com.hugo.demo.api.dateItemPrice.SaveItemPriceRequestDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.DateItemPriceService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_DATE_ITEM_PRICE_PATH)
@Tag(name = "Date Item APIs", description = "Date Item APIs.")
public class DateItemPriceController {

    private final DateItemPriceService dateItemPriceService;

    @Autowired
    public DateItemPriceController(DateItemPriceService dateItemPriceService) {
        this.dateItemPriceService = dateItemPriceService;
    }

    @GetMapping("/fetch-item-details")
    public ApiResponse getItemPriceDetails(@RequestParam String metalId, @RequestParam long providerId, @RequestParam String date , @RequestParam String currencyCode) {
        DateItemPriceAPIResponseDTO itemPrice = dateItemPriceService.fetchItemPriceDetails(metalId, providerId, date, currencyCode);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, itemPrice);
    }

    @PostMapping("/save-item-price")
    public ApiResponse saveItemPrice(@RequestBody SaveItemPriceRequestDTO request) {
        HistoricalDateItemPriceAPIResponseDTO response = dateItemPriceService.saveItemPrice(
            request.getMetalId(), request.getProviderId(), request.getDate(),
            request.getBaseApiUrl(), request.getCurrencyCode(), request.getWeightUnit()
        );
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @PutMapping("/edit-item-price")
    public ApiResponse editItemPrice(@RequestBody EditItemPriceRequestDTO request) {
        HistoricalDateItemPriceAPIResponseDTO response = dateItemPriceService.editItemPrice(
            request.getMetalId(), request.getProviderId(), request.getDate(),
            request.getBaseApiUrl(), request.getCurrencyCode(), request.getWeightUnit()
        );
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }
}

package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.currency.Currency;
import com.hugo.demo.api.currency.CurrencyRequestDTO;
import com.hugo.demo.api.currency.CurrencyResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.CurrencyService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_CURRENCY_PATH)
@Tag(name = "Currency APIs", description = "Currency APIs.")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping("/create")
    public ApiResponse createCurrency(@RequestBody CurrencyRequestDTO currencyRequestDTO) {
      CurrencyResponseDTO response = currencyService.createCurrency(currencyRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @PutMapping("/edit")
    public ApiResponse updateCurrency(@RequestBody CurrencyRequestDTO currencyRequestDTO) {
        CurrencyResponseDTO response = currencyService.updateCurrency(currencyRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteCurrency(@RequestBody String currencyCode) {
        PlainResponseDTO response = currencyService.deleteCurrency(currencyCode);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @GetMapping("/detail")
    public ApiResponse getCurrencyDetails(@RequestParam String currencyCode) {
        CurrencyResponseDTO response = currencyService.getCurrency(currencyCode);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

}

package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.liveItemPrice.LiveItemPriceFilter;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;
import com.hugo.demo.service.LiveItemPriceService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_LIVE_ITEM_PRICE_PATH)
@Tag(name = "Product APIs", description = "Product Management APIs.")
public class LiveItemPriceController {

    private final LiveItemPriceService liveItemPriceService;

    @Autowired
    public LiveItemPriceController(LiveItemPriceService liveItemPriceService) {
        this.liveItemPriceService = liveItemPriceService;
    }

    @GetMapping("/fetch-live-item-details")
    public ApiResponse getLiveItemPrice(@RequestParam long providerId, @RequestParam String metalId){
        LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO = liveItemPriceService.fetchLiveItemPriceDetails(providerId, metalId);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, liveItemPriceAPIResponseDTO);
    }

    @GetMapping("/all")
    public ApiResponse getAllLiveItemPrices(
        @RequestParam(required = false, defaultValue = "") String metalId,
        @RequestParam(required = false, defaultValue = "0") Long providerId,
        @RequestParam(required = false, defaultValue = "0.0") Double performanceUpperLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double performanceLowerLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double askValueUpperLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double askValueLowerLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double bidValueUpperLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double bidValueLowerLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double valueUpperLimit,
        @RequestParam(required = false, defaultValue = "0.0") Double valueLowerLimit,
        @RequestParam(defaultValue = "dateTime") String sortBy,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        LiveItemPriceFilter liveItemPriceFilter = LiveItemPriceFilter.newBuilder()
            .setMetalId(metalId)
            .setProviderId(providerId)
            .setPage(page)
            .setPageSize(pageSize)
            .setPerformanceUpperLimit(performanceUpperLimit)
            .setPerformanceLowerLimit(performanceLowerLimit)
            .setAskValueUpperLimit(askValueUpperLimit)
            .setAskValueLowerLimit(askValueLowerLimit)
            .setBidValueUpperLimit(bidValueUpperLimit)
            .setBidValueLowerLimit(bidValueLowerLimit)
            .setValueUpperLimit(valueUpperLimit)
            .setValueLowerLimit(valueLowerLimit)
            .setSortBy(sortBy)
            .build();

        PaginatedLiveItemPrice paginatedLiveItemPrices = liveItemPriceService.fetchLiveItemPrices(liveItemPriceFilter);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, paginatedLiveItemPrices);
    }

}

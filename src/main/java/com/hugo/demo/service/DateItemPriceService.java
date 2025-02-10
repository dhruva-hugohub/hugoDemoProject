package com.hugo.demo.service;

import com.hugo.demo.api.dateItemPrice.DateItemPriceAPIResponseDTO;
import com.hugo.demo.api.dateItemPrice.HistoricalDateItemPriceAPIResponseDTO;

public interface DateItemPriceService {

    HistoricalDateItemPriceAPIResponseDTO saveItemPrice(String metalId, long providerId, String date, String baseApiUrl, String currencyCode, String weightUnit);

    HistoricalDateItemPriceAPIResponseDTO editItemPrice(String metalId, long providerId, String date, String baseApiUrl, String currencyCode, String weightUnit);

    DateItemPriceAPIResponseDTO fetchItemPriceDetails(String metalId, long providerId, String date);
}

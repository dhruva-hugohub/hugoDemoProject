package com.hugo.demo.service;

import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.liveItemPrice.LiveItemPriceFilter;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;

public interface LiveItemPriceService {
    LiveItemPriceAPIResponseDTO saveItemPrice(String metalCode, long providerId, String currencyCode, String weightUnit, String baseApiUrl );

    LiveItemPriceAPIResponseDTO editItemPrice(String metalCode, long providerId, String currencyCode, String weightUnit, String baseApiUrl );

    void saveItemPricesForAllProviders(String currencyCode, String weightUnit);

    PaginatedLiveItemPrice fetchLiveItemPrices(LiveItemPriceFilter liveItemPriceFilter);

    LiveItemPriceAPIResponseDTO fetchLiveItemPriceDetails(long providerId, String metalId);
}

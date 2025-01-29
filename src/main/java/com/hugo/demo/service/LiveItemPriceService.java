package com.hugo.demo.service;

import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;

public interface LiveItemPriceService {
    LiveItemPriceAPIResponseDTO saveItemPrice(String metalCode, String currencyCode, String weightUnit );

    void saveItemPricesForAllProviders(String currencyCode, String weightUnit);

}

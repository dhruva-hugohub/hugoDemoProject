package com.hugo.demo.service;

import com.hugo.demo.api.currency.CurrencyRequestDTO;
import com.hugo.demo.api.currency.CurrencyResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;

public interface CurrencyService {

    CurrencyResponseDTO createCurrency(CurrencyRequestDTO currencyRequestDTO);

    CurrencyResponseDTO updateCurrency(CurrencyRequestDTO currencyRequestDTO);

    PlainResponseDTO deleteCurrency(String currencyCode);

    CurrencyResponseDTO getCurrency(String currencyCode);

    void updateCurrencyRates();

}

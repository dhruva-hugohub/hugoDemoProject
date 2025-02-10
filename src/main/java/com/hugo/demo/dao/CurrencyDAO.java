package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.currency.CurrencyEntity;

public interface CurrencyDAO {

    CurrencyEntity addCurrency(CurrencyEntity currencyEntity);

    CurrencyEntity updateCurrency(CurrencyEntity currencyEntity);

    CurrencyEntity fetchCurrencyDetails(String currencyCode);

    void deleteCurrency(String currencyCode);

    List<CurrencyEntity> fetchAllCurrencies();
}

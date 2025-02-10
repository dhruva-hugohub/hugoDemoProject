package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.currency.CurrencyEntity;
import com.hugo.demo.dao.CurrencyDAO;
import org.springframework.stereotype.Component;

@Component
public class CurrencyFacade {

    private final CurrencyDAO currencyDAO;

    public CurrencyFacade(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    public CurrencyEntity addCurrency(CurrencyEntity currencyEntity) {
        return currencyDAO.addCurrency(currencyEntity);
    }

    public CurrencyEntity updateCurrency(CurrencyEntity currencyEntity){
        return currencyDAO.updateCurrency(currencyEntity);
    }

    public CurrencyEntity fetchCurrencyDetails(String currencyCode){
        return currencyDAO.fetchCurrencyDetails(currencyCode);
    }

    public void deleteCurrency(String currencyCode){
        currencyDAO.deleteCurrency(currencyCode);
    }

    public List<CurrencyEntity> fetchAllCurrencies(){
        return currencyDAO.fetchAllCurrencies();
    }

}

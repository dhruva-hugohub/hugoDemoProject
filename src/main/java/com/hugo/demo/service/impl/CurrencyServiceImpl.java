package com.hugo.demo.service.impl;

import java.util.Map;

import com.hugo.demo.api.currency.CurrencyRequestDTO;
import com.hugo.demo.api.currency.CurrencyResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.currency.CurrencyEntity;
import com.hugo.demo.facade.CurrencyFacade;
import com.hugo.demo.service.CurrencyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyFacade currencyFacade;

    private final RestTemplate restTemplate;

    @Value("{app.currency-api-key}")
    private String apiKey;

    public CurrencyServiceImpl(CurrencyFacade currencyFacade, RestTemplate restTemplate) {
        this.currencyFacade = currencyFacade;
        this.restTemplate = restTemplate;
    }

    @Override
    public CurrencyResponseDTO createCurrency(CurrencyRequestDTO currencyRequestDTO) {
        CurrencyEntity currencyEntity =
            CurrencyEntity.newBuilder().setCurrencyCode(currencyRequestDTO.getCurrencyCode()).setCurrencyName(currencyRequestDTO.getCurrencyName())
                .setValue(
                    currencyRequestDTO.getValue()).build();

        currencyFacade.addCurrency(currencyEntity);

        return CurrencyResponseDTO.newBuilder().setCurrencyCode(currencyEntity.getCurrencyCode()).setCurrencyName(currencyEntity.getCurrencyName())
            .setValue(
                currencyEntity.getValue()).build();
    }

    @Override
    public CurrencyResponseDTO updateCurrency(CurrencyRequestDTO currencyRequestDTO) {
        CurrencyEntity currencyEntity =
            CurrencyEntity.newBuilder().setCurrencyCode(currencyRequestDTO.getCurrencyCode()).setCurrencyName(currencyRequestDTO.getCurrencyName())
                .setValue(
                    currencyRequestDTO.getValue()).build();

        currencyFacade.updateCurrency(currencyEntity);

        return CurrencyResponseDTO.newBuilder().setCurrencyCode(currencyEntity.getCurrencyCode()).setCurrencyName(currencyEntity.getCurrencyName())
            .setValue(
                currencyEntity.getValue()).build();
    }

    @Override
    public PlainResponseDTO deleteCurrency(String currencyCode) {
        currencyFacade.deleteCurrency(currencyCode);
        return PlainResponseDTO.newBuilder().setMessage("Currency deleted successfully").build();
    }

    @Override
    public CurrencyResponseDTO getCurrency(String currencyCode) {
        CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(currencyCode);
        return CurrencyResponseDTO.newBuilder().setCurrencyCode(currencyEntity.getCurrencyCode()).setCurrencyName(currencyEntity.getCurrencyName())
            .setValue(
                currencyEntity.getValue()).build();
    }

    public void updateCurrencyRates() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(URLConstants.V1_GET_CURRENCY_BASE_URL + apiKey, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                if (responseBody.containsKey("data")) {
                    Map<String, Double> exchangeRates = (Map<String, Double>) responseBody.get("data");

                    if (exchangeRates != null) {
                        exchangeRates.forEach((currencyCode, value) -> {
                            CurrencyEntity currencyEntity = CurrencyEntity.newBuilder()
                                .setCurrencyCode(currencyCode)
                                .setValue(value)
                                .build();

                            currencyFacade.updateCurrency(currencyEntity);
                        });

                        System.out.println("Currency rates updated successfully.");
                    }
                } else {
                    System.err.println("API response does not contain 'data' key.");
                }
            } else {
                System.err.println("Failed to fetch currency data: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error updating currency rates: " + e.getMessage());
        }
    }

}

package com.hugo.demo.service.impl;

import java.util.Optional;

import com.hugo.demo.api.dateItemPrice.DateItemPriceAPIResponseDTO;
import com.hugo.demo.api.dateItemPrice.HistoricalDateItemPriceAPIResponseDTO;
import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.currency.CurrencyEntity;
import com.hugo.demo.dateItemPrice.DateItemPriceEntity;
import com.hugo.demo.facade.CurrencyFacade;
import com.hugo.demo.facade.DateItemPriceFacade;
import com.hugo.demo.facade.LiveItemPriceFacade;
import com.hugo.demo.service.DateItemPriceService;
import com.hugo.demo.util.ProtoJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DateItemPriceServiceImpl implements DateItemPriceService {

    private final DateItemPriceFacade dateItemPriceFacade;
    private final RestTemplate restTemplate;
    private final CurrencyFacade currencyFacade;

    public DateItemPriceServiceImpl(DateItemPriceFacade dateItemPriceFacade, RestTemplate restTemplate, CurrencyFacade currencyFacade) {
        this.dateItemPriceFacade = dateItemPriceFacade;
        this.restTemplate = restTemplate;
        this.currencyFacade = currencyFacade;
    }

    @Override
    public HistoricalDateItemPriceAPIResponseDTO saveItemPrice(String metalId, long providerId, String date, String baseApiUrl, String currencyCode, String weightUnit) {
        return processItemPrice(metalId, providerId, date, baseApiUrl, currencyCode, weightUnit, true);
    }

    @Override
    public HistoricalDateItemPriceAPIResponseDTO editItemPrice(String metalId, long providerId, String date, String baseApiUrl, String currencyCode, String weightUnit) {
        return processItemPrice(metalId, providerId, date, baseApiUrl, currencyCode, weightUnit, false);
    }

    @Override
    public DateItemPriceAPIResponseDTO fetchItemPriceDetails(String metalId, long providerId, String date, String currencyCode) {


        Optional<DateItemPriceEntity> itemPriceRecord = dateItemPriceFacade.getRecord(metalId, date, providerId);

        return mapToDateItemPriceAPIResponseDTO(itemPriceRecord.orElse(DateItemPriceEntity.getDefaultInstance()), currencyCode);
    }

    private HistoricalDateItemPriceAPIResponseDTO processItemPrice(String metalId, long providerId, String date, String baseApiUrl, String currencyCode, String weightUnit, boolean isNewRecord) {
        String uri = UriComponentsBuilder.fromUriString(baseApiUrl)
            .queryParam("metal", metalId)
            .queryParam("currency", currencyCode)
            .queryParam("weight_unit", weightUnit)
            .toUriString();

        String jsonResponse = restTemplate.getForObject(uri, String.class);
        try {
            HistoricalDateItemPriceAPIResponseDTO apiResponse = ProtoJsonUtil.fromJson(jsonResponse, HistoricalDateItemPriceAPIResponseDTO.class);
            assert apiResponse != null;

            for (DateItemPriceAPIResponseDTO dateItemPriceDTO : apiResponse.getItemsList()) {
                saveDateItemPrice(dateItemPriceDTO);
            }

            return apiResponse;
        } catch (Exception e) {
            throw new RuntimeException("Error processing historical price data", e);
        }
    }

    private void saveDateItemPrice(DateItemPriceAPIResponseDTO dateItemPriceDTO) {
        Optional<DateItemPriceEntity> optionalEntity = dateItemPriceFacade.getRecord(
            dateItemPriceDTO.getMetalId(), dateItemPriceDTO.getDate(), dateItemPriceDTO.getProviderId()
        );

        DateItemPriceEntity entityToSave;

        if (optionalEntity.isEmpty()) {
            entityToSave = DateItemPriceEntity.newBuilder()
                .setDate(dateItemPriceDTO.getDate())
                .setMetalId(dateItemPriceDTO.getMetalId())
                .setOpen(dateItemPriceDTO.getOpen())
                .setClose(dateItemPriceDTO.getClose())
                .setHigh(dateItemPriceDTO.getHigh())
                .setLow(dateItemPriceDTO.getLow())
                .setProviderId(dateItemPriceDTO.getProviderId())
                .build();

            dateItemPriceFacade.addRecord(entityToSave);
        } else {
            DateItemPriceEntity existingEntity = optionalEntity.get();
            entityToSave = existingEntity.toBuilder()
                .setClose(dateItemPriceDTO.getClose())
                .setLow(Math.min(existingEntity.getLow(), dateItemPriceDTO.getLow()))
                .setHigh(Math.max(existingEntity.getHigh(), dateItemPriceDTO.getHigh()))
                .build();

            dateItemPriceFacade.editRecord(entityToSave);
        }
    }

    private DateItemPriceAPIResponseDTO mapToDateItemPriceAPIResponseDTO(DateItemPriceEntity entity, String currencyCode) {
        CurrencyEntity currencyEntity = currencyFacade.fetchCurrencyDetails(currencyCode);

        return DateItemPriceAPIResponseDTO.newBuilder()
            .setDate(entity.getDate())
            .setWeightUnit("g")
            .setOpen(entity.getOpen() * currencyEntity.getValue())
            .setClose(entity.getClose() * currencyEntity.getValue())
            .setHigh(entity.getHigh() * currencyEntity.getValue())
            .setLow(entity.getLow() * currencyEntity.getValue())
            .setMetalId(entity.getMetalId())
            .setProviderId(entity.getProviderId())
            .build();
    }


}

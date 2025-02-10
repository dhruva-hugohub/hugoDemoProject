package com.hugo.demo.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.Option;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.hugo.demo.alert.AlertEntity;
import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.dateItemPrice.DateItemPriceEntity;
import com.hugo.demo.enums.alertType.TypeOfAlert;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.GenericException;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.facade.AlertFacade;
import com.hugo.demo.facade.DateItemPriceFacade;
import com.hugo.demo.facade.LiveItemPriceFacade;
import com.hugo.demo.facade.ProductFacade;
import com.hugo.demo.facade.ProviderFacade;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;
import com.hugo.demo.liveItemPrice.LiveItemPriceFilter;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.provider.ProviderEntity;
import com.hugo.demo.service.LiveItemPriceService;
import com.hugo.demo.service.ProductService;
import com.hugo.demo.util.DateUtil;
import com.hugo.demo.util.ProtoJsonUtil;
import com.hugo.demo.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LiveItemPriceServiceImpl implements LiveItemPriceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveItemPriceServiceImpl.class);
    private final LiveItemPriceFacade liveItemPriceFacade;
    private final RestTemplate restTemplate;
    private final ProviderFacade providerFacade;
    private final DateItemPriceFacade dateItemPriceFacade;
    private final ProductService productService;
    private final AlertFacade alertFacade;

    @Autowired
    public LiveItemPriceServiceImpl(RestTemplate restTemplate, LiveItemPriceFacade liveItemPriceFacade, ProviderFacade providerFacade,
                                    DateItemPriceFacade dateItemPriceFacade, ProductService productService, AlertFacade alertFacade

    ) {
        this.restTemplate = restTemplate;
        this.liveItemPriceFacade = liveItemPriceFacade;
        this.providerFacade = providerFacade;
        this.dateItemPriceFacade = dateItemPriceFacade;
        this.productService = productService;
        this.alertFacade = alertFacade;

    }

    @Override
    public LiveItemPriceAPIResponseDTO saveItemPrice(String metalCode, long providerId, String currencyCode, String weightUnit, String baseApiUrl) {
        return processItemPrice(metalCode, providerId, currencyCode, weightUnit, baseApiUrl, true);
    }

    @Override
    public LiveItemPriceAPIResponseDTO editItemPrice(String metalCode, long providerId, String currencyCode, String weightUnit, String baseApiUrl) {
        return processItemPrice(metalCode, providerId, currencyCode, weightUnit, baseApiUrl, false);
    }

    @Override
    public void saveItemPricesForAllProviders(String currencyCode, String weightUnit) {
        try {
            List<ProviderEntity> providerEntities = providerFacade.fetchAllProviders();
            if (providerEntities.isEmpty()) {
                LOGGER.warn("No providers found in the database.");
                return;
            }

            for (ProviderEntity providerEntity : providerEntities) {
                processProviderPrices(providerEntity, currencyCode, weightUnit);
            }
        } catch (InternalServerErrorException e) {
            throw new InternalServerErrorException("Error saving item prices for all providers", e);
        }
    }

    @Override
    public LiveItemPriceAPIResponseDTO fetchLiveItemPriceDetails(long providerId, String metalId) {
        Optional<LiveItemPriceEntity> liveItemPriceRecord =
            liveItemPriceFacade.fetchItemRecordByDate(metalId, getCurrentTimestamp(), providerId);

        if (liveItemPriceRecord.isPresent()) {
            LiveItemPriceEntity entity = liveItemPriceRecord.get();

            return LiveItemPriceAPIResponseDTO.newBuilder()
                .setDate(Timestamps.toString(entity.getDateTime()))
                .setWeightUnit("g")
                .setAsk(entity.getAskValue())
                .setMid(entity.getValue())
                .setBid(entity.getBidValue())
                .setValue(entity.getValue())
                .setPerformance(entity.getPerformance())
                .setMetalId(entity.getMetalId())
                .setProviderId(entity.getProviderId())
                .build();
        }

        // Return a default response or throw an exception if no record is found
        return LiveItemPriceAPIResponseDTO.newBuilder().build();
    }

    private void processProviderPrices(ProviderEntity providerEntity, String currencyCode, String weightUnit) {
        int providerId = providerEntity.getProviderId();
        LOGGER.info("Processing products for providerId: {}", providerId);

        PaginatedProducts productEntities = productService.fetchProductsByProviderId((long) providerId);
        if (productEntities.getProductsList().isEmpty()) {
            LOGGER.warn("No products found for providerId: {}", providerId);
            return;
        }

        for (ProductEntity productEntity : productEntities.getProductsList()) {
            processProductPrice(providerEntity, productEntity, currencyCode, weightUnit);
        }
    }

    private void processProductPrice(ProviderEntity providerEntity, ProductEntity productEntity, String currencyCode, String weightUnit) {
        String metalCode = productEntity.getMetalId();
        int providerId = providerEntity.getProviderId();

        LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO = liveItemPriceFacade.fetchItemRecordByDate(metalCode, getCurrentTimestamp(), providerId).isEmpty() ?
            saveItemPrice(metalCode, providerId, currencyCode, weightUnit, providerEntity.getProviderAPIUrl()) :
            editItemPrice(metalCode, providerId, currencyCode, weightUnit, providerEntity.getProviderAPIUrl());

        DateItemPriceEntity dateItemPriceEntityResponse = processDateItemPrice(metalCode, liveItemPriceAPIResponseDTO);
        updateProductPrice(liveItemPriceAPIResponseDTO);
        processAlerts(metalCode, dateItemPriceEntityResponse);
    }

    private DateItemPriceEntity processDateItemPrice(String metalCode, LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO) {
        Optional<DateItemPriceEntity> optionalDateItemPriceFacade = dateItemPriceFacade.getRecord(metalCode, LocalDate.now().toString(), liveItemPriceAPIResponseDTO.getProviderId());

        if (optionalDateItemPriceFacade.isEmpty()) {
            return dateItemPriceFacade.addRecord(DateItemPriceEntity.newBuilder()
                .setDate(LocalDate.now().toString())
                .setMetalId(metalCode)
                .setOpen(liveItemPriceAPIResponseDTO.getValue())
                .setProviderId(liveItemPriceAPIResponseDTO.getProviderId())
                .setClose(liveItemPriceAPIResponseDTO.getValue())
                .setHigh(liveItemPriceAPIResponseDTO.getValue())
                .setLow(liveItemPriceAPIResponseDTO.getValue())
                .build());
        } else {
            return dateItemPriceFacade.editRecord(DateItemPriceEntity.newBuilder()
                .setProviderId(optionalDateItemPriceFacade.get().getProviderId())
                .setDate(LocalDate.now().toString())
                .setMetalId(metalCode)
                .setOpen(optionalDateItemPriceFacade.get().getOpen())
                .setClose(liveItemPriceAPIResponseDTO.getValue())
                .setLow(Math.min(liveItemPriceAPIResponseDTO.getValue(), optionalDateItemPriceFacade.get().getLow()))
                .setHigh(Math.max(liveItemPriceAPIResponseDTO.getValue(), optionalDateItemPriceFacade.get().getHigh()))
                .build());
        }
    }

    private void updateProductPrice(LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO) {
        EditProductRequestDTO editProductRequestDTO = EditProductRequestDTO.newBuilder()
            .setMetalId(liveItemPriceAPIResponseDTO.getMetalId())
            .setProviderId(liveItemPriceAPIResponseDTO.getProviderId())
            .setPrice(liveItemPriceAPIResponseDTO.getValue())
            .build();
        productService.updateProduct(editProductRequestDTO);
    }

    private void processAlerts(String metalCode, DateItemPriceEntity dateItemPriceEntityResponse) {
        List<AlertEntity> alertEntityList = alertFacade.getAlertByMetalId(metalCode);
        for (AlertEntity alertEntity : alertEntityList) {
            if (alertEntity.getTypeOfAlert() == TypeOfAlert.ALL_TIME_HIGH && dateItemPriceEntityResponse.getHigh() > alertEntity.getMaxPrice()) {
                LOGGER.info("-----ALLTIME_HIGH------");
            } else if (alertEntity.getTypeOfAlert() == TypeOfAlert.ALL_TIME_LOW && dateItemPriceEntityResponse.getLow() < alertEntity.getMinPrice()) {
                LOGGER.info("-----ALLTIME_LOW------");
            }
        }
    }

    @Override
    public PaginatedLiveItemPrice fetchLiveItemPrices(LiveItemPriceFilter liveItemPriceFilter) {
        try {
            ValidationUtil.validatePaginationInputs(liveItemPriceFilter.getPage(), liveItemPriceFilter.getPageSize());

            ValidationUtil.validateSortBy(liveItemPriceFilter.getSortBy(),
                "askValue", "bidValue", "value", "dateTime", "performance", "metalId");

            return liveItemPriceFacade.fetchLiveItemPrices(liveItemPriceFilter);
        }
        catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
        catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private LiveItemPriceAPIResponseDTO processItemPrice(String metalCode, long providerId, String currencyCode,
                                                         String weightUnit, String baseApiUrl, boolean isNewRecord) {
        String uri = UriComponentsBuilder.fromUriString(baseApiUrl)
            .queryParam("metal", metalCode)
            .queryParam("currency", currencyCode)
            .queryParam("weight_unit", weightUnit)
            .toUriString();

        String jsonResponse = restTemplate.getForObject(uri, String.class);

        try {
            LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO = ProtoJsonUtil.fromJson(jsonResponse, LiveItemPriceAPIResponseDTO.class);
            assert liveItemPriceAPIResponseDTO != null;

            LiveItemPriceEntity liveItemPriceEntity = LiveItemPriceEntity.newBuilder()
                .setProviderId(providerId)
                .setMetalId(metalCode)
                .setAskValue(liveItemPriceAPIResponseDTO.getAsk())
                .setBidValue(liveItemPriceAPIResponseDTO.getBid())
                .setValue(liveItemPriceAPIResponseDTO.getValue())
                .setPerformance(liveItemPriceAPIResponseDTO.getPerformance())
                .setDateTime(convertStringToSqlTimeStamp(liveItemPriceAPIResponseDTO.getDate()))
                .build();

            LiveItemPriceEntity responseEntity = isNewRecord
                ? liveItemPriceFacade.addItemRecord(liveItemPriceEntity)
                : liveItemPriceFacade.editItemRecord(liveItemPriceEntity);

            return buildResponseDTO(responseEntity, liveItemPriceAPIResponseDTO);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error processing item price data", e);
        }
    }

    private LiveItemPriceAPIResponseDTO buildResponseDTO(LiveItemPriceEntity responseEntity,
                                                         LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO) {
        return LiveItemPriceAPIResponseDTO.newBuilder()
            .setProviderId(responseEntity.getProviderId())
            .setMetalId(responseEntity.getMetalId())
            .setAsk(responseEntity.getAskValue())
            .setBid(responseEntity.getBidValue())
            .setValue(responseEntity.getValue())
            .setPerformance(responseEntity.getPerformance())
            .setDate(DateUtil.convertTimestampToString(responseEntity.getDateTime()))
            .setWeightUnit(liveItemPriceAPIResponseDTO.getWeightUnit())
            .build();
    }


    private Timestamp getCurrentTimestamp() {
        return DateUtil.convertSqlTimestampToProtoTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
    }

    private Timestamp convertStringToSqlTimeStamp(String dateTimeString) {
        java.sql.Timestamp sqlTimestamp = DateUtil.convertStringToTimestamp(dateTimeString);
        return DateUtil.convertSqlTimestampToProtoTimestamp(sqlTimestamp);
    }

}

package com.hugo.demo.service.impl;

import java.util.List;

import com.google.protobuf.Timestamp;
import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.dao.DayItemPriceDAO;
import com.hugo.demo.dao.LiveItemPriceDAO;
import com.hugo.demo.dao.ProductDAO;
import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.dateItemPrice.DateItemPriceEntity;
import com.hugo.demo.liveItemPrice.LiveItemPriceEntity;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.provider.ProviderEntity;
import com.hugo.demo.service.LiveItemPriceService;
import com.hugo.demo.util.DateUtil;
import com.hugo.demo.util.ProtoJsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class LiveItemPriceServiceImpl implements LiveItemPriceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveItemPriceServiceImpl.class);
    private final LiveItemPriceDAO liveItemPriceDAO;
    private final RestTemplate restTemplate;
    private final ProviderDAO providerDAO;
    private final ProductDAO productDAO;
    private final DayItemPriceDAO dayItemPriceDAO;

    @Autowired
    public LiveItemPriceServiceImpl(RestTemplate restTemplate, LiveItemPriceDAO liveItemPriceDAO, ProviderDAO providerDAO, ProductDAO productDAO, DayItemPriceDAO dayItemPriceDAO) {
        this.restTemplate = restTemplate;
        this.liveItemPriceDAO = liveItemPriceDAO;
        this.providerDAO = providerDAO;
        this.productDAO = productDAO;
        this.dayItemPriceDAO = dayItemPriceDAO;
    }

    @Override
    public LiveItemPriceAPIResponseDTO saveItemPrice(String metalCode, String currencyCode, String weightUnit) {

        String uri = UriComponentsBuilder.fromUriString(URLConstants.V1_GOLD_BROKER_BASE_URL + "/spot-price").queryParam("metal", metalCode)
            .queryParam("currency", currencyCode).queryParam("weight_unit", weightUnit).toUriString();

        String jsonResponse = restTemplate.getForObject(uri, String.class);

        try {
            LiveItemPriceAPIResponseDTO liveItemPriceAPIResponseDTO = ProtoJsonUtil.fromJson(jsonResponse, LiveItemPriceAPIResponseDTO.class);
            assert liveItemPriceAPIResponseDTO != null;

            LiveItemPriceEntity liveItemPriceEntity =
                LiveItemPriceEntity.newBuilder().setMetalId(metalCode).setAskValue(liveItemPriceAPIResponseDTO.getAsk())
                    .setBidValue(liveItemPriceAPIResponseDTO.getBid()).setValue(liveItemPriceAPIResponseDTO.getValue())
                    .setPerformance(liveItemPriceAPIResponseDTO.getPerformance())
                    .setDateTime(convertStringToSqlTimeStamp(liveItemPriceAPIResponseDTO.getDate())).build();

            LiveItemPriceEntity responseEntity = liveItemPriceDAO.addItemRecord(liveItemPriceEntity);

            liveItemPriceAPIResponseDTO =
                LiveItemPriceAPIResponseDTO.newBuilder().setMetalId(responseEntity.getMetalId()).setAsk(responseEntity.getAskValue())
                    .setBid(responseEntity.getBidValue()).setValue(responseEntity.getValue()).setPerformance(responseEntity.getPerformance())
                    .setDate(DateUtil.convertTimestampToString(responseEntity.getDateTime()))
                    .setWeightUnit(liveItemPriceAPIResponseDTO.getWeightUnit()).build();

            return liveItemPriceAPIResponseDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error processing item price data", e);
        }
    }

    @Override
    public void saveItemPricesForAllProviders(String currencyCode, String weightUnit) {
        try {
            List<ProviderEntity> providerEntities = providerDAO.fetchAllProviderDetails();

            if (providerEntities.isEmpty()) {
                LOGGER.warn("No providers found in the database.");
                return;
            }

            for (ProviderEntity providerEntity : providerEntities) {
                int providerId = providerEntity.getProviderId();
                LOGGER.info("Processing products for providerId: {}", providerId);

                List<ProductEntity> productEntities = productDAO.getProductsByProviderId(providerId, "providerId", "desc", 1, 10);

                if (productEntities.isEmpty()) {
                    LOGGER.warn("No products found for providerId: {}", providerId);
                    continue;
                }

                for (ProductEntity productEntity : productEntities) {
                    String metalCode = productEntity.getMetalId();
                    saveItemPrice(metalCode, currencyCode, weightUnit);
//                    DateItemPriceEntity dateItemPriceEntity = DateItemPriceEntity.newBuilder().setDate("").setMetalId(metalCode).set.build();
//                    dayItemPriceDAO.addRecord()
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error saving item prices for all providers", e);
            throw new RuntimeException("Error saving item prices for all providers", e);
        }
    }

    private Timestamp convertStringToSqlTimeStamp(String dateTimeString) {
        java.sql.Timestamp sqlTimestamp = DateUtil.convertStringToTimestamp(dateTimeString);
        return DateUtil.convertSqlTimestampToProtoTimestamp(sqlTimestamp);
    }

}

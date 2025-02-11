package com.hugo.demo.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.hugo.demo.api.alert.AlertResponseDTO;
import com.hugo.demo.api.currency.CurrencyResponseDTO;
import com.hugo.demo.api.dateItemPrice.DateItemPriceAPIResponseDTO;
import com.hugo.demo.api.dateItemPrice.HistoricalDateItemPriceAPIResponseDTO;
import com.hugo.demo.api.liveItemPrice.LiveItemPriceAPIResponseDTO;
import com.hugo.demo.api.order.OrderResponseDTO;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.product.ProductResponseDTO;
import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.api.provider.ProviderResponseDTO;
import com.hugo.demo.api.user.UserResponseDTO;
import com.hugo.demo.api.userquantity.UserQuantityResponseDTO;
import com.hugo.demo.api.wallet.WalletResponseDTO;
import com.hugo.demo.liveItemPrice.PaginatedLiveItemPrice;
import com.hugo.demo.order.PaginatedOrders;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.protobuf.ProtoMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProfileConfig implements WebMvcConfigurer {

    @Bean
    ProtoMessageConverter protoMessageConverter() {
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder()
            .add(PlainResponseDTO.getDescriptor())
            .add(UserResponseDTO.getDescriptor())
            .add(LiveItemPriceAPIResponseDTO.getDescriptor())
            .add(PaginatedProviders.getDescriptor())
            .add(PaginatedProducts.getDescriptor())
            .add(ProviderResponseDTO.getDescriptor())
            .add(ProductResponseDTO.getDescriptor())
            .add(AlertResponseDTO.getDescriptor())
            .add(OrderResponseDTO.getDescriptor())
            .add(CurrencyResponseDTO.getDescriptor())
            .add(PaginatedLiveItemPrice.getDescriptor())
            .add(HistoricalDateItemPriceAPIResponseDTO.getDescriptor())
            .add(DateItemPriceAPIResponseDTO.getDescriptor())
            .add(UserQuantityResponseDTO.getDescriptor())
            .add(WalletResponseDTO.getDescriptor())
            .add(PaginatedOrders.getDescriptor())
            .build();
        return new ProtoMessageConverter(typeRegistry);
    }

    @Bean
    @Primary
    ProtobufHttpMessageConverter protobufHttpMessageConverter(ProtoMessageConverter messageConverter) {
        ProtobufHttpMessageConverter converter = messageConverter.getHttpConverter(true);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROTOBUF));
        return converter;
    }

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ProtobufModule());
        return objectMapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addFirst(new MappingJackson2HttpMessageConverter(objectMapper()));
        converters.addFirst(protobufHttpMessageConverter(protoMessageConverter()));
    }
}

package com.hugo.demo.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.hugo.demo.api.user.UserLoginResponseDTO;
import com.hugo.demo.api.user.UserRegisterResponseDTO;
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
            .add(UserLoginResponseDTO.getDescriptor())
            .add(UserRegisterResponseDTO.getDescriptor())
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

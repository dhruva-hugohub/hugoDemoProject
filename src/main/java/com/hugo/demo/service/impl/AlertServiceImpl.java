package com.hugo.demo.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.hugo.demo.alert.AlertEntity;
import com.hugo.demo.api.alert.AlertResponseDTO;
import com.hugo.demo.api.alert.CreateAlertRequestDTO;
import com.hugo.demo.api.alert.EditAlertRequestDTO;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.GenericException;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.facade.AlertFacade;
import com.hugo.demo.service.AlertService;
import com.hugo.demo.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertFacade alertFacade;

    @Autowired
    public AlertServiceImpl(AlertFacade alertFacade) {
        this.alertFacade = alertFacade;
    }

    @Override
    public AlertResponseDTO createAlert(CreateAlertRequestDTO dto) {
        try {
            ValidationUtil.validateCreateAlertRequest(dto);

            AlertEntity alertEntity = AlertEntity.newBuilder()
                .setUserId(dto.getUserId())
                .setMetalId(dto.getMetalId())
                .setProviderId(dto.getProviderId())
                .setMinPrice(dto.getMinPrice())
                .setMaxPrice(dto.getMaxPrice())
                .setEmail(dto.getEmail())
                .setFcmToken(dto.getFcmToken())
                .setTypeOfAlert(dto.getTypeOfAlert())
                .setDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                .build();

            AlertEntity alertEntityResponse = alertFacade.addItemRecord(alertEntity);
            return AlertResponseDTO.newBuilder()
                .setUserId(alertEntityResponse.getUserId())
                .setMetalId(alertEntityResponse.getMetalId())
                .setProviderId(alertEntityResponse.getProviderId())
                .setMinPrice(alertEntityResponse.getMinPrice())
                .setMaxPrice(alertEntityResponse.getMaxPrice())
                .setEmail(alertEntityResponse.getEmail())
                .setFcmToken(alertEntityResponse.getFcmToken())
                .setTypeOfAlert(alertEntityResponse.getTypeOfAlert())
                .build();

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public AlertResponseDTO editAlert(EditAlertRequestDTO dto) {
        try {
            ValidationUtil.validateEditAlertRequest(dto);

            AlertEntity alertEntity = AlertEntity.newBuilder()
                .setUserId(dto.getUserId())
                .setMetalId(dto.getMetalId())
                .setProviderId(dto.getProviderId())
                .setMinPrice(dto.getMinPrice())
                .setMaxPrice(dto.getMaxPrice())
                .setEmail(dto.getEmail())
                .setFcmToken(dto.getFcmToken())
                .build();

            AlertEntity alertEntityResponse = alertFacade.editItemRecord(alertEntity);
            return AlertResponseDTO.newBuilder()
                .setUserId(alertEntityResponse.getUserId())
                .setMetalId(alertEntityResponse.getMetalId())
                .setProviderId(alertEntityResponse.getProviderId())
                .setMinPrice(alertEntityResponse.getMinPrice())
                .setMaxPrice(alertEntityResponse.getMaxPrice())
                .setEmail(alertEntityResponse.getEmail())
                .setFcmToken(alertEntityResponse.getFcmToken())
                .setTypeOfAlert(alertEntityResponse.getTypeOfAlert())
                .build();

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

}





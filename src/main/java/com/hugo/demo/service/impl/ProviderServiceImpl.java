package com.hugo.demo.service.impl;

import java.util.Optional;

import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.api.provider.Provider;
import com.hugo.demo.api.provider.ProviderResponseDTO;
import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.GenericException;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.exception.RecordNotFoundException;
import com.hugo.demo.facade.ProviderFacade;
import com.hugo.demo.provider.ProviderEntity;
import com.hugo.demo.service.ProviderService;
import com.hugo.demo.util.ValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderFacade providerFacade;

    public ProviderServiceImpl(ProviderFacade providerFacade) {
        this.providerFacade = providerFacade;
    }

    @Override
    @Transactional
    public ProviderResponseDTO addProviderDetails(AddProviderRequestDTO requestDTO) {
        try {
            ValidationUtil.validateAddProviderRequest(requestDTO);

            Optional<ProviderEntity> existingProvider = providerFacade.fetchProviderDetailsByName(requestDTO.getProviderName());
            if (existingProvider.isPresent()) {
                throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR,
                    "Provider with name '" + requestDTO.getProviderName() + "' already exists.");
            }

            ProviderEntity providerEntity =
                ProviderEntity.newBuilder().setProviderName(requestDTO.getProviderName()).setProviderAPIUrl(requestDTO.getProviderAPIUrl())
                    .setSchedulerTimePeriod(requestDTO.getSchedulerTimePeriod()).build();

            ProviderEntity savedProviderEntity = providerFacade.addProviderDetails(providerEntity);
            Provider savedProvider =
                Provider.newBuilder().setProviderId(savedProviderEntity.getProviderId()).setProviderName(savedProviderEntity.getProviderName())
                    .setProviderAPIUrl(savedProviderEntity.getProviderAPIUrl()).setSchedulerTimePeriod(savedProviderEntity.getSchedulerTimePeriod())
                    .build();
            return ProviderResponseDTO.newBuilder().setProvider(savedProvider).build();
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        }
        catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    @Transactional
    public ProviderResponseDTO editProviderDetails(EditProviderRequestDTO requestDTO) {
        try {
            ValidationUtil.validateEditProviderRequest(requestDTO);

            ProviderEntity existingProvider = getProviderById(requestDTO.getProviderId());

            if (existingProvider.getProviderId() != requestDTO.getProviderId()) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Account doesn't exist with userId : " + requestDTO.getProviderId());
            }

            ProviderEntity updatedProvider = ProviderEntity.newBuilder(existingProvider).setProviderName(requestDTO.getProviderName())
                .setProviderAPIUrl(requestDTO.getProviderAPIUrl()).setSchedulerTimePeriod(requestDTO.getSchedulerTimePeriod()).build();

            ProviderEntity savedProviderEntity = providerFacade.editProviderDetails(updatedProvider);
            Provider savedProvider =
                Provider.newBuilder().setProviderId(savedProviderEntity.getProviderId()).setProviderName(savedProviderEntity.getProviderName())
                    .setProviderAPIUrl(savedProviderEntity.getProviderAPIUrl()).setSchedulerTimePeriod(savedProviderEntity.getSchedulerTimePeriod())
                    .build();
            return ProviderResponseDTO.newBuilder().setProvider(savedProvider).build();
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
        catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    @Transactional
    public PlainResponseDTO deleteProviderDetails(int providerId) {
        try {
            ProviderEntity existingProvider = getProviderById(providerId);

            if (existingProvider.getProviderId() != providerId) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED, "Provider doesn't exist with providerId : " + providerId);
            }

            boolean isDeleted = providerFacade.deleteProviderDetails(providerId);
            if (isDeleted) {
                return PlainResponseDTO.newBuilder().setMessage("User Deleted Successfully").build();
            } else {
                return PlainResponseDTO.newBuilder().setMessage("Couldn't Delete User").build();
            }
        }
        catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
        catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }


    @Override
    public ProviderResponseDTO fetchProviderDetailsById(int providerId) {
        ProviderEntity providerEntity = getProviderById(providerId);
        Provider provider =
            Provider.newBuilder().setProviderId(providerEntity.getProviderId()).setProviderName(providerEntity.getProviderName())
                .setProviderAPIUrl(providerEntity.getProviderAPIUrl()).setSchedulerTimePeriod(providerEntity.getSchedulerTimePeriod())
                .build();
        return ProviderResponseDTO.newBuilder().setProvider(provider).build();
    }


    @Override
    public PaginatedProviders fetchProviders(String providerName, String sortBy, int page, int pageSize) {
        try {
            ValidationUtil.validatePaginationInputs(page, pageSize);
            ValidationUtil.validateSortBy(sortBy, "providerId", "providerName", "schedulerTimePeriod");

            return providerFacade.fetchProviders(providerName, sortBy, page, pageSize);
        }
        catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        }
        catch (InternalServerErrorException e){
                throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }

    }

    private ProviderEntity getProviderById(int providerId) {
        return providerFacade.fetchProviderDetails(providerId)
            .orElseThrow(() -> new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR,"Provider with ID " + providerId + " not found."));
    }
}

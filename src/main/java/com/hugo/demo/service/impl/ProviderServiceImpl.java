package com.hugo.demo.service.impl;

import java.util.List;

import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.AllProvidersResponseDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.provider.ProviderResponseDTO;
import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.provider.ProviderEntity;
import com.hugo.demo.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderServiceImpl implements ProviderService {
    private final ProviderDAO providerDAO;

    @Autowired
    public ProviderServiceImpl(ProviderDAO providerDAO) {
        this.providerDAO = providerDAO;
    }

    @Override
    public ProviderResponseDTO addProviderDetails(AddProviderRequestDTO addProviderRequestDTO) {
        try {
            ProviderEntity addProviderEntity = ProviderEntity.newBuilder().setProviderName(addProviderRequestDTO.getProviderName())
                .setProviderAPIUrl(addProviderRequestDTO.getProviderAPIUrl()).build();

            ProviderEntity providerResponseEntity = providerDAO.addProviderDetails(addProviderEntity);

            return ProviderResponseDTO.newBuilder().setProviderId(providerResponseEntity.getProviderId())
                .setProviderName(providerResponseEntity.getProviderName()).setProviderAPIUrl(providerResponseEntity.getProviderAPIUrl()).build();
        } catch (Exception e) {
            throw new RuntimeException("Error adding provider details", e);
        }
    }

    @Override
    public ProviderResponseDTO fetchProviderDetails(int providerId) {
        try {

            ProviderEntity providerResponseEntity = providerDAO.fetchProviderDetails(providerId).orElse(null);
            assert providerResponseEntity != null;

            return ProviderResponseDTO.newBuilder().setProviderId(providerResponseEntity.getProviderId())
                .setProviderName(providerResponseEntity.getProviderName()).setProviderAPIUrl(providerResponseEntity.getProviderAPIUrl()).build();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching provider details for providerId: " + providerId, e);
        }
    }

    @Override
    public ProviderResponseDTO editProviderDetails(EditProviderRequestDTO editProviderRequestDTO) {
        try {
            ProviderEntity editProviderEntity = ProviderEntity.newBuilder().setProviderId(editProviderRequestDTO.getProviderId())
                .setProviderName(editProviderRequestDTO.getProviderName()).setProviderAPIUrl(editProviderRequestDTO.getProviderAPIUrl()).build();

            ProviderEntity providerResponseEntity = providerDAO.addProviderDetails(editProviderEntity);

            return ProviderResponseDTO.newBuilder().setProviderId(providerResponseEntity.getProviderId())
                .setProviderName(providerResponseEntity.getProviderName()).setProviderAPIUrl(providerResponseEntity.getProviderAPIUrl()).build();
        } catch (Exception e) {
            throw new RuntimeException("Error editing provider details", e);
        }
    }

    @Override
    public AllProvidersResponseDTO fetchAllProviderDetails() {
        try {
            List<ProviderEntity> providerEntityList = providerDAO.fetchAllProviderDetails();

            AllProvidersResponseDTO.Builder allProvidersResponseDTOBuilder = AllProvidersResponseDTO.newBuilder();

            for (ProviderEntity providerEntity : providerEntityList) {
                ProviderResponseDTO providerResponseDTO =
                    ProviderResponseDTO.newBuilder().setProviderId(providerEntity.getProviderId()).setProviderName(providerEntity.getProviderName())
                        .setProviderAPIUrl(providerEntity.getProviderAPIUrl()).setCreateTs(providerEntity.getCreateTs())
                        .setUpdateTs(providerEntity.getUpdateTs()).build();

                allProvidersResponseDTOBuilder.addProviders(providerResponseDTO);
            }

            return allProvidersResponseDTOBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all provider details", e);
        }
    }


    @Override
    public boolean deleteProviderDetails(int providerId) {
        try {
            return providerDAO.deleteProviderDetails(providerId);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting provider details for providerId: " + providerId, e);
        }
    }
}

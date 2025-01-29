package com.hugo.demo.service;

import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.AllProvidersResponseDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.provider.ProviderResponseDTO;

public interface ProviderService {

    ProviderResponseDTO addProviderDetails(AddProviderRequestDTO addProviderRequestDTO);

    ProviderResponseDTO fetchProviderDetails(int providerId);

    ProviderResponseDTO editProviderDetails(EditProviderRequestDTO editProviderRequestDTO);

    AllProvidersResponseDTO fetchAllProviderDetails();

    boolean deleteProviderDetails(int providerId);
}

package com.hugo.demo.service;

import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.api.provider.ProviderResponseDTO;

public interface ProviderService {

    ProviderResponseDTO addProviderDetails(AddProviderRequestDTO addProviderRequestDTO);

    ProviderResponseDTO fetchProviderDetailsById(int providerId);

    ProviderResponseDTO editProviderDetails(EditProviderRequestDTO editProviderRequestDTO);

    PaginatedProviders fetchProviders(String providerName, String sortBy, int page, int pageSize);

    PlainResponseDTO deleteProviderDetails(int providerId);
}

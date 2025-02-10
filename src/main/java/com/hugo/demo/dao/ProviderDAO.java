package com.hugo.demo.dao;


import java.util.List;
import java.util.Optional;

import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.provider.ProviderEntity;

public interface ProviderDAO {

    ProviderEntity addProviderDetails(ProviderEntity provider);

    Optional<ProviderEntity> fetchProviderDetails(int providerId);

    Optional<ProviderEntity> fetchProviderDetailsByName(String providerName);

    ProviderEntity editProviderDetails(ProviderEntity provider);

    boolean deleteProviderDetails(int providerId);

    List<ProviderEntity> fetchAllProviders();

    PaginatedProviders fetchProviders(String providerName, String sortBy, int page, int pageSize);
}


package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.provider.ProviderEntity;

public interface ProviderDAO {

    ProviderEntity addProviderDetails(ProviderEntity provider);

    Optional<ProviderEntity> fetchProviderDetails(int providerId);

    ProviderEntity editProviderDetails(ProviderEntity provider);

    List<ProviderEntity> fetchAllProviderDetails();

    Optional<ProviderEntity> fetchProviderDetailsByName(String providerName);

    boolean deleteProviderDetails(int providerId);
}

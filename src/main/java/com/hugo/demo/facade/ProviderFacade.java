package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.dao.ProviderDAO;
import com.hugo.demo.provider.ProviderEntity;
import org.springframework.stereotype.Component;

@Component
public class ProviderFacade {

    private final ProviderDAO providerDAO;

    public ProviderFacade(ProviderDAO providerDAO) {
        this.providerDAO = providerDAO;
    }

    public ProviderEntity addProviderDetails(ProviderEntity provider) {
        return providerDAO.addProviderDetails(provider);
    }

    public Optional<ProviderEntity> fetchProviderDetails(int providerId) {
        return providerDAO.fetchProviderDetails(providerId);
    }

    public Optional<ProviderEntity> fetchProviderDetailsByName(String providerName) {
        return providerDAO.fetchProviderDetailsByName(providerName);
    }

    public ProviderEntity editProviderDetails(ProviderEntity provider) {
        return providerDAO.editProviderDetails(provider);
    }

    public boolean deleteProviderDetails(int providerId) {
        return providerDAO.deleteProviderDetails(providerId);
    }

    public List<ProviderEntity> fetchAllProviders() {
        return providerDAO.fetchAllProviders();
    }

    public PaginatedProviders fetchProviders(String providerName, String sortBy, int page, int pageSize) {
        return providerDAO.fetchProviders(providerName, sortBy, page, pageSize);
    }
}


package com.hugo.demo.dao;

import java.util.List;

import com.hugo.demo.product.ProductEntity;

public interface ProductDAO {

    ProductEntity addProduct(ProductEntity product);

    ProductEntity updateProduct(ProductEntity product);

    void deleteProduct(String metalId, int providerId);

    List<ProductEntity> getAllProducts(String sortField, String sortOrder, Integer page, Integer size);

    List<ProductEntity> getProductsByProviderId(int providerId, String sortField, String sortOrder, Integer page, Integer size);

    List<ProductEntity> getProductsByMetalCode(String metalId, String sortField, String sortOrder, Integer page, Integer size);

    int getTotalItems();

    int getTotalPages(int pageSize);
}

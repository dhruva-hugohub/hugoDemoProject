package com.hugo.demo.dao;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.product.ProductFilter;

public interface ProductDAO {

    ProductEntity addProduct(ProductEntity product);

    ProductEntity updateProduct(ProductEntity product);

    boolean deleteProduct(String metalId, long providerId);

    Optional<ProductEntity> fetchProductDetails(Long providerId, String metalId);

    PaginatedProducts fetchProducts(ProductFilter filter);

    List<ProductEntity> fetchAllProducts();

    boolean isProductExists(Long providerId, String metalId);

    void updateStock(long providerId, String metalId, int stock);
}

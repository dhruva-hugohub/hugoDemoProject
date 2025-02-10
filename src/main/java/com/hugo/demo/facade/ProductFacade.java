package com.hugo.demo.facade;

import java.util.List;
import java.util.Optional;

import com.hugo.demo.constants.ResourceConstants;
import com.hugo.demo.dao.ProductDAO;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.product.ProductFilter;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheManager = ResourceConstants.BEAN_CACHE_MANAGER_REDIS, cacheNames = ResourceConstants.CACHE_NAME_PRODUCT)
public class ProductFacade {

    private final ProductDAO productDAO;

    public ProductFacade(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @CachePut(value = "product", key = "#product.metalId + #product.providerId")
    public ProductEntity addProduct(ProductEntity product){
        return productDAO.addProduct(product);
    }

    @CachePut(value = "product", key = "#product.metalId + #product.providerId")
    public ProductEntity updateProduct(ProductEntity product){
        return productDAO.updateProduct(product);
    }

    @CacheEvict(value = "product", key = "#metalId + #providerId")
    public boolean deleteProduct(String metalId, long providerId){
        return productDAO.deleteProduct(metalId, providerId);
    }

    @Cacheable(value = "product", key = "#metalId + #providerId")
    public Optional<ProductEntity> fetchProductDetails(Long providerId, String metalId){
        return productDAO.fetchProductDetails(providerId, metalId);
    }

    public PaginatedProducts fetchProducts(ProductFilter filter){
        return productDAO.fetchProducts(filter);
    }

    public List<ProductEntity> fetchAllProducts(){
        return productDAO.fetchAllProducts();
    }

    public boolean isProductExists(Long providerId, String metalId){
        return productDAO.isProductExists(providerId, metalId);
    }

    @CachePut(value = "product", key="#metalId + #providerId")
    public void updateStock(long providerId, String metalId, int stock){
        productDAO.updateStock(providerId, metalId, stock);
    }

}

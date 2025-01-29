package com.hugo.demo.service.impl;

import java.util.List;

import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.AllProductsResponseDTO;
import com.hugo.demo.api.product.DeleteProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.product.GetProductsByMetalCodeRequestDTO;
import com.hugo.demo.api.product.GetProductsByMetalCodeResponseDTO;
import com.hugo.demo.api.product.GetProductsByProviderIdRequestDTO;
import com.hugo.demo.api.product.GetProductsByProviderIdResponseDTO;
import com.hugo.demo.api.product.ProductResponseDTO;
import com.hugo.demo.dao.ProductDAO;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public ProductResponseDTO addProduct(AddProductRequestDTO addProductRequestDTO) {
        try {
            ProductEntity addProductEntity = ProductEntity.newBuilder()
                .setMetalId(addProductRequestDTO.getMetalId())
                .setProductName(addProductRequestDTO.getProductName())
                .setProductDescription(addProductRequestDTO.getDescription())
                .setProviderId(addProductRequestDTO.getProviderId())
                .setProductValue(addProductRequestDTO.getPrice())
                .build();

            ProductEntity productResponseEntity = productDAO.addProduct(addProductEntity);

            return ProductResponseDTO.newBuilder()
                .setMetalId(productResponseEntity.getMetalId())
                .setProviderId(productResponseEntity.getProviderId())
                .setProductName(productResponseEntity.getProductName())
                .setDescription(productResponseEntity.getProductDescription())
                .setPrice(productResponseEntity.getProductValue())
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Error adding product details", e);
        }
    }

    @Override
    public ProductResponseDTO updateProduct(EditProductRequestDTO editProductRequestDTO) {
        try {
            ProductEntity editProductEntity = ProductEntity.newBuilder()
                .setMetalId(editProductRequestDTO.getMetalId())
                .setProviderId(editProductRequestDTO.getProviderId())
                .setProductName(editProductRequestDTO.getProductName())
                .setProductDescription(editProductRequestDTO.getDescription())
                .setProductValue(editProductRequestDTO.getPrice())
                .build();

            ProductEntity updatedProductEntity = productDAO.updateProduct(editProductEntity);

            return ProductResponseDTO.newBuilder()
                .setMetalId(updatedProductEntity.getMetalId())
                .setProviderId(updatedProductEntity.getProviderId())
                .setProductName(updatedProductEntity.getProductName())
                .setDescription(updatedProductEntity.getProductDescription())
                .setPrice(updatedProductEntity.getProductValue())
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Error updating product details", e);
        }
    }

    @Override
    public void deleteProduct(DeleteProductRequestDTO deleteProductRequestDTO) {
        try {
            productDAO.deleteProduct(deleteProductRequestDTO.getMetalId(), deleteProductRequestDTO.getProviderId());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting product details", e);
        }
    }

    @Override
    public AllProductsResponseDTO fetchAllProductDetails(int page, int size, String sortField, String sortOrder) {
        try {
            List<ProductEntity> productEntityList = productDAO.getAllProducts(sortField, sortOrder, page, size);

            AllProductsResponseDTO.Builder allProductsResponseDTOBuilder = AllProductsResponseDTO.newBuilder();

            for (ProductEntity productEntity : productEntityList) {
                ProductResponseDTO productResponseDTO = ProductResponseDTO.newBuilder()
                    .setMetalId(productEntity.getMetalId())
                    .setProviderId(productEntity.getProviderId())
                    .setProductName(productEntity.getProductName())
                    .setDescription(productEntity.getProductDescription())
                    .setPrice(productEntity.getProductValue())

                    .build();

                allProductsResponseDTOBuilder.addProducts(productResponseDTO);
            }

            return allProductsResponseDTOBuilder.build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching all product details", e);
        }
    }

    @Override
    public GetProductsByProviderIdResponseDTO fetchProductsByProviderId(GetProductsByProviderIdRequestDTO getProductsByProviderIdRequestDTO) {
        try {
            List<ProductEntity> productEntityList = productDAO.getProductsByProviderId(
                getProductsByProviderIdRequestDTO.getProviderId(),
                getProductsByProviderIdRequestDTO.getSortField(),
                getProductsByProviderIdRequestDTO.getSortOrder(),
                getProductsByProviderIdRequestDTO.getPage(),
                getProductsByProviderIdRequestDTO.getSize()
            );

            GetProductsByProviderIdResponseDTO.Builder responseBuilder = GetProductsByProviderIdResponseDTO.newBuilder();

            for (ProductEntity productEntity : productEntityList) {
                ProductResponseDTO productResponseDTO = ProductResponseDTO.newBuilder()
                    .setMetalId(productEntity.getMetalId())
                    .setProviderId(productEntity.getProviderId())
                    .setProductName(productEntity.getProductName())
                    .setDescription(productEntity.getProductDescription())
                    .setPrice(productEntity.getProductValue())

                    .build();

                responseBuilder.addProducts(productResponseDTO);
            }

            int totalPages = productDAO.getTotalPages(getProductsByProviderIdRequestDTO.getSize());
            int totalItems = productDAO.getTotalItems();

            return responseBuilder
                .setTotalPages(totalPages)
                .setTotalItems(totalItems)
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching products by provider ID", e);
        }
    }

    @Override
    public GetProductsByMetalCodeResponseDTO fetchProductsByMetalCode(GetProductsByMetalCodeRequestDTO getProductsByMetalCodeRequestDTO) {
        try {
            List<ProductEntity> productEntityList = productDAO.getProductsByMetalCode(
                getProductsByMetalCodeRequestDTO.getMetalId(),
                getProductsByMetalCodeRequestDTO.getSortField(),
                getProductsByMetalCodeRequestDTO.getSortOrder(),
                getProductsByMetalCodeRequestDTO.getPage(),
                getProductsByMetalCodeRequestDTO.getSize()
            );

            GetProductsByMetalCodeResponseDTO.Builder responseBuilder = GetProductsByMetalCodeResponseDTO.newBuilder();

            for (ProductEntity productEntity : productEntityList) {
                ProductResponseDTO productResponseDTO = ProductResponseDTO.newBuilder()
                    .setMetalId(productEntity.getMetalId())
                    .setProviderId(productEntity.getProviderId())
                    .setProductName(productEntity.getProductName())
                    .setDescription(productEntity.getProductDescription())
                    .setPrice(productEntity.getProductValue())
                    .build();

                responseBuilder.addProducts(productResponseDTO);
            }

            int totalPages = productDAO.getTotalPages(getProductsByMetalCodeRequestDTO.getSize());
            int totalItems = productDAO.getTotalItems();

            return responseBuilder
                .setTotalPages(totalPages)
                .setTotalItems(totalItems)
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching products by metal code", e);
        }
    }


}

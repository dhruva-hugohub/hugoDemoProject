package com.hugo.demo.service.impl;

import java.util.Optional;

import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.DeleteProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.product.ProductResponseDTO;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.GenericException;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import com.hugo.demo.exception.RecordNotFoundException;
import com.hugo.demo.facade.ProductFacade;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductEntity;
import com.hugo.demo.product.ProductFilter;
import com.hugo.demo.service.ProductService;
import com.hugo.demo.util.ValidationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductFacade productFacade;

    @Autowired
    public ProductServiceImpl(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @Override
    @Transactional
    public ProductResponseDTO addProduct(AddProductRequestDTO addProductRequestDTO) {
        try {

            ValidationUtil.validateAddProductRequest(addProductRequestDTO);

            boolean isProductExists = productFacade.isProductExists(addProductRequestDTO.getProviderId(), addProductRequestDTO.getMetalId());
            if (isProductExists) {
                throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR,
                    "Provider with name '" + addProductRequestDTO.getProviderId() + "' already exists.");
            }

            ProductEntity addProductEntity = ProductEntity.newBuilder()
                .setMetalId(addProductRequestDTO.getMetalId())
                .setProductName(addProductRequestDTO.getProductName())
                .setProductDescription(addProductRequestDTO.getDescription())
                .setProviderId(addProductRequestDTO.getProviderId())
                .setProductValue(addProductRequestDTO.getPrice())
                .build();

            ProductEntity productResponseEntity = productFacade.addProduct(addProductEntity);

            return ProductResponseDTO.newBuilder()
                .setMetalId(productResponseEntity.getMetalId())
                .setProviderId(productResponseEntity.getProviderId())
                .setProductName(productResponseEntity.getProductName())
                .setDescription(productResponseEntity.getProductDescription())
                .setPrice(productResponseEntity.getProductValue())
                .build();

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordAlreadyExistsException e) {
            throw new RecordAlreadyExistsException(CommonStatusCode.DUPLICATE_RECORD_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(EditProductRequestDTO editProductRequestDTO) {
        try {
            ValidationUtil.validateEditProductRequest(editProductRequestDTO);

            boolean isProductExists = productFacade.isProductExists(editProductRequestDTO.getProviderId(), editProductRequestDTO.getMetalId());

            if (!isProductExists) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED,
                    "Product doesn't exist with providerId : " + editProductRequestDTO.getProviderId());
            }

            ProductEntity editProductEntity = ProductEntity.newBuilder()
                .setMetalId(editProductRequestDTO.getMetalId())
                .setProviderId(editProductRequestDTO.getProviderId())
                .setProductName(editProductRequestDTO.getProductName())
                .setProductDescription(editProductRequestDTO.getDescription())
                .setProductValue(editProductRequestDTO.getPrice())
                .build();

            ProductEntity updatedProductEntity = productFacade.updateProduct(editProductEntity);

            return ProductResponseDTO.newBuilder()
                .setMetalId(updatedProductEntity.getMetalId())
                .setProviderId(updatedProductEntity.getProviderId())
                .setProductName(updatedProductEntity.getProductName())
                .setDescription(updatedProductEntity.getProductDescription())
                .setPrice(updatedProductEntity.getProductValue())
                .build();

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    @Transactional
    public PlainResponseDTO deleteProduct(DeleteProductRequestDTO deleteProductRequestDTO) {
        try {
            boolean isProductExists = productFacade.isProductExists(deleteProductRequestDTO.getProviderId(), deleteProductRequestDTO.getMetalId());

            if (!isProductExists) {
                throw new RecordNotFoundException(CommonStatusCode.FAILED,
                    "Product doesn't exist with providerId : " + deleteProductRequestDTO.getProviderId());
            }

            boolean isDeleted = productFacade.deleteProduct(deleteProductRequestDTO.getMetalId(), deleteProductRequestDTO.getProviderId());

            if (isDeleted) {
                return PlainResponseDTO.newBuilder().setMessage("Product Deleted Successfully").build();
            } else {
                return PlainResponseDTO.newBuilder().setMessage("Couldn't Delete Product").build();
            }

        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public ProductResponseDTO fetchProductsByProviderAndMetalId(Long providerId, String metalId) {
        try {
            Optional<ProductEntity> productEntityResponse = productFacade.fetchProductDetails(providerId, metalId);
            if (productEntityResponse.isEmpty()) {
                throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, "Product doesn't exist with providerId : " + providerId);
            }
            ProductEntity productEntity = productEntityResponse.get();
            return ProductResponseDTO.newBuilder()
                .setMetalId(productEntity.getMetalId())
                .setProviderId(productEntity.getProviderId())
                .setProductName(productEntity.getProductName())
                .setDescription(productEntity.getProductDescription())
                .setPrice(productEntity.getProductValue())
                .build();
        } catch (RecordNotFoundException e) {
            throw new RecordNotFoundException(CommonStatusCode.NOT_FOUND_ERROR, e.getMessage());
        }
    }

    @Override
    public PaginatedProducts fetchProductsByProviderId(Long providerId) {
        try {
            ProductFilter productFilter = ProductFilter.newBuilder().setProductName("").setMetalId("").setStockLowerLimit(0).setStockUpperLimit(0)
                .setProductValueLowerLimit(0.00).setProductValueUpperLimit(0.00).setSortBy("").setPage(0).setPageSize(0).setProviderId(providerId)
                .build();
            return productFacade.fetchProducts(productFilter);
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public PaginatedProducts fetchProductsByMetalId(String metalId) {
        try {
            ProductFilter productFilter =
                ProductFilter.newBuilder().setProductName("").setMetalId(metalId).setStockLowerLimit(0).setStockUpperLimit(0)
                    .setProductValueLowerLimit(0.00).setProductValueUpperLimit(0.00).setSortBy("").setPage(0).setPageSize(0).setProviderId(0)
                    .build();
            return productFacade.fetchProducts(productFilter);
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public PaginatedProducts fetchProducts(ProductFilter productFilter) {
        try {
            ValidationUtil.validatePaginationInputs(productFilter.getPage(), productFilter.getPageSize());

            ValidationUtil.validateSortBy(productFilter.getSortBy(), "providerId", "metalId", "productName", "stock", "productValue");

            return productFacade.fetchProducts(productFilter);
        } catch (InvalidInputException e) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, e.getMessage());
        } catch (InternalServerErrorException e) {
            throw new GenericException(CommonStatusCode.INTERNAL_SERVER_ERROR, e);
        }
    }

}

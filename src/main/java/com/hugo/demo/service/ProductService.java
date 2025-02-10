package com.hugo.demo.service;

import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.DeleteProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.product.ProductResponseDTO;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductFilter;

public interface ProductService {

    ProductResponseDTO addProduct(AddProductRequestDTO addProductRequestDTO);

    ProductResponseDTO updateProduct(EditProductRequestDTO editProductRequestDTO);

    PlainResponseDTO deleteProduct(DeleteProductRequestDTO deleteProductRequestDTO);

    PaginatedProducts fetchProducts(ProductFilter productFilter);

    ProductResponseDTO fetchProductsByProviderAndMetalId(Long providerId, String metalId);

    PaginatedProducts fetchProductsByProviderId(Long providerId);

    PaginatedProducts fetchProductsByMetalId(String metalId);

}

package com.hugo.demo.service;

import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.AllProductsResponseDTO;
import com.hugo.demo.api.product.DeleteProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.product.GetProductsByMetalCodeRequestDTO;
import com.hugo.demo.api.product.GetProductsByMetalCodeResponseDTO;
import com.hugo.demo.api.product.GetProductsByProviderIdRequestDTO;
import com.hugo.demo.api.product.GetProductsByProviderIdResponseDTO;
import com.hugo.demo.api.product.ProductResponseDTO;

public interface ProductService {

    ProductResponseDTO addProduct(AddProductRequestDTO addProductRequestDTO);

    ProductResponseDTO updateProduct(EditProductRequestDTO editProductRequestDTO);

    void deleteProduct(DeleteProductRequestDTO deleteProductRequestDTO);

    AllProductsResponseDTO fetchAllProductDetails(int page, int size, String sortField, String sortOrder);

    GetProductsByProviderIdResponseDTO fetchProductsByProviderId(GetProductsByProviderIdRequestDTO getProductsByProviderIdRequestDTO);

    GetProductsByMetalCodeResponseDTO fetchProductsByMetalCode(GetProductsByMetalCodeRequestDTO getProductsByMetalCodeRequestDTO);
}

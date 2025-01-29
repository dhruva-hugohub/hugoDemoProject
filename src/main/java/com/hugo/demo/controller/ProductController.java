package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.AllProductsResponseDTO;
import com.hugo.demo.api.product.DeleteProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.product.GetProductsByMetalCodeRequestDTO;
import com.hugo.demo.api.product.GetProductsByMetalCodeResponseDTO;
import com.hugo.demo.api.product.GetProductsByProviderIdRequestDTO;
import com.hugo.demo.api.product.GetProductsByProviderIdResponseDTO;
import com.hugo.demo.api.product.ProductResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.ProductService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_PRODUCT_PATH)
@Tag(name = "Product APIs", description = "Product Management APIs.")
public class ProductController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/add")
    public ApiResponse addProduct(@RequestBody AddProductRequestDTO productRequestDTO) {
        String validationError = validateRequest(productRequestDTO);
        if (validationError != null) {
            return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
        }

        try {
            ProductResponseDTO responseDTO = productService.addProduct(productRequestDTO);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);
        } catch (Exception e) {
            LOGGER.error("Error processing add product", e);
            return ResponseUtil.buildEmptyResponse(CommonStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/{productId}")
//    public ApiResponse getProduct(@PathVariable int productId) {
//        try {
//            ProductResponseDTO productResponseDTO = productService.fetchProductDetail(productId);
//            if (productResponseDTO != null) {
//                return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, productResponseDTO);
//            } else {
//                return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
//            }
//        } catch (Exception e) {
//            LOGGER.error("Error fetching product details", e);
//            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
//        }
//    }

    @PutMapping("/edit")
    public ApiResponse updateProduct(@RequestBody EditProductRequestDTO productRequestDTO) {
        String validationError = validateRequest(productRequestDTO);
        if (validationError != null) {
            return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
        }

        try {
            ProductResponseDTO updatedProductDTO = productService.updateProduct(productRequestDTO);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, updatedProductDTO);
        } catch (Exception e) {
            LOGGER.error("Error processing update product", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GetMapping("/all")
    public ApiResponse getAllProducts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortField,
        @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        try {
            AllProductsResponseDTO allProducts = productService.fetchAllProductDetails(page, size, sortField, sortOrder);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, allProducts);
        } catch (Exception e) {
            LOGGER.error("Error fetching all products", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GetMapping("/provider/{providerId}")
    public ApiResponse getProductsByProviderId(
        @PathVariable int providerId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortField,
        @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        try {
            GetProductsByProviderIdRequestDTO getProductsByProviderIdRequestDTO = GetProductsByProviderIdRequestDTO.newBuilder().setProviderId(providerId).setPage(page).setSize(size).setSortField(sortField).setSortOrder(sortOrder).build();
            GetProductsByProviderIdResponseDTO products = productService.fetchProductsByProviderId(getProductsByProviderIdRequestDTO);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, products);
        } catch (Exception e) {
            LOGGER.error("Error fetching products by provider ID", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GetMapping("/metalCode/{metalCode}")
    public ApiResponse getProductsByMetalCode(
        @PathVariable String metalCode,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortField,
        @RequestParam(defaultValue = "asc") String sortOrder
    ) {
        try {
            GetProductsByMetalCodeRequestDTO getProductsByMetalCodeRequestDTO = GetProductsByMetalCodeRequestDTO.newBuilder().setMetalId(metalCode).setPage(page).setSize(size).setSortField(sortField).setSortOrder(sortOrder).build();
            GetProductsByMetalCodeResponseDTO products = productService.fetchProductsByMetalCode(getProductsByMetalCodeRequestDTO);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, products);
        } catch (Exception e) {
            LOGGER.error("Error fetching products by metal code", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @DeleteMapping("/delete")
    public ApiResponse deleteProduct(@RequestBody DeleteProductRequestDTO productRequestDTO) {
        try {

            productService.deleteProduct(productRequestDTO);
            if (true) {
                return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, null);
            } else {
                return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting product", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    private String validateRequest(Object requestDTO) {
        if (requestDTO instanceof AddProductRequestDTO productRequest) {
            if (productRequest.getProductName() == null || productRequest.getProductName().isEmpty()) {
                return "Product name cannot be empty";
            }
            if (productRequest.getProviderId() <= 0) {
                return "Provider ID is required";
            }
        }
        return null;
    }
}


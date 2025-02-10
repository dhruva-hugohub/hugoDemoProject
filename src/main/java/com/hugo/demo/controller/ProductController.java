package com.hugo.demo.controller;

import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.DeleteProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.product.ProductResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.product.PaginatedProducts;
import com.hugo.demo.product.ProductFilter;
import com.hugo.demo.service.ProductService;
import com.hugo.demo.util.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/add")
    public ApiResponse addProduct(@RequestBody AddProductRequestDTO productRequestDTO) {
        ProductResponseDTO responseDTO = productService.addProduct(productRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);
    }

    @PutMapping("/edit")
    public ApiResponse updateProduct(@RequestBody EditProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProductDTO = productService.updateProduct(productRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, updatedProductDTO);
    }

    @GetMapping("/all")
    public ApiResponse getAllProducts(
        @RequestParam(required = false, defaultValue = "") String productName,
        @RequestParam(required = false, defaultValue = "") String metalId,
        @RequestParam(required = false, defaultValue = "0") Integer providerId,
        @RequestParam(required = false, defaultValue = "0") Integer stockLowerLimit,
        @RequestParam(required = false, defaultValue = "0") Integer stockUpperLimit,
        @RequestParam(required = false, defaultValue = "0.00") Double productValueLowerLimit,
        @RequestParam(required = false, defaultValue = "0.00") Double productValueUpperLimit,
        @RequestParam(defaultValue = "productName") String sortBy,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        ProductFilter productFilter =
            ProductFilter.newBuilder().setProviderId(providerId).setProductName(productName).setMetalId(metalId).setStockLowerLimit(stockLowerLimit)
                .setStockUpperLimit(stockUpperLimit).setProductValueLowerLimit(productValueLowerLimit)
                .setProductValueUpperLimit(productValueUpperLimit).setSortBy(sortBy).setPage(page).setPageSize(pageSize).build();
        PaginatedProducts paginatedProducts = productService.fetchProducts(productFilter);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, paginatedProducts);
    }


    @DeleteMapping("/delete")
    public ApiResponse deleteProduct(@RequestBody DeleteProductRequestDTO productRequestDTO) {
        PlainResponseDTO plainResponseDTO = productService.deleteProduct(productRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, plainResponseDTO);
    }

}


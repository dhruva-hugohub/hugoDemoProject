package com.hugo.demo.controller;


import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.plainResponseProto.PlainResponseDTO;
import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.provider.PaginatedProviders;
import com.hugo.demo.api.provider.ProviderResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.ProviderService;
import com.hugo.demo.util.ResponseUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping(value = URLConstants.V1_PROVIDER_PATH)
@Tag(name = "Provider APIs", description = "Provider Management APIs.")
public class ProviderController {

    private final ProviderService providerService;

    @Autowired
    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping(value = "/add")
    public ApiResponse addProvider(@RequestBody AddProviderRequestDTO providerRequestDTO) {
        ProviderResponseDTO response = providerService.addProviderDetails(providerRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, response);
    }

    @GetMapping("/{providerId}")
    public ApiResponse getProvider(@PathVariable int providerId) {
        ProviderResponseDTO providerResponseDTO = providerService.fetchProviderDetailsById(providerId);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, providerResponseDTO);
    }

    @PutMapping("/edit")
    public ApiResponse updateProvider(@RequestBody EditProviderRequestDTO providerRequestDTO) {
        ProviderResponseDTO updatedProviderDTO = providerService.editProviderDetails(providerRequestDTO);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, updatedProviderDTO);
    }

    @DeleteMapping("/{providerId}")
    public ApiResponse deleteProvider(@PathVariable int providerId) {
            PlainResponseDTO deleted = providerService.deleteProviderDetails(providerId);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, deleted);
    }

    @GetMapping("/fetch-providers")
    public ApiResponse fetchProviders(
        @RequestParam(required = false, defaultValue = "") String providerName,
        @RequestParam(required = false, defaultValue = "providerId") String sortBy,
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "10") int pageSize) {

        PaginatedProviders paginatedProviders = providerService.fetchProviders(providerName, sortBy, page, pageSize);
        return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, paginatedProviders);
    }
}

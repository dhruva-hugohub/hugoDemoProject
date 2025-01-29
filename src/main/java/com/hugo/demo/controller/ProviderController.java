package com.hugo.demo.controller;


import com.common.utility.proto.ApiResponse;
import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.AllProvidersResponseDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.provider.ProviderResponseDTO;
import com.hugo.demo.constants.URLConstants;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.service.ProviderService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = URLConstants.V1_PROVIDER_PATH)
@Tag(name = "Provider APIs", description = "Provider Management APIs.")
public class ProviderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderController.class);

    private final ProviderService providerService;

    @Autowired
    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping(value = "/add")
    public ApiResponse addProvider(@RequestBody AddProviderRequestDTO providerRequestDTO) {
        String validationError = validateRequest(providerRequestDTO);
        if (validationError != null) {
            return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
        }

        try {
            ProviderResponseDTO responseDTO = providerService.addProviderDetails(providerRequestDTO);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, responseDTO);
        } catch (Exception e) {
            LOGGER.error("Error processing add provider", e);
            return ResponseUtil.buildEmptyResponse(CommonStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{providerId}")
    public ApiResponse getProvider(@PathVariable int providerId) {
        try {
            ProviderResponseDTO providerResponseDTO = providerService.fetchProviderDetails(providerId);
            if (providerResponseDTO != null) {
                return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, providerResponseDTO);
            } else {
                return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching provider details", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @PutMapping("/edit")
    public ApiResponse updateProvider(@RequestBody EditProviderRequestDTO providerRequestDTO) {
        String validationError = validateRequest(providerRequestDTO);
        if (validationError != null) {
            return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
        }

        try {
            ProviderResponseDTO updatedProviderDTO = providerService.editProviderDetails(providerRequestDTO);
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, updatedProviderDTO);
        } catch (Exception e) {
            LOGGER.error("Error processing update provider", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @GetMapping("/all")
    public ApiResponse getAllProviders() {
        try {
            AllProvidersResponseDTO allProviders = providerService.fetchAllProviderDetails();
            return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, allProviders);
        } catch (Exception e) {
            LOGGER.error("Error fetching all providers", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    @DeleteMapping("/{providerId}")
    public ApiResponse deleteProvider(@PathVariable int providerId) {
        try {
            boolean deleted = providerService.deleteProviderDetails(providerId);
            if (deleted) {
                return ResponseUtil.buildResponse(CommonStatusCode.SUCCESS, null);
            } else {
                return ResponseUtil.buildResponse(CommonStatusCode.BAD_REQUEST_ERROR, null);
            }
        } catch (Exception e) {
            LOGGER.error("Error deleting provider", e);
            return ResponseUtil.buildResponse(CommonStatusCode.INTERNAL_SERVER_ERROR, null);
        }
    }

    private String validateRequest(Object requestDTO) {
        if (requestDTO instanceof AddProviderRequestDTO providerRequest) {
            providerRequest.getProviderName();
            if (providerRequest.getProviderName().isEmpty()) {
                return "Provider name cannot be empty";
            }
            providerRequest.getProviderAPIUrl();
            if (providerRequest.getProviderAPIUrl().isEmpty()) {
                return "Provider API URL cannot be empty";
            }
        }
        return null;
    }
}

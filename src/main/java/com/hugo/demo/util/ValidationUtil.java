package com.hugo.demo.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hugo.demo.api.alert.CreateAlertRequestDTO;
import com.hugo.demo.api.alert.EditAlertRequestDTO;
import com.hugo.demo.api.order.CreateOrderRequestDTO;
import com.hugo.demo.api.order.EditOrderRequestDTO;
import com.hugo.demo.api.product.AddProductRequestDTO;
import com.hugo.demo.api.product.EditProductRequestDTO;
import com.hugo.demo.api.provider.AddProviderRequestDTO;
import com.hugo.demo.api.provider.EditProviderRequestDTO;
import com.hugo.demo.api.user.EditUserPasswordRequestDTO;
import com.hugo.demo.api.user.EditUserPinRequestDTO;
import com.hugo.demo.api.user.EditUserRequestDTO;
import com.hugo.demo.api.user.UserLoginRequestDTO;
import com.hugo.demo.api.user.UserRegisterRequestDTO;
import com.hugo.demo.api.user.UserVerifyPinRequestDTO;
import com.hugo.demo.api.userquantity.CreateUserQuantityRequestDTO;
import com.hugo.demo.api.userquantity.EditUserQuantityRequestDTO;
import com.hugo.demo.api.wallet.CreateWalletRequestDTO;
import com.hugo.demo.api.wallet.EditWalletRequestDTO;
import com.hugo.demo.constants.RegexConstants;
import com.hugo.demo.enums.alertType.TypeOfAlert;
import com.hugo.demo.enums.typeOfTransaction.TransactionType;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InvalidInputException;

public class ValidationUtil {

    public static void validateUserRegisterRequest(UserRegisterRequestDTO dto) {
        if (dto == null || dto.getAllFields().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }

        validateField(dto.getName(), "name", "Invalid name format");
        validateField(dto.getEmail(), "email", "Invalid email format");
        validateField(dto.getPhoneNumber(), "phoneNumber", "Invalid phone number format");
        validateField(dto.getPassword(), "password", "Invalid password format");
        validateField(dto.getPin(), "pin", "Invalid pin format");
    }

    public static void validateUserEditRequest(EditUserRequestDTO dto) {
        if (dto == null || dto.getAllFields().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }
        if (!dto.getPhoneNumber().isEmpty()) {
            validateField(dto.getPhoneNumber(), "phoneNumber", "Invalid phone number format");
        }
        if (!dto.getName().isEmpty()) {
            validateField(dto.getName(), "name", "Invalid name format");
        }
    }

    public static void validateEditPasswordRequest(EditUserPasswordRequestDTO dto) {
        if (dto == null || dto.getAllFields().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }
        validateField(dto.getEmail(), "email", "Invalid email format");
        validateField(dto.getNewPassword(), "password", "Invalid password format");
        validateField(dto.getConfirmPassword(), "password", "Invalid password format");

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Confirm password does not match new password");
        }
    }

    public static void validateEditPinRequest(EditUserPinRequestDTO dto) {
        if (dto == null || dto.getAllFields().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }
        validateField(dto.getEmail(), "email", "Invalid email format");
        validateField(dto.getNewPin(), "pin", "Invalid pin format");
        validateField(dto.getConfirmPin(), "pin", "Invalid pin format");

        if (!dto.getNewPin().equals(dto.getConfirmPin())) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Confirm pin does not match new pin");
        }
    }

    public static void validateVerifyPinRequest(UserVerifyPinRequestDTO dto) {
        if (dto == null || dto.getAllFields().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }

        validateField(dto.getPin(), "pin", "Invalid pin format");
    }

    public static void validateUserLoginRequest(UserLoginRequestDTO dto) {
        if (dto == null || dto.getAllFields().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request body cannot be null");
        }

    }

    private static void validateField(String requestObject, String objectType, String errorMessage) {
        if (requestObject == null || objectType == null || requestObject.trim().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request Object is empty" + objectType);
        }

        Pattern pattern = switch (objectType) {
            case "userId" -> RegexConstants.userIdPattern;
            case "email" -> RegexConstants.emailPattern;
            case "phoneNumber" -> RegexConstants.phoneNumberPattern;
            case "password" -> RegexConstants.passwordPattern;
            case "pin" -> RegexConstants.pinPattern;
            default -> RegexConstants.namePattern;
        };

        Matcher matcher = pattern.matcher(requestObject);
        if (!matcher.matches()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, errorMessage);
        }
    }

    public static void validateAddProviderRequest(AddProviderRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }

        if (isNullOrEmpty(requestDTO.getProviderAPIUrl())) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provider API URL cannot be null or empty.");
        }

        validProviderFields(requestDTO.getProviderName(), requestDTO.getSchedulerTimePeriod());
    }

    public static void validateAddProductRequest(AddProductRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }

        validProductFields(requestDTO.getProviderId(), requestDTO.getMetalId());
    }

    public static void validateEditProductRequest(EditProductRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }

        validProductFields(requestDTO.getProviderId(), requestDTO.getMetalId());
    }


    public static void validateEditProviderRequest(EditProviderRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }

        if (isNullOrEmpty(String.valueOf(requestDTO.getProviderId())) || requestDTO.getProviderId() <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provider Id must be greater than 0.");
        }

        validProviderFields(requestDTO.getProviderName(), requestDTO.getSchedulerTimePeriod());
    }

    private static void validProviderFields(String providerName, String schedulerTimePeriod) {
        if (isNullOrEmpty(providerName)) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provider name cannot be null or empty.");
        }


        if (isNullOrEmpty(schedulerTimePeriod) || Integer.parseInt(schedulerTimePeriod) <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Scheduler time period must be greater than 0.");
        }
    }

    private static void validProductFields(long providerId, String metalId) {
        if (isNullOrEmpty(String.valueOf(providerId)) || providerId <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provider Id cannot be null or empty.");
        }

        if (isNullOrEmpty(metalId)) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Metal Id cannot be null or empty.");
        }

    }

    public static void validOrderFields(long orderId, String currencyCode) {
        if (isNullOrEmpty(String.valueOf(orderId)) || orderId <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Order Id cannot be null or empty.");
        }
        validateCurrencyCode(currencyCode);
    }


    public static void validatePaginationInputs(int page, int pageSize) {
        if (page < 1) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Page number must be 1 or greater.");
        }
        if (pageSize < 1) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Page size must be 1 or greater.");
        }
    }


    public static void validateSortBy(String sortBy, String... allowedFields) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "SortBy field cannot be null or empty.");
        }

        List<String> validFields = Arrays.asList(allowedFields);
        if (!validFields.contains(sortBy)) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR,
                "Invalid sorting field: " + sortBy + ". Allowed values: " + validFields);
        }
    }

    public static void validateCreateOrderRequest(CreateOrderRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        } else if (requestDTO.getTransactionType() == TransactionType.UNRECOGNIZED || requestDTO.getTransactionType() == TransactionType.UNKNOWN) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Transaction Type for Create Order.");
        } else if (requestDTO.hasAmount() && requestDTO.getTransactionType() == TransactionType.BUY) {
            if (requestDTO.getProviderId() <= 0) {
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Provider Id for Create Order.");
            }
            if (requestDTO.getAmount() <= 0) {
                throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Amount for Create Order.");
            }
        } else if (requestDTO.hasQuantity() && requestDTO.getTransactionType() == TransactionType.SELL) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Quantity for Create Order.");
        } else if (requestDTO.getMetalId().trim().length() != 3) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Metal Id for Create Order.");
        } else if (requestDTO.getUserId() <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter User Id for Create Order.");
        }
    }

    public static void validateUpdateOrderRequest(EditOrderRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }
    }

    public static void validateWalletCreateRequest(CreateWalletRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }

        if (requestDTO.getUserId() <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "User Id cannot be empty or less or equal to 0.");
        }
    }

    public static void validateEditWalletRequest(EditWalletRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }
        if (requestDTO.getUserId() <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "User Id cannot be empty or less or equal to 0.");
        }

        if (requestDTO.getWalletId() <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Wallet Id cannot be empty or less or equal to 0.");
        }
    }

    public static void validateCreateUserQuantity(CreateUserQuantityRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }
        validateUserQuantityFields(requestDTO.getUserId(), requestDTO.getQuantity(), requestDTO.getMetalId());
    }

    public static void validateUpdateUserQuantity(EditUserQuantityRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }
        validateUserQuantityFields(requestDTO.getUserId(), requestDTO.getQuantity(), requestDTO.getMetalId());
    }

    private static void validateUserQuantityFields(long userId, double quantity, String metalId) {
        if (userId <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "User Id cannot be empty or less or equal to 0.");
        }

        if (quantity <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Quantity cannot be empty or less or equal to 0.");
        }

        if (metalId.trim().length() != 3) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Metal Id for Create User Metal Quantity.");
        }
    }

    public static void validateCreateAlertRequest(CreateAlertRequestDTO requestDTO) {
        validateAlertFields(requestDTO == null, requestDTO.getUserId(), requestDTO.getMetalId(), requestDTO.getProviderId(), requestDTO.getEmail(),
            requestDTO.getMinPrice(), requestDTO.getMaxPrice(), requestDTO.getFcmToken());

        boolean isValid = Arrays.stream(TypeOfAlert.values())
            .anyMatch(type -> type == requestDTO.getTypeOfAlert());

        if (!isValid) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Type of Alert is not valid.");
        }

        validateCurrencyCode(requestDTO.getCurrencyCode());

    }

    public static void validateEditAlertRequest(EditAlertRequestDTO requestDTO) {
        validateAlertFields(requestDTO == null, requestDTO.getUserId(), requestDTO.getMetalId(), requestDTO.getProviderId(), requestDTO.getEmail(),
            requestDTO.getMinPrice(), requestDTO.getMaxPrice(), requestDTO.getFcmToken());

        validateCurrencyCode(requestDTO.getCurrencyCode());
    }

    private static void validateAlertFields(boolean b, long userId, String metalId, long providerId, String email, double minPrice, double maxPrice,
                                            String fcmToken) {
        if (b) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Request cannot be null.");
        }

        if (userId <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "User Id cannot be empty or less or equal to 0.");
        }

        if (metalId.trim().length() != 3) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Metal Id for Create Alert Quantity.");
        }

        if (providerId <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provider Id cannot be empty or less or equal to 0.");
        }

        if (email.trim().length() <= 3) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Email for Create Alert Quantity.");
        }

        if (minPrice <= 0.00) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "MinPrice cannot be less or equal to 0.");
        }

        if (maxPrice <= 0.00) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "MaxPrice cannot be less or equal to 0.");
        }

        if (fcmToken.trim().length() <= 5) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid FCM Token for Create Alert Quantity.");
        }
    }

    private static void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.length() != 3) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Currency Code for Create Alert Quantity.");
        }
    }

    public static void validLiveItemFields(String metalCode, long providerId, String currencyCode) {
        if (metalCode == null || metalCode.length() != 3) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Enter Valid Metal Code for Create Live Item Record.");
        }
        if (providerId <= 0) {
            throw new InvalidInputException(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR, "Provider Id cannot be empty or less or equal to 0.");
        }

        validateCurrencyCode(currencyCode);
    }


    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}


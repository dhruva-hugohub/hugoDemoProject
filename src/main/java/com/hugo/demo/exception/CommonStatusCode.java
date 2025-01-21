package com.hugo.demo.exception;

import java.util.List;

import com.hugo.demo.api.ApiStatusCode;

public enum CommonStatusCode implements ApiStatusCode {
    SUCCESS("200", "Success"),
    FAILED("500", "Failed"),

    GTW_UNAUTHORIZED("GSM-401", "Unauthorized"),
    GTW_BAD_REQUEST_ERROR("GSM-9400", "Bad Request"),
    GTW_INTERNAL_SERVER_ERROR("GSM-9500", "Internal Server Error"),

    BAD_REQUEST_ERROR("E9400", "Bad Request"),
    UNAUTHORIZED_ERROR("E9401", "User is not authorised to perform the request"),
    FORBIDDEN_ERROR("E9403", "User is forbidden to process the request"),
    NOT_FOUND_ERROR("E9404", "The requested representation is not found"),
    METHOD_NOT_ALLOWED_ERROR("E9405", "Invalid input provider or bad request"),
    NOT_ACCEPTABLE_ERROR("E9406", "The given request is not in acceptable state"),
    ILLEGAL_ARGUMENT_ERROR("E9409", "Illegal argument exception"),
    ILLEGAL_STATE_ERROR("E9409", "Illegal state exception"),
    INVALID_ORIGIN("E9410", "Invalid Origin is passed"),
    INVALID_MEDIA_TYPE_ERROR("E9415", "Invalid media type provided"),
    UNPROCESSABLE_ENTITY_ERROR("E9422", "Unable to process the given request"),
    TOO_MANY_REQUESTS_ERROR("E9429", "Too many requests"),
    INTERNAL_SERVER_ERROR("E9500", "An error occurred while processing the request"),
    QUEUE_NOT_FOUND_ERROR("EUM_E0001", "Queue implementation not found"),
    INVALID_TOPIC("EUM_E0002", "Topic not found"),
    INVALID_STORAGE_PROVIDER("EUM_E0003", "Storage Provider not found"),
    MISSING_PARAMETER_EXCEPTION("E9400", "Missing Parameter"),
    INVALID_LOCATION_COORDINATES("E9407", "Invalid Location Coordinates are passed"),

    CUSTOMER_PROFILE_HEADER("E400", "Header[X-CUSTOMER-PROFILE-ID] is missing");

    private final String statusCode;
    private final String message;

    CommonStatusCode(String statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    //Will remove later
    @Deprecated
    public static ApiStatusCode buildMissingParameterErrorHeader(List<String> params) {
        String message = String.format("Missing params [%s]", params);
        return new ApiStatusCode() {
            @Override
            public String getStatusCode() {
                return MISSING_PARAMETER_EXCEPTION.getStatusCode();
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    @Override
    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}


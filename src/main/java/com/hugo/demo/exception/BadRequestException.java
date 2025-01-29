package com.hugo.demo.exception;

import com.hugo.demo.api.ApiStatusCode;

public class BadRequestException extends RuntimeException {
    private final ApiStatusCode errorStatusCode;

    public BadRequestException() {
        super(CommonStatusCode.BAD_REQUEST_ERROR.getMessage());
        this.errorStatusCode = CommonStatusCode.BAD_REQUEST_ERROR;
    }

    public BadRequestException(String message) {
        super(message);
        this.errorStatusCode = CommonStatusCode.BAD_REQUEST_ERROR;
    }

    public BadRequestException(String message, Throwable e) {
        super(message, e);
        this.errorStatusCode = CommonStatusCode.BAD_REQUEST_ERROR;
    }

    public BadRequestException(ApiStatusCode apiStatusCode) {
        super(apiStatusCode.getMessage());
        errorStatusCode = apiStatusCode;
    }

    public BadRequestException(ApiStatusCode apiStatusCode,  String message) {
        super(message);
        errorStatusCode = apiStatusCode;
    }

    public ApiStatusCode getErrorStatusCode() {
        return errorStatusCode;
    }
}


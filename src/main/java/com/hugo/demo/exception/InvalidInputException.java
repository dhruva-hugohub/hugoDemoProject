package com.hugo.demo.exception;

public class InvalidInputException extends RuntimeException{
    private final CommonStatusCode statusCode;

    public InvalidInputException(CommonStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public CommonStatusCode getStatusCode() {
        return statusCode;
    }
}

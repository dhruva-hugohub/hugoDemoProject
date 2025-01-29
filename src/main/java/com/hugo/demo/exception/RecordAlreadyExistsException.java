package com.hugo.demo.exception;

public class RecordAlreadyExistsException extends RuntimeException {
    private final CommonStatusCode statusCode;
    public RecordAlreadyExistsException(CommonStatusCode statusCode,String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public CommonStatusCode getStatusCode() {
        return statusCode;
    }
}

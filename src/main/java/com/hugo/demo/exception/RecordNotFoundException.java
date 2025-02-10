package com.hugo.demo.exception;

public class RecordNotFoundException extends RuntimeException {
    private final CommonStatusCode statusCode;
    public RecordNotFoundException(CommonStatusCode statusCode,String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public CommonStatusCode getStatusCode() {
        return statusCode;
    }
}

package com.hugo.demo.exception;

import com.hugo.demo.api.ApiStatusCode;

public class GenericException extends RuntimeException {
  private final ApiStatusCode errorStatusCode;

    public GenericException(ApiStatusCode errorStatusCode) {
      super(errorStatusCode.getMessage());
      this.errorStatusCode = errorStatusCode;
    }

    public GenericException(ApiStatusCode errorStatusCode, Exception e) {
      super(errorStatusCode.getMessage(), e);
      this.errorStatusCode = errorStatusCode;
    }

    public GenericException(String message) {
      super(message);
      this.errorStatusCode = CommonStatusCode.INTERNAL_SERVER_ERROR;
    }

    public GenericException(Exception e) {
      super(e);
      this.errorStatusCode = CommonStatusCode.INTERNAL_SERVER_ERROR;
    }

    public ApiStatusCode getErrorStatusCode() {
      return errorStatusCode;
    }
  }


package com.hugo.demo.exception.handler;

import static com.hugo.demo.util.ResponseUtil.buildApiResponseHeader;

import com.common.utility.proto.ApiResponse;
import com.common.utility.proto.ApiResponseHeader;
import com.hugo.demo.exception.CommonStatusCode;
import com.hugo.demo.exception.InternalServerErrorException;
import com.hugo.demo.exception.InvalidInputException;
import com.hugo.demo.exception.RecordAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {InvalidInputException.class, IllegalArgumentException.class})
    public ApiResponse handleInvalidInputException(Exception ex) {
        LOG.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        return buildApiResponse(buildApiResponseHeader(CommonStatusCode.ILLEGAL_ARGUMENT_ERROR));
    }

    @ExceptionHandler(value = {RecordAlreadyExistsException.class})
    public ApiResponse handleDuplicateRecordException(RecordAlreadyExistsException ex) {
        LOG.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        return buildApiResponse(buildApiResponseHeader(CommonStatusCode.DUPLICATE_RECORD_ERROR));
    }

    @ExceptionHandler(value = {HttpServerErrorException.InternalServerError.class, NullPointerException.class, NoSuchMethodException.class,
        InternalServerErrorException.class})
    protected ApiResponse handleInternalServerError(Exception ex) {
        LOG.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        return buildApiResponse(buildApiResponseHeader(CommonStatusCode.INTERNAL_SERVER_ERROR));
    }

    private ApiResponse buildApiResponse(ApiResponseHeader errorResponseHeader) {
        return ApiResponse.newBuilder().setHeaders(errorResponseHeader).build();
    }

}

package com.hugo.demo.util;

import java.util.Map;

import com.common.utility.proto.ApiResponse;
import com.common.utility.proto.ApiResponseHeader;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.hugo.demo.api.ApiStatusCode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility to convert the given data to the required response format.
 */
public class ResponseUtil {
    public static ApiResponse buildEmptyResponse(ApiStatusCode headers) {
        return ApiResponse.newBuilder()
            .setHeaders(buildApiResponseHeader(headers))
            .build();
    }

    /**
     * This Method takes the given inputs and returns ApiResponse.
     *
     * @param headers headers we need to pass to the UI/other caller apps.
     * @param data    Data to be returned to clients.
     * @return ApiResponse incorporated with the above input params.
     */
    public static ApiResponse buildResponse(ApiStatusCode headers, Message data) {
        ApiResponse.Builder builder = ApiResponse.newBuilder()
            .setHeaders(buildApiResponseHeader(headers));
        if (data != null) {
            builder.setData(Any.pack(data));
        }
        return builder.build();
    }

//    /**
//     * This Method takes the given inputs and returns ApiResponse.
//     *
//     * @param headers        headers we need to pass to the UI/other caller apps.
//     * @param paginationData Paginated data to return to clients.
//     * @return ApiResponse incorporated with the above input params.
//     */
//    public static <T extends Message> ApiResponse buildPaginatedResponse(ApiStatusCode headers,
//                                                                         PaginationUtils.PaginatedResponseRecord<T> paginationData) {
//        return ApiResponse.newBuilder()
//            .setHeaders(buildApiResponseHeader(headers))
//            .setData(Any.pack(paginationData.dto()))
//            .setPageToken(paginationData.pageToken())
//            .setHasMorePages(paginationData.hasMorePages())
//            .setReversePageToken(paginationData.reversePageToken())
//            .setHasMoreReversePages(paginationData.hasMoreReversePages())
//            .build();
//    }

    public static ApiResponseHeader buildApiResponseHeader(ApiStatusCode headers) {
        return ApiResponseHeader.newBuilder()
            .setStatusCode(headers.getStatusCode())
            .setMessage(headers.getMessage())
            .build();
    }

    public static ApiResponseHeader buildApiResponseHeader(String statusCode, String message) {
        return ApiResponseHeader.newBuilder()
            .setStatusCode(statusCode)
            .setMessage(message)
            .build();
    }

    public static Map<String, Object> buildEmptyResponseMap(ApiStatusCode statusCode, String traceId) {
        return ImmutableMap.<String, Object>builder()
            .put(
                "headers", ImmutableMap.<String, String>builder()
                    .put("status_code", statusCode.getStatusCode())
                    .put("message", statusCode.getMessage())
                    .put("trace_id", traceId).build()
            ).build();
    }

    public static Map<String, Object> buildEmptyResponseMap(ApiStatusCode statusCode, @Nullable String message, String traceId) {
        return ImmutableMap.<String, Object>builder()
            .put(
                "headers", ImmutableMap.<String, String>builder()
                    .put("status_code", statusCode.getStatusCode())
                    .put("message", message == null ? statusCode.getMessage() : message)
                    .put("trace_id", traceId).build()
            ).build();
    }

    private ResponseUtil() {
    }
}


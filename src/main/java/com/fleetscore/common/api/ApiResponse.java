package com.fleetscore.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(Instant timestamp, boolean success, int status, String code, String message, String path, T data) {

    public static <T> ApiResponse<T> ok(T data, String code, String message, int status, String path) {
        return new ApiResponse<>(Instant.now(), true, status, code, message, path, data);
    }

    public static ApiResponse<NoContent> ok(String code, String message, int status, String path) {
        return new ApiResponse<>(Instant.now(), true, status, code, message, path, new NoContent());
    }

    public static <T> ApiResponse<T> error(String code, String message, int status, String path, T data) {
        return new ApiResponse<>(Instant.now(), false, status, code, message, path, data);
    }

    public static <T> ApiResponse<T> error(String code, String message, int status, String path) {
        return new ApiResponse<>(Instant.now(), false, status, code, message, path, null);
    }
}

package com.fleetscore.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(Instant timestamp, boolean success, int status, String message, String path, T data) {

    public static <T> ApiResponse<T> ok(T data, String message, int status, String path) {
        return new ApiResponse<>(Instant.now(), true, status, message, path, data);
    }

    public static ApiResponse<NoContent> ok(String message, int status, String path) {
        return new ApiResponse<>(Instant.now(), true, status, message, path, new NoContent());
    }

    public static <T> ApiResponse<T> error(String message, int status, String path, T data) {
        return new ApiResponse<>(Instant.now(), false, status, message, path, data);
    }

    public static <T> ApiResponse<T> error(String message, int status, String path) {
        return new ApiResponse<>(Instant.now(), false, status, message, path, null);
    }
}

package com.fleetscore.common.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import com.fleetscore.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.accept.NotAcceptableApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex,
                                                                    HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({EmailAlreadyInUseException.class, DuplicateResourceException.class})
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(RuntimeException ex,
                                                             HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({TokenExpiredException.class, TokenAlreadyUsedException.class, InvalidTokenException.class})
    public ResponseEntity<ApiResponse<Void>> handleTokenErrors(RuntimeException ex,
                                                               HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
            MissingApiVersionException.class,
            InvalidApiVersionException.class,
            NotAcceptableApiVersionException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleApiVersioning(RuntimeException ex,
                                                                 HttpServletRequest request) {
        String message = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Invalid API version"
                : ex.getMessage();
        ApiResponse<Void> body = ApiResponse.error(message, HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex,
                                                                             HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        ApiResponse<Map<String, String>> body = ApiResponse.error(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraint(ConstraintViolationException ex,
                                                                             HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("errors", ex.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList());
        ApiResponse<Map<String, Object>> body = ApiResponse.error(
                "Constraint violation",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                data
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex,
                                                                HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex,
                                                                HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex,
                                                                   HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex,
                                                          HttpServletRequest request) {
        ApiResponse<Void> body = ApiResponse.error("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

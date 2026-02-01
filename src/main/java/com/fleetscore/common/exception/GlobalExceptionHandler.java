package com.fleetscore.common.exception;

import com.fleetscore.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex,
                                                                    HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailInUse(EmailAlreadyInUseException ex,
                                                              HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("EMAIL_IN_USE", ex.getMessage(), HttpStatus.CONFLICT.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex,
                                                             HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("DUPLICATE_RESOURCE", ex.getMessage(), HttpStatus.CONFLICT.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpired(TokenExpiredException ex,
                                                                HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("TOKEN_EXPIRED", ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenAlreadyUsed(TokenAlreadyUsedException ex,
                                                                    HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("TOKEN_ALREADY_USED", ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex,
                                                                HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("INVALID_TOKEN", ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
            MissingApiVersionException.class,
            InvalidApiVersionException.class,
            NotAcceptableApiVersionException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleApiVersioning(RuntimeException ex,
                                                                 HttpServletRequest request) {
        logException(request, ex);
        String message = (ex.getMessage() == null || ex.getMessage().isBlank())
                ? "Invalid API version"
                : ex.getMessage();
        ApiResponse<Void> body = ApiResponse.error("INVALID_API_VERSION", message, HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex,
                                                                             HttpServletRequest request) {
        logException(request, ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        ApiResponse<Map<String, String>> body = ApiResponse.error(
                "VALIDATION_FAILED",
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
        logException(request, ex);
        Map<String, Object> data = new HashMap<>();
        data.put("errors", ex.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList());
        ApiResponse<Map<String, Object>> body = ApiResponse.error(
                "CONSTRAINT_VIOLATION",
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
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("ACCESS_DENIED", ex.getMessage(), HttpStatus.FORBIDDEN.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex,
                                                                HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("ILLEGAL_STATE", ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex,
                                                                   HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("ILLEGAL_ARGUMENT", ex.getMessage(), HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex,
                                                          HttpServletRequest request) {
        logException(request, ex);
        ApiResponse<Void> body = ApiResponse.error("INTERNAL_ERROR", "Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private void logException(HttpServletRequest request, Exception ex) {
        log.warn("Controller exception: {} {} -> {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
    }
}

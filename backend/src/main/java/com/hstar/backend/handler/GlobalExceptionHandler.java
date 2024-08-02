package com.hstar.backend.handler;

import com.hstar.backend.dto.ApiResponse;
import com.hstar.backend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({AuthenticationFailedException.class, InvalidTokenException.class, AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationExceptions(Exception ex) {
        String message = ex instanceof AuthenticationException ?
                "Authentication failed: " + ex.getMessage() : ex.getMessage();
        return createErrorResponse(HttpStatus.UNAUTHORIZED, message);
    }

    @ExceptionHandler({RateLimitException.class, ForbiddenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<String>> handleForbiddenExceptions(Exception ex) {
        String prefix = ex instanceof RateLimitException ? "Rate Limit Exceeded: " : "Access Forbidden: ";
        return createErrorResponse(HttpStatus.FORBIDDEN, prefix + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }

    private ResponseEntity<ApiResponse<String>> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(null, message));
    }
}
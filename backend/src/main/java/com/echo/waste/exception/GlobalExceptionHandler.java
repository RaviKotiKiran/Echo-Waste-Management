package com.echo.waste.exception;

import com.echo.waste.dto.ApiDtos;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiDtos.Message> api(ApiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(new ApiDtos.Message(exception.getMessage()));
    }

    // Method-security denials are thrown inside the controller proxy, so the generic
    // Exception handler would otherwise turn them into 500 responses before the
    // security filter chain can translate them. Handle them explicitly instead.
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiDtos.Message> accessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiDtos.Message("You do not have permission to perform this action."));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiDtos.Message> authentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiDtos.Message("Authentication required."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiDtos.Message> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .sorted(java.util.Comparator.comparing(org.springframework.validation.FieldError::getField))
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed.");
        return ResponseEntity.badRequest().body(new ApiDtos.Message(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiDtos.Message> constraint(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .sorted()
                .findFirst()
                .orElse("Request validation failed.");
        return ResponseEntity.badRequest().body(new ApiDtos.Message(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiDtos.Message> unreadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ApiDtos.Message("Request body is missing or contains invalid JSON."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiDtos.Message> integrity(DataIntegrityViolationException exception) {
        log.warn("Database constraint rejected an API operation", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiDtos.Message("This change conflicts with existing data. Refresh and try again."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiDtos.Message> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ApiDtos.Message(exception.getReason() == null ? "Request failed." : exception.getReason()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiDtos.Message> unexpected(Exception exception) {
        log.error("Unhandled API exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiDtos.Message("Unexpected server error. Please try again later."));
    }
}

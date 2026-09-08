package com.reynan.inventoryservice.exceptions.handler;

import com.reynan.inventoryservice.exceptions.DuplicateResourceException;
import com.reynan.inventoryservice.exceptions.InsufficientStockException;
import com.reynan.inventoryservice.exceptions.ResourceAlreadyExistsException;
import com.reynan.inventoryservice.exceptions.ResourceNotFoundException;
import com.reynan.inventoryservice.exceptions.model.StandardError;
import com.reynan.inventoryservice.exceptions.model.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleAllExceptions(
            Exception exception,
            HttpServletRequest request
    ) {
        UUID traceId = UUID.randomUUID();
        log.error("[TraceID: {}] Internal Server Error at path: {}", traceId, request.getRequestURI(), exception);

        return ResponseEntity
                .status(getStatus(exception))
                .body(buildError("Unexpected internal server error", request, traceId));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        UUID traceId = UUID.randomUUID();
        log.warn("[TraceID: {}] Resource not found at path: {} | Reason: {}", traceId, request.getRequestURI(), exception.getMessage());

        return ResponseEntity
                .status(getStatus(exception))
                .body(buildError(exception.getMessage(), request, traceId));
    }

    @ExceptionHandler({DuplicateResourceException.class, ResourceAlreadyExistsException.class})
    public ResponseEntity<StandardError> handleConflictException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        UUID traceId = UUID.randomUUID();
        log.warn("[TraceID: {}] Duplicate resource at path: {} | Reason: {}",
                traceId, request.getRequestURI(), exception.getMessage());

        return ResponseEntity
                .status(getStatus(exception))
                .body(buildError(exception.getMessage(), request, traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        UUID traceId = UUID.randomUUID();
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("[TraceID: {}] Data validation failed at path: {} | Invalid fields: {}", traceId, request.getRequestURI(), errors.keySet());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationError(Instant.now(), errors, request.getRequestURI(), traceId));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<StandardError> handleInsufficientStockException(
            Exception exception,
            HttpServletRequest request
    ) {
        UUID traceId = UUID.randomUUID();
        log.warn("[TraceID: {}] Insufficient stock at path {}: {}", traceId, request.getRequestURI(), exception.getMessage());

        return ResponseEntity
                .status(getStatus(exception))
                .body(buildError(exception.getMessage(), request, traceId));
    }

    private StandardError buildError(
            String message,
            HttpServletRequest request,
            UUID traceId
    ) {
        return new StandardError(Instant.now(), message, request.getRequestURI(), traceId);
    }

    private HttpStatus getStatus(Exception exception) {
        ResponseStatus annotation = AnnotatedElementUtils.findMergedAnnotation(
                exception.getClass(),
                ResponseStatus.class
        );

        return annotation == null
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(annotation.code().value());
    }

}

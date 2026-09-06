package com.reynan.orderservice.exceptions.handler;

import com.reynan.orderservice.exceptions.model.StandardError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleAllExceptions(Exception ex, HttpServletRequest request) {

        String message = "Unexpected internal server error";
        UUID traceId = UUID.randomUUID();

        log.error("[TraceID: {}] Internal Server Error at path: {} | Message: {}", traceId, request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(message, request, traceId));
    }

    private StandardError buildError(
            String message,
            HttpServletRequest request,
            UUID traceId
    ) {
        return new StandardError(
                Instant.now(),
                message,
                request.getRequestURI(),
                traceId
        );
    }
}

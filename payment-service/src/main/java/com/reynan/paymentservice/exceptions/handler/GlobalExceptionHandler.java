package com.reynan.paymentservice.exceptions.handler;

import com.reynan.paymentservice.exceptions.model.StandardError;
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
    public ResponseEntity<StandardError> handleAllExceptions(Exception exception, HttpServletRequest request) {
        UUID traceId = UUID.randomUUID();
        log.error("[TraceID: {}] Internal Server Error at path: {}", traceId, request.getRequestURI(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new StandardError(Instant.now(), "Unexpected internal server error", request.getRequestURI(), traceId));
    }
}

package com.spring.fit.backend.common.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.spring.fit.backend.common.model.response.ErrorDetail;
import com.spring.fit.backend.common.model.response.ErrorResponse;
import com.spring.fit.backend.common.model.response.RateLimitResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class AdviceController {

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorResponse> handleErrorException(ErrorException ex) {
        log.error("Inside handleErrorException(): {}", ex.getErrorResponse().getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ex.getErrorResponse());
    }

    // 429
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler(RateLimitException.class)
    public ErrorResponse handleTooManyRequestError(RateLimitException ex) {
        return RateLimitResponse.builder()
                .retryAfter(ex.getRetryAfter())
                .hourlyLimit(ex.getHourlyLimit())
                .currentUsage(ex.getCurrentUsage())
                .message(ex.getMessage())
                .build();
    }

    // 400 - HttpMessageNotReadableException
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadableError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        List<ErrorDetail> errors = new ArrayList<>();

        if (cause instanceof InvalidFormatException formatException) {
            Class<?> targetType = formatException.getTargetType();
            if (targetType.isEnum() || targetType.equals(LocalDateTime.class)) {
                String fieldName = formatException.getPath().get(0).getFieldName();
                String message = targetType.isEnum()
                        ? "Invalid value for enums: " + formatException.getValue()
                        : "Invalid date format for field '" + fieldName + "', expected format: yyyy-MM-dd HH:mm:ss";

                errors.add(ErrorDetail.builder()
                        .field(fieldName)
                        .message(message)
                        .build());
            }
        } else if (cause instanceof ValueInstantiationException) {
            String meg = Objects.requireNonNull(ex.getRootCause()).getMessage();
            String message = meg.isEmpty() ? "Invalid request format" : meg;
            errors.add(ErrorDetail.builder()
                    .field(null)
                    .message(message)
                    .build());
        }

        if (errors.isEmpty()) {
            errors.add(ErrorDetail.builder()
                    .field(null)
                    .message("Malformed JSON request")
                    .build());
        }

        return ErrorResponse.builder()
                .errors(errors)
                .build();
    }

    // 400 - MethodArgumentNotValid
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidError(MethodArgumentNotValidException ex) {
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> ErrorDetail.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build())
        .toList();

        return ErrorResponse.builder()
                .errors(errors)
                .build();
    }


    // 400 - ConstraintViolationException
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolationError(ConstraintViolationException ex) {
        List<ErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(violation -> ErrorDetail.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .build())
                .toList();

        return ErrorResponse.builder()
                .errors(errors)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalErrorException(Exception ex) {
        log.error("Error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().message(ex.getMessage()).build());
    }
}

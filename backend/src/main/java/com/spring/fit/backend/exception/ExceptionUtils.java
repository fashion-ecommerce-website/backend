package com.spring.fit.backend.exception;

import java.util.HashMap;
import java.util.Map;

public class ExceptionUtils {

    /**
     * Tạo ResourceNotFoundException với format chuẩn
     */
    public static ResourceNotFoundException resourceNotFound(String resourceName, String fieldName, Object fieldValue) {
        return new ResourceNotFoundException(resourceName, fieldName, fieldValue);
    }

    /**
     * Tạo ConflictException với format chuẩn
     */
    public static ConflictException conflict(String resourceName, String fieldName, Object fieldValue) {
        return new ConflictException(resourceName, fieldName, fieldValue);
    }

    /**
     * Tạo ValidationException với một error
     */
    public static ValidationException validationError(String field, String message) {
        Map<String, String> errors = new HashMap<>();
        errors.put(field, message);
        return new ValidationException("Validation failed", errors);
    }

    /**
     * Tạo ValidationException với nhiều errors
     */
    public static ValidationException validationErrors(Map<String, String> errors) {
        return new ValidationException("Validation failed", errors);
    }

    /**
     * Tạo BadRequestException
     */
    public static BadRequestException badRequest(String message) {
        return new BadRequestException(message);
    }

    /**
     * Tạo UnauthorizedException
     */
    public static UnauthorizedException unauthorized(String message) {
        return new UnauthorizedException(message);
    }

    /**
     * Tạo ForbiddenException
     */
    public static ForbiddenException forbidden(String message) {
        return new ForbiddenException(message);
    }
}
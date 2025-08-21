package com.spring.fit.backend.common.exception;


import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import com.spring.fit.backend.common.model.response.ErrorDetail;
import com.spring.fit.backend.common.model.response.ErrorResponse;

import java.util.Collection;

@Getter
@Accessors(chain = true)
public class ErrorException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorResponse errorResponse;

    private ErrorException(HttpStatus status, String type, String message, Collection<ErrorDetail> errors) {
        super(message);
        this.status = status;
        this.errorResponse = ErrorResponse.builder()
                .type(type)
                .message(message)
                .errors(errors)
                .build();
    }

    public ErrorException(HttpStatus status, String message) {
        this(status, null, message, null);
    }

    public ErrorException(HttpStatus status, String type, String message) {
        this(status, type, message, null);
    }

    public ErrorException(HttpStatus status, String message, Collection<ErrorDetail> errors) {
        this(status, null, message, errors);
    }
}

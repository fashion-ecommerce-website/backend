package com.spring.fit.backend.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalException extends RuntimeException {

    private final int status;
    private final String error;

    public InternalException() {
        super("An internal server error occurred.");
        this.status = 500;
        this.error = "Internal Server Error";
    }
}

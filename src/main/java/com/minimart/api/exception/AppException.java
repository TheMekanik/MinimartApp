package com.minimart.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String details;

    public AppException(HttpStatus status, String message, String details) {
        super(message);
        this.httpStatus = status;
        this.details = details;
    }

}

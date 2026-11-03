package com.minimart.api.exception;

import com.minimart.api.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAppException(
            AppException ex
    ) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponseDto.<Void>builder()
                        .code(ex.getHttpStatus().value())
                        .message(ex.getMessage())
                        .details(ex.getDetails())
                        .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<List<String>>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.ok().body(
                ApiResponseDto.<List<String>>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(HttpStatus.BAD_GATEWAY.getReasonPhrase())
                        .data(errors)
                        .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidRequestFormat(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .details(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDatabaseException(
            DataIntegrityViolationException ex
    ) {
        return ResponseEntity.ok().body(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.CONFLICT.value())
                        .message("Data conflict or constraint violation")
                        .details(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(
            Exception ex
    ) {
        log.error("Unexpected error occured : ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.<Void>builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .build()
                );
    }
}

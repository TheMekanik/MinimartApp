package com.minimart.api.dto;

public record ErrorResponseDto(
        Integer code,
        String message,
        String details
) {
}

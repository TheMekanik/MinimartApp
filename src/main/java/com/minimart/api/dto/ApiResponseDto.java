package com.minimart.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ApiResponseDto<T>(
        Integer code,
        String message,
        String details,
        T data,
        PaginationDto meta
) {
}

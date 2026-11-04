package com.minimart.api.dto.store;

import com.minimart.api.dto.PaginationDto;

import java.util.List;

public record StoresResponseDto(
        List<StoreResponseDto> stores,
        PaginationDto meta
) {
}

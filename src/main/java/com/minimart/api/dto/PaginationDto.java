package com.minimart.api.dto;

public record PaginationDto(
        int currentPage,
        int perPage,
        long totalItems,
        int totalPages,
        long from,
        long to,
        boolean hasNextPage,
        boolean hasPrevPage
) {
}

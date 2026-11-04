package com.minimart.api.dto.branch;

import com.minimart.api.dto.PaginationDto;

import java.util.List;

public record BranchesResponseDto(
        List<BranchResponseDto> branches,
        PaginationDto meta
) {
}

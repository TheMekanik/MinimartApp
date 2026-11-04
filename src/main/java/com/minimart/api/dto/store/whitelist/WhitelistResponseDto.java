package com.minimart.api.dto.store.whitelist;

import com.minimart.api.dto.PaginationDto;

import java.util.List;

public record WhitelistResponseDto(
        List<WhitelistDto> whitelists,
        PaginationDto meta
) {
}

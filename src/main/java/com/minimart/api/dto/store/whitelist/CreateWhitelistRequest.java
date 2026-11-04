package com.minimart.api.dto.store.whitelist;

import jakarta.validation.constraints.NotNull;

public record CreateWhitelistRequest(
        @NotNull(message = "store id is required")
        Long store_id
) {
}

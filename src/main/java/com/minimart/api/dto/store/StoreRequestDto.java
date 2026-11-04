package com.minimart.api.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreRequestDto(
        Long id,
        @NotNull(message = "Branch id is required")
        Long branch_id,

        @NotBlank(message = "Store name is required")
        @Size(max = 150, message = "Store name must not exceed 150 characters")
        String store_name,

        @NotBlank(message = "Store code is required")
        @Size(max = 30, message = "Store code must not exceed 30 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Store code may only contain letters, numbers, underscores, and hyphens"
        )
        String store_code,

        @NotNull(message = "Active status is required")
        Boolean is_active
) {
}
